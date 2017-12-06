const CIRCUMFERENCE = 3;
const AXIS_HEIGHT = 25;
const PADDING = 20;
const BORDER = 25;
const SVG_WIDTH = 800;
		
let laneIndex = new Array(500);
let laneMax = new Array(500);
let numLanes = 1;
laneIndex[0] = 0;

let isDragging = false;
let mouseDownX = 0;
let mouseDownY = 0;
let mouseUpX = 0;
let mouseUpY = 0;
let selectRect = null;

let minX = 0;
let maxX = 0;
let minY = 0;
let maxY = 0;

let dataAreSelected = false;

let dataArea = null;
let line1 = null;
let line2 = null;

let xScale = null;

let dataMean = 0;
let dataSd = 0;
let eventHandler = null;

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

function setThresholdLines(numSd) {
	lowerThresh = dataMean - numSd * dataSd;
	lowerX = xScale(lowerThresh) + BORDER;
	line1.attr("x1", lowerX).attr("x2", lowerX);
	upperThresh = dataMean + numSd * dataSd;
	upperX = xScale(upperThresh) + BORDER;
	line2.attr("x1", upperX).attr("x2", upperX);
}

function renderBeeswarm(dataUrl, fieldName, mean, sd, numSd, siteName, subjectName, siteFieldName, subjectFieldName, handler) {
	dataMean = mean;
	dataSd = sd;
	eventHandler = handler;
	
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
		const lowerThresh = mean - numSd * sd;
		const upperThresh = mean + numSd * sd;
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
				return (siteName != "-1" && d[siteFieldName] != siteName) ||
					(subjectName != "-1" && d[subjectFieldName] != subjectName)
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
				/*
				document.getElementById("button_show").setAttribute("disabled", "true");
				document.getElementById("button_query").setAttribute("disabled", "true");
				document.getElementById("button_change").setAttribute("disabled", "true");
				document.getElementById("button_suppress").setAttribute("disabled", "true");
				*/
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
						if ((siteName == "-1" && subjectName == "-1") || d[siteFieldName] == siteName || d[subjectFieldName] == subjectName) {
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
				/*
				if (dataAreSelected) {
					document.getElementById("button_show").removeAttribute("disabled");
					document.getElementById("button_query").removeAttribute("disabled");
					document.getElementById("button_change").removeAttribute("disabled");
					document.getElementById("button_suppress").removeAttribute("disabled");
				}
				*/
			}
		});
	});
}

let selectedData = null;

function getSelectedData() {
	selectedData = new Array();
	dataArea.selectAll(".data-selected").each(function(d) {
		selectedData.push(d);
	});
	return selectedData;
}

let hmtl = "";

function onExport() {
	html = "<table class='wide'><tr><th>Site</th><th>RecruitID</th><th>Event</th><th>"
		+ dataFieldName + "</th></tr>";
	dataArea.selectAll(".data-selected").each(function(d) {
		let row =
			"<tr>" +
			"<td>" + d.Site + "</td>" +
			"<td>" + d.RecruitID + "</td>" +
			"<td>" + d.event + "</td>" +
			"<td>" + d.value + "</td>" +
			"</tr>";
		html += row;
	});
	hmtl += "</table>"
	$("#dialog_data").html(html);
	$("#dialog_data").dialog("open");
}

function onChange() {
	let option = document.getElementById("select_label").value;
	if (option == "issue") {
		let recruitIds = "";
		let events = "";
		let count = 0;
		dataArea.selectAll(".inlier-selected")
			.each(function(d) {
				count++;
				if (count > 1) {
					recruitIds += ",";
					events += ",";
				}
				recruitIds += d[subjectFieldName];
				events += d["event"];
			});
		let data =
			"data_field_id=" + dataFieldId + "&" +
			"recruit_ids=" + recruitIds + "&" +
			"events=" + events.replace(/&/g, "%26");
		data = encodeURI(data);
		//console.log(data);
		$.post("/rest/anomaly/is_an_issue", data, function() {
			dataArea.selectAll(".inlier-selected")
			.classed("inlier", false)
			.classed("inlier-selected", false)
			.classed("outlier", true)
			.classed("outlier-selected", true);
		})
	}
	else if (option == "non-issue") {
		let data = "anomaly_ids=";
		let count = 0;
		dataArea.selectAll(".outlier-selected")
			.each(function(d) {
				count++;
				if (count > 1) {
					data += ","
				}
				data += d["anomaly_id"];
			});
		$.post("/rest/anomaly/not_an_issue", data, function() {
			dataArea.selectAll(".outlier-selected")
			.classed("outlier", false)
			.classed("outlier-selected", false)
			.classed("inlier", true)
			.classed("inlier-selected", true)
		});
	}
}

function onSave() {
	document.getElementById("button_save").setAttribute("disabled", "true");
	let x1 = caret1.attr("points").split(",")[0] - BORDER;
	let x2 = caret2.attr("points").split(",")[0] - BORDER;
	lowerThreshold = xScale.invert(x1);
	upperThreshold = xScale.invert(x2);
	const data = "data_field_id=" + dataFieldId + "&lower_threshold=" + lowerThreshold
		+ "&upper_threshold=" + upperThreshold;
	$.post("/rest/anomaly/set_univariate_thresholds", data, function() {
		dataArea.selectAll("circle").each(function(d) {
			let v = parseFloat(d["value"]);
			//console.log(v);
			if (v < lowerThreshold || v > upperThreshold) {
				d3.select(this)
				.classed("inlier", false)
				.classed("outlier", true);
			}
			else {
				d3.select(this)
				.classed("outlier", false)
				.classed("inlier", true);
			}
		});
	});
}
