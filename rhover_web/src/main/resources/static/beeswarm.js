// Drawing constants
const CIRCUMFERENCE = 3;
const AXIS_HEIGHT = 25;
const PADDING = 20;
const BORDER = 25;
const SVG_WIDTH = 800;
const MAX_LANES = 100;
		
// Helper variables for laying out data points in beeswarm configuration
let laneOrder = new Array(MAX_LANES);
let laneMaxX = new Array(MAX_LANES);
let numLanes = 1;
laneOrder[0] = 0;
const overflows = new Array();
let currentOverflow = null;

// State variables used during user interaction
let isDragging = false;
let mouseDownX = 0;
let mouseDownY = 0;
let mouseUpX = 0;
let mouseUpY = 0;
let selectRect = null;
let dataAreSelected = false;
let line1 = null;
let line2 = null;
let selectedData = null;

// Variables computed during initial plotting and re-used when
// the user changes something
let minX = 0;
let maxX = 0;
let minY = 0;
let maxY = 0;
let dataMidPoint = 0;
let dataArea = null;
let xScale = null;

// Statistical properties
let dataMean = 0;
let dataSd = 0;

// Parameters set by the user
let lowerThresh = -1;
let upperThresh = -1;
let siteFilter = -1;
let subjectFilter = -1;
let filterCriteria = [];
let highlightCriteria = [];

// Metadata passed in by system
let siteField = null;
let subjectField = null;

// HTML client event handler function, which is invoked
// upon certain user actions, such as selecting data
let eventHandler = null;

// SVG canvas object
let svg = null;
let brush = null;
let brushGroup = null;
let dataPoints = null;

//
// FUNCTIONS INVOKED BY CLIENT WEB PAGE
//

/**
 * Set data filter
 * @param entity Subject or site
 * @param value Subject ID or site ID
 */
function setFilter(entity, value) {
	if (entity == "none") {
		siteFilter = -1;
		subjectFilter = -1;
	}
	else if (entity == "site") {
		siteFilter = value;
		subjectFilter = -1;
	}
	else if (entity == "subject") {
		subjectFilter = value;
		siteFilter = -1;
	}
}

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
}

function setHighlightCriteria(criteria) {
	highlightCriteria = criteria;
}

/**
 * Get data selected by user
 * @returns Array of data records
 */
function getSelectedData() {
	selectedData = new Array();
	dataArea.selectAll(".selected").each(function(d) {
		selectedData.push(d);
	});
	return selectedData;
	//return dataArea.selectAll(".selected");
}

/**
 * Redraw plot
 */
function reDraw() {
	dataPoints.classed("background", isBackground);
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
 * Get y-coordinate for a data point in the beeswarm.  Function will position
 * data point in a vertical lane so that it does not overlap 
 * @param x X-coordinate in pixels
 * @returns Y coordinate in pixels
 */
function getY(x) {
	let lane = 0;
	while (lane < numLanes && (laneMaxX[lane] + 2 * CIRCUMFERENCE) > x) {
		lane++;
	}
	if (lane >= MAX_LANES) {
		if (currentOverflow == null) {
			currentOverflow = new Object();
			currentOverflow.x = x;
			currentOverflow.count = 1;
			overflows.push(currentOverflow);
		}
		else if (x < currentOverflow.x + 2 * CIRCUMFERENCE) {
			currentOverflow.count++;
		}
		else {
			currentOverflow = new Object();
			currentOverflow.x = x;
			currentOverflow.count = 1;
			overflows.push(currentOverflow);
		}
		return NaN;
	}
	laneMaxX[lane] = x;
	if (lane == numLanes) {
		numLanes++;
		if (laneOrder[lane - 1] == 0) {
			laneOrder[lane] = -1;
		}
		else if (laneOrder[lane - 1] < 0) {
			laneOrder[lane] = -laneOrder[lane - 1];
		}
		else {
			laneOrder[lane] = -laneOrder[lane - 1] - 1;
		}
	}
	let y = laneOrder[lane] * 2 * CIRCUMFERENCE;
	return y;
}

/**
 * Compute y coordinate of each data point and return height of plot
 * @param data Dataset to plot
 * @param fieldName Name of field to plot
 * @param xScale Conversion from domain values to pixels
 */
function setYAndComputHeight(data, fieldName, xScale) {
	let minY = 0;
	let maxY = 0;
	let count = 0;
	data.forEach(function(d) {
		let y = getY(xScale(d[fieldName]));
		d.__y__ = y;
		if (!isNaN(y)) {
			count++;
			if (count == 1) {
				minY = y;
				maxY = y;
			}
			if (y < minY) {
				minY = y;
			}
			if (y > maxY) {
				maxY = y;
			}
		}
	});

	return maxY - minY;
}

function onMouseDown(mouseCoords) {
	d3.selectAll("rect").remove();
	if (dataAreSelected) {
		dataArea.selectAll(".outlier-selected").classed("outlier-selected", false);
		dataArea.selectAll(".inlier-selected").classed("inlier-selected", false);
		dataArea.selectAll(".data-selected").classed("data-selected", false);
		dataAreSelected = false;
		eventHandler(false);
	}
	isDragging = true;
	mouseDownX = mouseCoords[0];
	mouseDownY = mouseCoords[1];
	selectRect = svg.append("rect")
		.attr("x", mouseDownX)
		.attr("y", mouseDownY)
		.attr("width", 0)
		.attr("height", 0)
		.classed("select-rect", true);
}

function onMouseMove(mouseCoords) {
	let x = mouseCoords[0];
	let y = mouseCoords[1];
	if (isDragging) {
		minX = mouseDownX;
		maxX = x;
		if (minX > maxX) {
			minX = x;
			maxX = mouseDownX;
		}
		minY = mouseDownY;
		maxY = y;
		if (minY > maxY) {
			minY = y;
			maxY = mouseDownY;
		}
		let width = maxX - minX;
		let height = maxY - minY;
		selectRect
			.attr("x", minX)
			.attr("y", minY)
			.attr("width", width)
			.attr("height", height);
	}
}

function onMouseUp(mouseCoords) {
	mouseUpX = mouseCoords[0];
	mouseUpY = mouseCoords[1];
	if (isDragging) {
		isDragging = false;
		selectRect.remove();
		minX = mouseDownX;
		maxX = mouseUpX;
		if (minX > maxX) {
			minX = mouseUpX;
			maxX = mouseDownX;
		}
		minY = mouseDownY;
		maxY = mouseUpY;
		if (minY > maxY) {
			minY = mouseUpY;
			maxY = mouseDownY;
		}
		minX -= BORDER;
		maxX -= BORDER;
		dataArea.selectAll("circle").each(function(d) {
			const x = d3.select(this).attr("cx");
			const y = dataMidPoint + parseInt(d3.select(this).attr("cy"));
			if (x >= minX && x <= maxX && y >= minY && y <= maxY) {
				if ((siteFilter == "-1" && subjectFilter == "-1") || d[siteField] == siteFilter || d[subjectField] == subjectFilter) {
					let node = d3.select(this);
					node.classed("data-selected", true);
					if (node.classed("outlier")) {
						node.classed("outlier-selected", true);
					}
					else if (node.classed("inlier")) {
						node.classed("inlier-selected", true)
					}
					dataAreSelected = true;
				}
			}
		});
		if (dataAreSelected) {
			eventHandler(true);
		}
	}
}

function isBrushed(brushCoords, dataPoint) {
	const x = parseInt(dataPoint.attr("cx")) + BORDER;
	const y = parseInt(dataPoint.attr("cy")) + dataMidPoint;
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
	dataPoints.classed("selected", false);
	dataPoints.classed("inlier-selected", false);
	dataPoints.classed("outlier-selected", false);
	dataPoints.classed("deselected", true);
}

function onBrush() {
	if (d3.event.selection == null) {
		return;
	}
	dataPoints.classed("selected", false);
	dataPoints.classed("inlier-selected", false);
	dataPoints.classed("outlier-selected", false);
	dataPoints.classed("deselected", true);
	const brushCoords = d3.brushSelection(this);
	const selectedPoints = dataPoints.filter(function() {
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
	let passes = false;
	for (let i = 0; i < filterCriteria.length && !passes; i++) {
		let propName = filterCriteria[i].name;
		let propValues = filterCriteria[i].values;
		for (let j = 0; j < propValues.length && !passes; j++) {
			let propValue = propValues[j];
			if (dataPoint[propName] == propValue) {
				passes = true;
			}
		}
	}
	return passes;
}

/**
 * Render the beeswarm
 * @param dataUrl URL to retrieve data
 * @param fieldName Name of field to plot
 * @param mean Statistical mean of field values
 * @param sd Statistical SD of field values
 * @param numSd Threshold value in SD units
 * @param siteFieldName Name of field providing site name
 * @param subjectFieldName Name of field providing subject name
 * @param handler Client event handler invoked later on when use performs certain actions,
 * such as selecting data
 */
function renderBeeswarm(dataUrl, fieldName, mean, sd, numSd, siteFieldName, subjectFieldName, handler) {
	dataMean = mean;
	dataSd = sd;
	eventHandler = handler;
	siteField = siteFieldName;
	subjectField = subjectFieldName;
	
	d3.csv(dataUrl, function(data) {
		//console.log(data);
		
		// Set extent of data and chart areas on the screen
		const min = data[0][fieldName];
		const max = data[data.length - 1][fieldName];
		const dataAreaWidth = SVG_WIDTH - 2 * BORDER;
		xScale = d3.scaleLinear()
			.domain([min, max])
			.range([0, dataAreaWidth]);
		let dataHeight = setYAndComputHeight(data, fieldName, xScale);
		laneMaxX[0] = min - 100;
		const svgHeight = dataHeight + 2 * BORDER + PADDING + AXIS_HEIGHT;
		svg = d3.select("svg")
			.attr("width", SVG_WIDTH)
			.attr("height", svgHeight);
		dataMidPoint = BORDER + dataHeight / 2;
		dataArea = svg.append("g")
			.attr("transform", "translate(" + BORDER + ", " + dataMidPoint + ")");
		const axisY = dataHeight + BORDER + PADDING;
		const axisArea = svg.append("g")
			.attr("transform", "translate(" + BORDER + ", " + axisY + ")");
					
		// Draw axis
		const xAxis = d3.axisBottom().scale(xScale);
		axisArea.call(xAxis);
		
		// Draw data points
		lowerThresh = mean - numSd * sd;
		upperThresh = mean + numSd * sd;
		dataPoints = dataArea.selectAll("circle")
			.data(data)
			.enter()
			.append("circle")
			.filter(function(d) {
				return !isNaN(d.__y__);
			})
			.attr("cx", function(d) {return xScale(d[fieldName]);})
			.attr("cy", function(d) {return d.__y__;})
			.attr("r", CIRCUMFERENCE)
			.classed("deselected", true)
			.classed("outlier", isOutlier)
			.classed("inlier", isInlier)
			.classed("background", isBackground);
		
		// Drawn any overflows
		svg.selectAll(".overflow-mark-top")
			.data(overflows)
			.enter()
			.append("polygon")
			.attr("points", function(d) {
				let points = (BORDER + d.x - CIRCUMFERENCE) + "," + (BORDER - 3 * CIRCUMFERENCE) + " " + (BORDER + d.x) + ","
					+ (BORDER - 3 * CIRCUMFERENCE - 5) + " " + (d.x + CIRCUMFERENCE + BORDER) + "," + (BORDER - 3 * CIRCUMFERENCE);
				return points;
			})
			.attr("x1", function(d) {return d.x + BORDER;})
			.attr("y1", BORDER - 20)
			.attr("x2", function(d) {return d.x + BORDER;})
			.attr("class", "overflow-mark-top")
			.attr("y2", BORDER - CIRCUMFERENCE * 2)
			.style("fill", "green");
		svg.selectAll(".overflow-mark-bottom")
			.data(overflows)
			.enter()
			.append("polygon")
			.attr("points", function(d) {
				let points = (
						BORDER + d.x - CIRCUMFERENCE) + "," + (BORDER + dataHeight + CIRCUMFERENCE) + " " + 
						(BORDER + d.x) + "," + (BORDER + dataHeight + CIRCUMFERENCE + 5) + " " + 
						(d.x + CIRCUMFERENCE + BORDER) + "," + (BORDER + dataHeight + CIRCUMFERENCE);
				return points;
			})
			.attr("x1", function(d) {return d.x + BORDER;})
			.attr("y1", BORDER - 20)
			.attr("x2", function(d) {return d.x + BORDER;})
			.attr("class", "overflow-mark-bottom")
			.attr("y2", BORDER - CIRCUMFERENCE * 2)
			.style("fill", "green");
		
		// Draw threshold lines
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
		
		brush = d3.brush()
			.on("start", onBrushStart)
			.on("brush", onBrush)
			.on("end", onBrushEnd)
		brushGroup = svg.append("g").call(brush);
	});
}
