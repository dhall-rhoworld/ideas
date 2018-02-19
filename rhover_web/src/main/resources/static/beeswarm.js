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

let highlightedSubjects = [];

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

// ------------------------------------

function initializeHighlightDialogFields() {
	$("#highlight_site").click(function() {
		onHighlightChange();
	});
	
	$("#highlight_phase").click(function() {
		onHighlightChange();
	});
	
	$("#highlight_remove").click(function() {
		$("#highlight_site").val("");
		$("#highlight_phase").val("");
		onHighlightChange();
	});
}

function synchronizeHighlightsBar(criteria) {
	$("#highlights_bar").empty();
	if (criteria.length > 0) {
		let html = "<span class='filter_label'>Highlight:</span>";
		for (let i = 0; i < criteria.length; i++) {
			let propName = criteria[i].name;
			let propValues = criteria[i].values;
			for (let j = 0; j < propValues.length; j++) {
				let propValue = propValues[j];
				html += "<span class='filter_button'><img src='/images/close.png' height='20' width='20' onclick=\"removeHighlightFromDialog('";
				html += propName;
				html += "', '";
				html += propValue;
				html += "');\"/>&nbsp;";
				html += propName;
				html += " = ";
				html += propValue;
				html += "</span>";
			}
		}
		$("#highlights_bar").html(html);
	}
}

function removeHighlightFromDialog(propName, propValue) {
	if (propName === subjectFieldName) {
		const p = highlightedSubjects.indexOf(propValue);
		highlightedSubjects.splice(p, 1);
	}
	else {
		const values = $("select[name='highlight-" + propName + "']").val();
		const p = values.indexOf(propValue);
		values.splice(p, 1);
		$("select[name='highlight-" + propName + "']").val(values);
	}
	onHighlightChange();
}

function onHighlightChange() {
	const sites = $("#highlight_site").val();
	const phases = $("#highlight_phase").val();
	const criteria = [];
	if (sites.length > 0) {
		let criterion = {};
		criterion.name = siteFieldName;
		criterion.values = sites;
		criteria.push(criterion);
	}
	if (phases.length > 0) {
		let criterion = {};
		criterion.name = phaseFieldName;
		criterion.values = phases;
		criteria.push(criterion);
	}
	if (highlightedSubjects.length > 0) {
		let criterion = {};
		criterion.name = subjectFieldName;
		criterion.values = highlightedSubjects;
		criteria.push(criterion);
	}
	setHighlightCriteria(criteria);
	reDraw();
	synchronizeHighlightsBar(criteria);
}

function isHighlighted(propName, propValue) {
	if (propName === subjectFieldName) {
		return highlightedSubjects.indexOf(propValue) >= 0;
	}
	const values = $("select[name='highlight-" + propName + "']").val();
	return values.indexOf(propValue) >= 0;
}

function onClickHighlightButton(propName, propValue) {
	const selector = "span[data-filter-prop-name='" + propName + "'][data-filter-prop-value='" + propValue + "']";
	const highlighted = $(selector).hasClass("highlight_button_small_clicked");
	if (highlighted) {
		$(selector).removeClass("highlight_button_small_clicked");
		$(selector).addClass("highlight_button_small");
		removeHighlightFromDialog(propName, propValue);
	}
	else {
		if (propName === subjectFieldName) {
			if (highlightedSubjects.indexOf(propValue) == -1) {
				highlightedSubjects.push(propValue);
			}
		}
		else {
			const values = $("select[name='highlight-" + propName + "']").val();
			if (values.indexOf(propValue) == -1) {
				values.push(propValue);
			}
			$("select[name='highlight-" + propName + "']").val(values);
		}
		$(selector).removeClass("highlight_button_small");
		$(selector).addClass("highlight_button_small_clicked");
	}
	onHighlightChange();
}

function onFilterChange() {
	const sites = $("#filter_site").val();
	const phases = $("#filter_phase").val();
	const filters = {};
	if (sites.length > 0) {
		filters[siteFieldName] = sites;
	}
	if (phases.length > 0) {
		filters[phaseFieldName] = phases;
	}
	console.log(JSON.stringify(filters));
}

function initializeFilters() {
	$("#filter_site").click(function() {
		onFilterChange();
	});
	
	$("#filter_phase").click(function() {
		onFilterChange();
	});
	
	$("#filter_remove").click(function() {
		$("#filter_site").val("");
		$("#filter_phase").val("");
		onFilterChange();
	});
}

/**
 * Initialize dialogs
 */
function initializeDialogs() {
	
	// Dialog for displaying user-selected data
	$("#dialog_data").dialog({
		autoOpen: false,
		modal: true,
		minWidth: 800
	});
	
	// Plot contorol dialog
	$("#dialog_controls").dialog({
		autoOpen: false,
		modal: false,
		minWidth: 500,
		title: "Chart Options"
	});
	
	$("#dialog_charts").dialog({
		autoOpen: false,
		modal: true,
		minWidth: 700,
		title: "Other Charts"
	});
}

/**
 * Initialize widgets
 */
function initializeWidgets() {
	$("#img_boundary").tooltip();
	
	$("#button_options").click(function() {
		$("#dialog_controls").dialog("open");
	});
	
	$("#button_charts").click(function() {
		$("#dialog_charts").dialog("open");
	});
	
	$("#spinner_sd").spinner({
		step: 0.25,
		numberFormat: "n",
		stop: function() {
			const sd = $("#spinner_sd").val();
			setThresholdLines(sd);
		}
	});
	
	
	$("#button_show").click(function() {
		onClickShowData();
	});
	
	$("#text_second_field").click(function() {
		if ($("#text_second_field").val() == "Enter second variable") {
			$("#text_second_field").removeClass("input_hint");
			$("#text_second_field").val("");
			
			$("#text_second_field").autocomplete({
				minLength: 4,
				source: function(request, response) {
					const url = "/rest/admin/study/get_matching_field_instances?study_id="
						+ studyId + "\u0026term=" + request.term;
					$.get(url)
						.done(function(data) {
							response(data);
						})
						.fail(function() {
							console.log("Error");
						});
				}
			});
		}
	});
	
	$("#button_scatter").click(function() {
		const value = $("#text_second_field").val();
		const namePatt = new RegExp("\\(.*\\)");
		const fieldNameField = namePatt.exec(value);
		const datasetPatt = new RegExp("\\[.*\\]");
		const datasetField = datasetPatt.exec(value);

		// TODO: Add logic to deal with invalid variables
		const fieldName2 = fieldNameField[0].substring(1, fieldNameField[0].length - 1);
		const datasetName2 = datasetField[0].substring(1, datasetField[0].length - 1);
		
		const url = "/anomaly/bivariate_scatter?" +
				"field_name_1=" + fieldName1 + "\u0026" +
				"dataset_name_1=" + datasetName + "\u0026" +
				"field_name_2=" + fieldName2 + "\u0026" +
				"dataset_name_2=" + datasetName2 + "\u0026" +
				"dataset_id=" + datasetId;
		window.location = url;
	});
}

function onClickShowData() {
	const data = getSelectedData();
	let varNames = Object.keys(data[0]);
	varNames.splice(varNames.indexOf("anomaly_id"), 1);
	varNames.splice(varNames.indexOf("__y__"), 1);
	let html = "<tr>";
	for (var i in varNames) {
		html += "<th>";
		html += varNames[i];
		html += "</th>"
	}
	for (var i in data) {
		html += "<tr>";
		for (var j in varNames) {
			let varName = varNames[j];
			let buttonId = "button-" + varName + "---" + data[i][varName];
			html += "<td>";
			if (varName === subjectFieldName || varName === siteFieldName || varName === phaseFieldName) {
				let className = "highlight_button_small";
				if (isHighlighted(varName, data[i][varName])) {
					className = "highlight_button_small_clicked";
				}
				html += "<span title='Click to highlight data' class='";
				html += className;
				html += "' data-filter-prop-name='";
				html += varName;
				html += "' data-filter-prop-value='";
				html += data[i][varName];
				html += "'><img src='/images/highlighter.png' height='15' width='15' onclick='onClickHighlightButton(\"";
				html += varName;
				html += "\", \"";
				html += data[i][varName];
				html += "\");'/></span>&nbsp;&nbsp;";
			}
			html += data[i][varName];
			html += "</td>";
		}
		html += "</tr>";
	}
	html += "</tr>";
	$("#dialog_data_table").html(html);
	$("#dialog_data").dialog("open");
}


$(function() {
	initializeDialogs();
	initializeWidgets();
	initializeFilters();
	initializeHighlightDialogFields();
	
	$("#tabs").tabs();
	
	// Set filters within beeswarm
	if (siteName != "-1") {
		setFilter("site", siteName);
	}
	else if (subjectName != "-1") {
		setFilter("subject", subjectName);
	}
	else {
		setFilter("none");
	}
	
	renderBeeswarm(url, fieldName, mean, sd, numSd, siteFieldName, subjectFieldName, function(itemsAreSelected) {
		if (itemsAreSelected) {
			$("#button_show").prop("disabled", false);
			$("#select_issue").prop("disabled", false);
			$("#button_status").prop("disabled", false);
		}
		else {
			$("#button_show").prop("disabled", true);
			$("#select_issue").prop("disabled", true);
			$("#button_status").prop("disabled", true);
		}
	});
	
	$.contextMenu({
		selector: "svg",
		items: {
			"options": {name: "Chart Options", icon: "fa-cogs"},
			"separator_1": {type: "cm_separator"},
			"data": {name: "Show selected data", icon: "fa-table"},
			"query": {name: "Add selected to Query List", icon: "fa-list"},
			"notissue": {name: "Label selected 'Not an Issue'", icon: "fa-check-circle"},
			"separator_2": {type: "cm_separator"},
			"scatter": {name: "Plot relationship with second variable", icon: "fa-chart-line"},
			"longitudinal": {name: "Plot data longitudinally"},
			"violin": {name: "Plot with additional variables"}
		},
		callback: function(key, options) {
			if (key == "options") {
				$("#dialog_controls").dialog("open");
			}
			else if (key == "scatter") {
				$("#dialog_charts").dialog("open");
			}
			else if (key == "data") {
				onClickShowData();
			}
		}
	});
});
