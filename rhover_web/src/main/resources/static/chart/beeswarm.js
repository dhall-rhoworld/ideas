// Drawing constants
const CIRCUMFERENCE = 3;
const AXIS_HEIGHT = 25;
const PADDING = 40;
const BORDER = 25;
const SVG_WIDTH = 800;
const MAX_LANES = 100;
const TRANS_DURATION = 750;
		
// Helper variables for laying out data points in beeswarm configuration
let panels = {};
let numPanels = 0;

// State variables used during user interaction
let line1 = null;
let line2 = null;

// Variables computed during initial plotting and re-used when
// the user changes something
let minX = 0;
let maxX = 0;
let minY = 0;
let maxY = 0;
let xScale = null;

// Statistical properties
let dataMean = 0;
let dataSd = 0;

// Parameters set by the user
let lowerThresh = -1;
let upperThresh = -1;
let filterCriteria = [];
let highlightCriteria = [];
let groupBy = "none";

// HTML client event handler function, which is invoked
// upon certain user actions, such as selecting data
let eventHandler = null;

// SVG canvas object
let svg = null;

let brush = null;
let brushGroup = null;

let highlightedSubjects = [];

let data = null;
let fieldToPlot = null;

let axisArea = null;

let recordIdField = null;

let min = NaN;
let max = NaN;

//
// FUNCTIONS INVOKED BY CLIENT WEB PAGE
//

/**
 * Set data filters.
 * 
 * Data points that do not match filter criteria
 * are not displayed. If the given filters array is empty, then no data
 * will be filtered.
 * 
 * @param criteria Array of filter criteria objects as follows:
 * 
 *                 {name: NAME, values: [VALUE1, VALUE2, ... , VALUEN]}
 *                
 *                 Data points that do not have an attributed named NAME with a
 *                 value in [VALUE...VALUEN] will not be displayed.
 */
function setFilterCriteria(criteria) {
	filterCriteria = criteria;
	reRenderBeeswarm();
}

function setHighlightCriteria(criteria) {
	highlightCriteria = criteria;
	svg.selectAll(".data-point").classed("background", isBackground);
}

function setGroupBy(attribute) {
	groupBy = attribute;
	reRenderBeeswarm();
}

/**
 * Get data selected by user
 * @returns Array of data records
 */
function getSelectedData() {
	selectedData = new Array();
	svg.selectAll(".selected").each(function(d) {
		selectedData.push(d);
	});
	return selectedData;
}

/**
 * Set x-axis position of vertical threhsold lines
 * @param numSd Number of SD from mean
 */
function setThresholdLines(numSd) {
	lowerThresh = dataMean - numSd * dataSd;
	lowerX = xScale(lowerThresh) + BORDER;
	line1.transition().attr("x1", lowerX).attr("x2", lowerX);
	upperThresh = dataMean + numSd * dataSd;
	upperX = xScale(upperThresh) + BORDER;
	line2.transition().attr("x1", upperX).attr("x2", upperX);
}

//
// HELPER FUNCTIONS FOR LAYING OUT CHART
//

/**
 * Set y-coordinate for a data point in the beeswarm.  Function will position
 * data point in a vertical lane so that it does not overlap.  Y-coordinate
 * is saves as property __y__ in the given data point.
 * @param dataPoint Data point to position
 */
function setY(dataPoint) {
	let panel = getPanel(dataPoint)
	let lane = 0;
	let x = xScale(dataPoint[fieldToPlot]);
	while (lane < panel.numLanes && (panel.laneMaxX[lane] + 2 * CIRCUMFERENCE) > x) {
		lane++;
	}
	if (lane >= MAX_LANES) {
		if (panel.currentOverflow == null) {
			panel.currentOverflow = new Object();
			panel.currentOverflow.x = x;
			panel.currentOverflow.count = 1;
			panel.overflows.push(panel.currentOverflow);
		}
		else if (x < panel.currentOverflow.x + 2 * CIRCUMFERENCE) {
			panel.currentOverflow.count++;
		}
		else {
			panel.currentOverflow = new Object();
			panel.currentOverflow.x = x;
			panel.currentOverflow.count = 1;
			panel.overflows.push(panel.currentOverflow);
		}
		return NaN;
	}
	panel.laneMaxX[lane] = x;
	if (lane == panel.numLanes) {
		panel.numLanes++;
		if (panel.laneOrder[lane - 1] == 0) {
			panel.laneOrder[lane] = -1;
		}
		else if (panel.laneOrder[lane - 1] < 0) {
			panel.laneOrder[lane] = -panel.laneOrder[lane - 1];
		}
		else {
			panel.laneOrder[lane] = -panel.laneOrder[lane - 1] - 1;
		}
	}
	let y = panel.laneOrder[lane] * 2 * CIRCUMFERENCE;
	dataPoint.__y__ = y;
	if (isNaN(panel.minY) || y < panel.minY) {
		panel.minY = y;
	}
	if (isNaN(panel.maxY) || y > panel.maxY) {
		panel.maxY = y;
	}
}

function getPanel(dataPoint) {
	let panelName = "default";
	if (groupBy != "none") {
		panelName = dataPoint[groupBy];
	}
	let panel = panels[panelName];
	if (panel === undefined) {
		panel = {};
		panel.panelName = panelName;
		panel.panelNum = numPanels;
		numPanels++;
		panel.laneOrder = new Array(MAX_LANES);
		panel.laneOrder[0] = 0;
		panel.laneMaxX = new Array(MAX_LANES);
		panel.numLanes = 1;
		panel.overflows = new Array();
		panel.currentOverflow = null;
		panel.minY = NaN;
		panel.maxY = NaN;
		panel.midY = NaN;
		
		panel.yCoord = function(dataPoint) {
			return this.midY + dataPoint.__y__;
		}
		
		panels[panelName] = panel;
	}
	return panel;
}

function numGroups() {
	if (groupBy === "none") {
		return 1;
	}
	const groups = new Object();
	data.forEach(function(d) {
		groups[d[groupBy]] = d[groupBy];
	});
	let count = 0;
	Object.keys(groups).forEach(function(key, index) {
		count++;
	});
	return count;
}

/**
 * Compute y coordinate of each data point and return height of plot
 * @param data Dataset to plot
 * @param xScale Conversion from domain values to pixels
 */
function setYAndComputHeight(data, xScale) {
	panels = new Object();
	numPanels = 0;
	let minY = 0;
	let maxY = 0;
	let count = 0;
	data.forEach(function(d) {
		if (passesFilter(d)) {
			setY(d);
		}
	});
	count = 0;
	let height = 0;
	Object.keys(panels).forEach(function(key, index) {
		count++;
		if (count > 1) {
			height += PADDING;
		}
		let panel = panels[key];
		panel.height = panel.maxY - panel.minY;
		panel.y = height + BORDER;
		panel.midY = panel.y + panel.height / 2;
		height += panel.height;
	});
	return height;
}

function isBrushed(brushCoords, dataPoint) {
	const panel = getPanel(dataPoint);
	const x = parseInt(dataPoint.attr("cx"));
	const y = parseInt(dataPoint.attr("cy"));
	const x1 = brushCoords[0][0];
	const y1 = brushCoords[0][1];
	const x2 = brushCoords[1][0];
	const y2 = brushCoords[1][1];
	return x1 <= x && x <= x2 && y1 <= y && y <= y2;
}

function onBrushStart() {
	if (!d3.event.selection) {
		return;
	}
	svg.selectAll(".data-point")
		.classed("selected", false)
		.classed("inlier-selected", false)
		.classed("outlier-selected", false)
		.classed("deselected", true);
}

function onBrush() {
	if (d3.event.selection == null) {
		return;
	}
	svg.selectAll(".data-point")
		.classed("selected", false)
		.classed("inlier-selected", false)
		.classed("outlier-selected", false)
		.classed("deselected", true);
	const brushCoords = d3.brushSelection(this);
	const selectedPoints = svg.selectAll(".data-point").filter(function() {
		return isBrushed(brushCoords, d3.select(this));
	});
	selectedPoints.classed("deselected", false);
	selectedPoints.classed("selected", true);
	selectedPoints.classed("inlier-selected", function() {
		return d3.select(this).classed("inlier");
	});
	selectedPoints.classed("outlier-selected", function() {
		return d3.select(this).classed("outlier");
	});
}

function onBrushEnd() {
	if (!d3.event.selection) {
		return;
	}
	d3.select(this).call(brush.move, null);
}

function isOutlier(dataPoint) {
	return dataPoint["anomaly_id"] > 0;
}

function isInlier(dataPoint) {
	return dataPoint["anomaly_id"] == 0;
}

function isBackground(dataPoint) {
	if (highlightCriteria.length == 0) {
		return false;
	}
	let background = true;
	for (let i = 0; i < highlightCriteria.length && background; i++) {
		let propName = highlightCriteria[i].name;
		let propValues = highlightCriteria[i].values;
		for (let j = 0; j < propValues.length && background; j++) {
			let propValue = propValues[j];
			if (dataPoint[propName] == propValue) {
				background = false;
			}
		}
	}
	return background;
}

function passesFilter(dataPoint) {
	if (filterCriteria.length == 0) {
		return true;
	}
	let passes = true;
	for (let i = 0; i < filterCriteria.length && passes; i++) {
		let propName = filterCriteria[i].name;
		let propValues = filterCriteria[i].values;
		let passesProp = false;
		for (let j = 0; j < propValues.length; j++) {
			let propValue = propValues[j];
			if (dataPoint[propName] == propValue) {
				passesProp = true;
				break;
			}
		}
		passes = passes && passesProp;
	}
	return passes;
}

function yCoordinate(dataPoint) {
	let panel = getPanel(dataPoint);
	return panel.yCoord(dataPoint);
}

function drawDataPoints() {
	const selection = svg.selectAll(".data-point")
		.data(data.filter(function(d) {
			return !isNaN(d.__y__) && passesFilter(d);
		}), function(d) {
			const key = d[recordIdField];
			return key;
		});
	
	// Exit
	selection.exit()
		.transition(TRANS_DURATION)
		.attr("cx", -CIRCUMFERENCE)
		.remove();
	
	// Enter
	selection.enter()
		.append("circle")
		.attr("cx", function(d) {return BORDER + xScale(d[fieldToPlot]);})
		.attr("cy", function(d) {
			return yCoordinate(d);
		})
		.attr("r", CIRCUMFERENCE)
		.classed("data-point", true)
		.classed("deselected", true)
		.classed("outlier", isOutlier)
		.classed("inlier", isInlier)
		.classed("background", isBackground);

	// Update
	selection
		.transition()
		.duration(TRANS_DURATION)
		.attr("cy", function(d) {
			return yCoordinate(d);
		});
}

function drawOverflowMarks() {
	Object.keys(panels).forEach(function(key, index) {
		let panel = panels[key];
		let className = "overflow-mark-top-" + panel.panelNum;
		const topOverflowMarks = 
			svg.selectAll("." + className)
				.data(panel.overflows, function(d) {
					return d.x;
				});
		topOverflowMarks.exit().remove();
		topOverflowMarks
			.enter()
			.append("polygon")
			.attr("points", function(d) {
				let points = (BORDER + d.x - CIRCUMFERENCE) + "," + (panel.y - 3 * CIRCUMFERENCE) + " " + (BORDER + d.x) + ","
					+ (panel.y - 3 * CIRCUMFERENCE - 5) + " " + (d.x + CIRCUMFERENCE + BORDER) + "," + (panel.y - 3 * CIRCUMFERENCE);
				return points;
			})
			.attr("x1", function(d) {return d.x + BORDER;})
			.attr("y1", BORDER - 20)
			.attr("x2", function(d) {return d.x + BORDER;})
			.attr("class", className)
			.attr("y2", BORDER - CIRCUMFERENCE * 2)
			.style("fill", "green");
		
		className = "overflow-mark-bottom-" + panel.panelNum;
		const bottomOverflowMarks = svg.selectAll("." + className)
			.data(panel.overflows, function(d) {return d.x;});
		bottomOverflowMarks.exit().remove();
		bottomOverflowMarks
			.enter()
			.append("polygon")
			.attr("points", function(d) {
				let points = (
						BORDER + d.x - CIRCUMFERENCE) + "," + (panel.y + panel.height + CIRCUMFERENCE) + " " + 
						(BORDER + d.x) + "," + (panel.y + panel.height + CIRCUMFERENCE + 5) + " " + 
						(d.x + CIRCUMFERENCE + BORDER) + "," + (panel.y + panel.height + CIRCUMFERENCE);
				return points;
			})
			.attr("x1", function(d) {return d.x + BORDER;})
			.attr("y1", BORDER - 20)
			.attr("x2", function(d) {return d.x + BORDER;})
			.attr("class", className)
			.attr("y2", BORDER - CIRCUMFERENCE * 2)
			.style("fill", "green");
	});
}

/**
 * Render the beeswarm
 * @param dataUrl URL to retrieve data
 * @param fieldName Name of field to plot
 * @param idField Name of record ID field
 * @param mean Statistical mean of field values
 * @param sd Statistical SD of field values
 * @param numSd Threshold value in SD units
 * @param handler Client event handler invoked later on when use performs certain actions,
 * such as selecting data
 */
function renderBeeswarm(dataUrl, fieldName, idField, mean, sd, numSd, handler) {
	fieldToPlot = fieldName;
	recordIdField = idField;
	dataMean = mean;
	dataSd = sd;
	eventHandler = handler;
	
	d3.csv(dataUrl, function(d) {
		data = d;
		//console.log(data);
		
		// Set extent of data and chart areas on the screen
		min = data[0][fieldToPlot];
		max = data[data.length - 1][fieldToPlot];
		const dataAreaWidth = SVG_WIDTH - 2 * BORDER;
		xScale = d3.scaleLinear()
			.domain([min, max])
			.range([0, dataAreaWidth]);
		let dataHeight = setYAndComputHeight(data, xScale);
		const svgHeight = dataHeight + 2 * BORDER + PADDING + AXIS_HEIGHT;
		svg = d3.select("svg")
			.attr("width", SVG_WIDTH)
			.attr("height", svgHeight);
		const axisY = dataHeight + BORDER + PADDING;
		axisArea = svg.append("g")
			.attr("transform", "translate(" + BORDER + ", " + axisY + ")");
		
		brush = d3.brush()
			.on("start", onBrushStart)
			.on("brush", onBrush)
			.on("end", onBrushEnd);
		brushGroup = svg.append("g").call(brush);
					
		// Draw axis
		const xAxis = d3.axisBottom().scale(xScale);
		axisArea.call(xAxis);
		
		// Draw data points
		svg.selectAll(".data-point")
			.data(data.filter(function(d) {
				return !isNaN(d.__y__) && passesFilter(d);
			}), function(d) {
				const key = d[recordIdField];
				return key;
			})
			.enter()
			.append("circle")
			.attr("cx", function(d) {return BORDER + xScale(d[fieldToPlot]);})
			.attr("cy", function(d) {
				return yCoordinate(d);
			})
			.attr("r", CIRCUMFERENCE)
			.classed("data-point", true)
			.classed("deselected", true)
			.classed("outlier", isOutlier)
			.classed("inlier", isInlier)
			.classed("background", isBackground);
		
		// Drawn any overflows
		drawOverflowMarks();
		
		// Draw threshold lines
		lowerThresh = mean - numSd * sd;
		upperThresh = mean + numSd * sd;
		let xLower = xScale(lowerThresh) + BORDER;
		let xUpper = xScale(upperThresh) + BORDER;
		let y1 = BORDER;
		let y2 = BORDER + dataHeight;
		line1 = svg.append("line")
			.attr("x1", xLower).attr("y1", y1).attr("x2", xLower).attr("y2", y2)
			.attr("stroke", "black").attr("stroke-width", 1).attr("stroke-dasharray", "5, 5");
		line2 = svg.append("line")
			.attr("x1", xUpper).attr("y1", y1).attr("x2", xUpper).attr("y2", y2)
			.attr("stroke", "black").attr("stroke-width", 1).attr("stroke-dasharray", "5, 5");
	});
}

function reRenderBeeswarm() {
	
	// Set extent of data and chart areas on the screen
	let dataHeight = setYAndComputHeight(data, xScale);
	const svgHeight = dataHeight + 2 * BORDER + PADDING + AXIS_HEIGHT;
	const axisY = dataHeight + BORDER + PADDING;
				
	// Update data points
	//drawDataPoints(false);
	
	let selection = svg.selectAll(".data-point")
	.data(data.filter(function(d) {
		return !isNaN(d.__y__) && passesFilter(d);
	}), function(d) {
		const key = d[recordIdField];
		return key;
	});

	// Exit
	selection.exit()
		.transition()
		.duration(TRANS_DURATION)
		.attr("cx", -CIRCUMFERENCE)
		.attr("cy", -CIRCUMFERENCE)
		.remove();
	
	// Enter
	const enteringPoints = selection.enter()
		.append("circle")
		.attr("cx", -CIRCUMFERENCE)
		.attr("cy", function(d) {
			let panel = getPanel(d);
			return panel.midY;
		})
		.attr("r", CIRCUMFERENCE)
		.classed("data-point", true)
		.classed("deselected", true)
		.classed("outlier", isOutlier)
		.classed("inlier", isInlier)
		.classed("background", isBackground);
	
	enteringPoints
		.transition()
		.duration(TRANS_DURATION)
		.attr("cx", function(d) {return BORDER + xScale(d[fieldToPlot]);})
		.attr("cy", function(d) {return yCoordinate(d);});
	
	// Update
	selection
		.transition()
		.duration(TRANS_DURATION)
		.attr("cy", function(d) {return yCoordinate(d);});
	
	// Update overflow marks
	drawOverflowMarks(dataHeight);
	
	// Update threshold lines
	let y2 = BORDER + dataHeight;
	line1
		.transition()
		.duration(TRANS_DURATION)
		.attr("y2", y2);
	line2
		.transition()
		.duration(TRANS_DURATION)
		.attr("y2", y2);
	
	axisArea
		.transition()
		.duration(TRANS_DURATION)
		.attr("transform", "translate(" + BORDER + ", " + axisY + ")");
	
	// Panel labels
	const panelKeys = Object.keys(panels);
	selection = svg.selectAll(".panel-label")
		.data(panelKeys, function(d) {return d;});
	
	selection
		.transition()
		.duration(TRANS_DURATION)
		.attr("y", function(d) {
			let panel = panels[d];
			return Math.floor(panel.midY - panel.height / 2);
		});
	
	selection.exit()
		.transition()
		.duration(TRANS_DURATION)
		.attr("x", -200)
		.attr("y", -20)
		.remove();
	
	const enteringText = selection.enter()
		.append("text")
		.filter(function(d) {return d != "default";})
		.attr("x", -200)
		.attr("y", -20)
		.classed("panel-label", true)
		.text(function(d) {return d;});
	
	enteringText
		.transition()
		.duration(TRANS_DURATION)
		.attr("x", BORDER)
		.attr("y", function(d) {
			let panel = panels[d];
			return Math.floor(panel.midY - panel.height / 2);
		});
	
	svg
		.transition()
		.duration(TRANS_DURATION)
		.attr("height", svgHeight);
}

