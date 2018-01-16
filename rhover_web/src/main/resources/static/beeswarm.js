// Drawing constants
const CIRCUMFERENCE = 3;
const AXIS_HEIGHT = 25;
const PADDING = 20;
const BORDER = 25;
const SVG_WIDTH = 800;
		
// Helper variables for laying out data points in beeswarm configuration
let laneIndex = new Array(500);
let laneMax = new Array(500);
let numLanes = 1;
laneIndex[0] = 0;

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

// Metadata passed in by system
let siteField = null;
let subjectField = null;

// HTML client event handler function, which is invoked
// upon certain user actions, such as selecting data
let eventHandler = null;

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
 * Get data selected by user
 * @returns Array of data records
 */
function getSelectedData() {
	selectedData = new Array();
	dataArea.selectAll(".data-selected").each(function(d) {
		selectedData.push(d);
	});
	return selectedData;
}

/**
 * Redraw plot
 */
function reDraw() {
	dataArea.selectAll("circle")
		.classed("outlier", function(d) {
			return d["anomaly_id"] > 0 && (d[fieldName] < lowerThresh || d[fieldName] > upperThresh);
		})
		.classed("inlier", function(d) {
			return d["anomaly_id"] == 0 || (d[fieldName] >= lowerThresh && d[fieldName] <= upperThresh);
		})
		.classed("background", function(d) {
			return (siteFilter != "-1" && d[siteField] != siteFilter) ||
				(subjectFilter != "-1" && d[subjectField] != subjectFilter)
		});
}

/**
 * Set x-axis position of vertical threhsold lines
 * @param numSd Number of SD from mean
 */
function setThresholdLines(numSd) {
	lowerThresh = dataMean - numSd * dataSd;
	lowerX = xScale(lowerThresh) + BORDER;
	line1.attr("x1", lowerX).attr("x2", lowerX);
	upperThresh = dataMean + numSd * dataSd;
	upperX = xScale(upperThresh) + BORDER;
	line2.attr("x1", upperX).attr("x2", upperX);
}

//
// HELPER FUNCTIONS FOR LAYING OUT CHART
//

/**
 * Get y-coordinate for a data point in the beeswarm
 * @param X coordinate in pixels
 * @returns Y coordinate in pixels
 */
function getY(x) {
	let lane = 0;
	while (lane < numLanes && (laneMax[lane] + 2 * CIRCUMFERENCE) > x) {
		lane++;
	}
	laneMax[lane] = x;
	if (lane == numLanes) {
		numLanes++;
		if (laneIndex[lane - 1] == 0) {
			laneIndex[lane] = -1;
		}
		else if (laneIndex[lane - 1] < 0) {
			laneIndex[lane] = -laneIndex[lane - 1];
		}
		else {
			laneIndex[lane] = -laneIndex[lane - 1] - 1;
		}
	}
	let y = laneIndex[lane] * 2 * CIRCUMFERENCE;
	return y;
}

/**
 * Compute height of beeswarm
 * @param data Dataset to plot
 * @param fieldName Name of field to plot
 * @param xScale Conversion from domain values to pixels
 */
function computeHeight(data, fieldName, xScale) {
	let minY = 0;
	let maxY = 0;
	let count = 0;
	data.forEach(function(d) {
		count++;
		let y = getY(xScale(d[fieldName]));
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
	});

	laneIndex = new Array(500);
	laneMax = new Array(500);
	numLanes = 1;
	laneIndex[0] = 0;
	
	return maxY - minY;
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
	console.log("siteFilter: " + siteFilter + ", subjectFilter: " + subjectFilter);
	console.log("siteFieldName: " + siteFieldName);
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
		let dataHeight = computeHeight(data, fieldName, xScale);
		laneMax[0] = min - 100;
		const svgHeight = dataHeight + 2 * BORDER + PADDING + AXIS_HEIGHT;
		const svg = d3.select("svg")
			.attr("width", SVG_WIDTH)
			.attr("height", svgHeight);
		const dataMidPoint = BORDER + dataHeight / 2;
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
		dataArea.selectAll("circle")
			.data(data)
			.enter()
			.append("circle")
			.attr("cx", function(d) {return xScale(d[fieldName]);})
			.attr("cy", function(d) {return getY(xScale(d[fieldName]));})
			.attr("r", CIRCUMFERENCE)
			.classed("outlier", function(d) {
				return d["anomaly_id"] > 0 && (d[fieldName] < lowerThresh || d[fieldName] > upperThresh);
			})
			.classed("inlier", function(d) {
				return d["anomaly_id"] == 0 || (d[fieldName] >= lowerThresh && d[fieldName] <= upperThresh);
			})
			.classed("background", function(d) {
				return (siteFilter != "-1" && d[siteField] != siteFilter) ||
					(subjectFilter != "-1" && d[subjectField] != subjectFilter)
			});
		
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
		
		// Add selection event handler
		svg.on("mousedown", function() {
			d3.selectAll("rect").remove();
			if (dataAreSelected) {
				dataArea.selectAll(".outlier-selected").classed("outlier-selected", false);
				dataArea.selectAll(".inlier-selected").classed("inlier-selected", false);
				dataArea.selectAll(".data-selected").classed("data-selected", false);
				dataAreSelected = false;
				eventHandler(false);
			}
			isDragging = true;
			mouseDownX = d3.mouse(this)[0];
			mouseDownY = d3.mouse(this)[1];
			selectRect = svg.append("rect")
				.attr("x", mouseDownX)
				.attr("y", mouseDownY)
				.attr("width", 0)
				.attr("height", 0)
				.classed("select-rect", true);
		});
		
		svg.on("mousemove", function() {
			let x = d3.mouse(this)[0];
			let y = d3.mouse(this)[1];
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
		});
		
		svg.on("mouseup", function() {
			mouseUpX = d3.mouse(this)[0];
			mouseUpY = d3.mouse(this)[1];
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
		});
	});
}




