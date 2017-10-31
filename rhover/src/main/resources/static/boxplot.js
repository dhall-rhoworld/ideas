const CIRCUMFERENCE = 3;
const BOX_HEIGHT = 40;
const AXIS_HEIGHT = 25;
const PADDING = 20;
const BORDER = 25;
const DATA_HEIGHT = 100;
const SVG_HEIGHT = BORDER * 2 + PADDING + AXIS_HEIGHT + DATA_HEIGHT;
const SVG_WIDTH = 800;
		
let isDragging = false;
let isMovingLowerThreshold = false;
let isMovingUpperThreshold = false;
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
let caret1 = null;
let caret2 = null;
let caretY = 0;

let xScale = null;

function computeCaretPoints(x) {
	let p1 = x + ", " + (caretY + 8);
	let p2 = (x - 8) + ", " + caretY;
	let p3 = (x + 8) + ", " + caretY;
	return p1 + " " + p2 + " " + p3;
}

function renderBoxplot(dataUrl, fieldName, lowerThresh, upperThresh, firstQuartile,
		secondQuartile, thirdQuartile) {
	
	d3.csv(dataUrl, function(data) {
		
		// Set extent of data and chart areas on the screen
		const min = data[0][fieldName];
		const max = data[data.length - 1][fieldName];
		const dataAreaWidth = SVG_WIDTH - 2 * BORDER;
		xScale = d3.scaleLinear()
			.domain([min, max])
			.range([0, dataAreaWidth]);
		const svg = d3.select("svg")
			.attr("width", SVG_WIDTH)
			.attr("height", SVG_HEIGHT);
		const dataMidPoint = BORDER + DATA_HEIGHT / 2;
		dataArea = svg.append("g")
			.attr("transform", "translate(" + BORDER + ", " + dataMidPoint + ")");
		const axisY = DATA_HEIGHT + BORDER + PADDING;
		const axisArea = svg.append("g")
			.attr("transform", "translate(" + BORDER + ", " + axisY + ")");
		
		/*
		console.log("svgHeight: " + svgHeight + ", dataMidPoint: " + dataMidPoint + ", axisY: " + axisY + ", dataAreaWidth: " + dataAreaWidth);
		svg.append("line").attr("x1", "0").attr("y1", "0").attr("x2", SVG_WIDTH).attr("y2", "0").attr("stroke", "black").attr("stroke-width", "1");
		svg.append("line").attr("x1", "0").attr("y1", svgHeight).attr("x2", SVG_WIDTH).attr("y2", svgHeight).attr("stroke", "black").attr("stroke-width", "1");
		svg.append("line").attr("x1", "0").attr("y1", BORDER).attr("x2", SVG_WIDTH).attr("y2", BORDER).attr("stroke", "black").attr("stroke-width", "1");
		svg.append("line").attr("x1", "0").attr("y1", svgHeight - BORDER).attr("x2", SVG_WIDTH).attr("y2", svgHeight - BORDER).attr("stroke", "black").attr("stroke-width", "1");
		svg.append("line").attr("x1", "0").attr("y1", axisY).attr("x2", SVG_WIDTH).attr("y2", axisY).attr("stroke", "black").attr("stroke-width", "1");
		svg.append("line").attr("x1", "0").attr("y1", dataMidPoint).attr("x2", SVG_WIDTH).attr("y2", dataMidPoint).attr("stroke", "black").attr("stroke-width", "1");
		svg.append("line").attr("x1", "200").attr("y1", dataMidPoint - dataHeight / 2).attr("x2", "200").attr("y2", dataMidPoint).attr("stroke", "black").attr("stroke-width", "1");
		*/
			
		// Draw axis
		const xAxis = d3.axisBottom().scale(xScale);
		axisArea.call(xAxis);
		
		// Draw outlier data points
		dataArea.selectAll("circle")
			.data(data)
			.enter()
			.append("circle")
			.filter(function(d) {
					return d[fieldName] <= lowerThreshold || d[fieldName] >= upperThreshold;
				})
			.attr("cx", function(d) {return xScale(d[fieldName]);})
			.attr("cy", "0")
			.attr("r", CIRCUMFERENCE)
			.classed("outlier", true);
		
		// Draw box
		let x = xScale(firstQuartile);
		let y = -BOX_HEIGHT / 2;
		let width = xScale(thirdQuartile) - x;
		dataArea.append("rect").attr("x", x).attr("y", y).attr("width", width).attr("height", BOX_HEIGHT)
			.classed("quartile-box", true);
		
		// Draw median line
		x = xScale(secondQuartile);
		let y2 = y + BOX_HEIGHT;
		dataArea.append("line").attr("x1", x).attr("y1", y).attr("x2", x).attr("y2", y2)
			.classed("boxplot-lines", true);
		
		// Draw whiskers
		x = xScale(lowerThreshold);
		dataArea.append("line").attr("x1", x).attr("y1", y).attr("x2", x).attr("y2", y2)
		.classed("boxplot-lines", true);
		
		let x2 = xScale(firstQuartile);
		dataArea.append("line").attr("x1", x).attr("y1", 0).attr("x2", x2).attr("y2", 0)
		.classed("boxplot-lines", true);
		
		x = xScale(thirdQuartile);
		x2 = xScale(upperThreshold);
		dataArea.append("line").attr("x1", x).attr("y1", 0).attr("x2", x2).attr("y2", 0)
		.classed("boxplot-lines", true);
		
		x = xScale(upperThreshold);
		dataArea.append("line").attr("x1", x).attr("y1", y).attr("x2", x).attr("y2", y2)
		.classed("boxplot-lines", true);
		
		
		
		/*
		
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
		
		// Draw draggable carets above threshold lines
		caretY = y1;
		caret1 = svg.append("polygon")
			.attr("points", computeCaretPoints(xLower))
			.attr("fill", "black")
			.on("mousedown", function() {
				isMovingLowerThreshold = true;
				document.getElementById("button_save").removeAttribute("disabled");
			});
		caret2 = svg.append("polygon")
			.attr("points", computeCaretPoints(xUpper))
			.attr("fill", "black")
			.on("mousedown", function() {
				isMovingUpperThreshold = true;
				document.getElementById("button_save").removeAttribute("disabled");
			});
		
		// Add selection event handler
		svg.on("mousedown", function() {
			console.log(isMovingLowerThreshold);
			if (dataAreSelected) {
				dataArea.selectAll(".outlier-selected").classed("outlier-selected", false);
				dataArea.selectAll(".inlier-selected").classed("inlier-selected", false);
				dataArea.selectAll(".data-selected").classed("data-selected", false);
				dataAreSelected = false;
				document.getElementById("button_show").setAttribute("disabled", "true");
				document.getElementById("button_query").setAttribute("disabled", "true");
				document.getElementById("button_change").setAttribute("disabled", "true");
				document.getElementById("button_suppress").setAttribute("disabled", "true");
			}
			if (! (isMovingLowerThreshold || isMovingUpperThreshold)) {
				isDragging = true;
				mouseDownX = d3.mouse(this)[0];
				mouseDownY = d3.mouse(this)[1];
				selectRect = svg.append("rect")
					.attr("x", mouseDownX)
					.attr("y", mouseDownY)
					.attr("width", 0)
					.attr("height", 0)
					.classed("select-rect", true);
			}
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
			else if (isMovingLowerThreshold) {
				caret1.attr("points", computeCaretPoints(x));
				line1.attr("x1", x);
				line1.attr("x2", x);
			}
			else if (isMovingUpperThreshold) {
				caret2.attr("points", computeCaretPoints(x));
				line2.attr("x1", x);
				line2.attr("x2", x);
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
				});
				if (dataAreSelected) {
					document.getElementById("button_show").removeAttribute("disabled");
					document.getElementById("button_query").removeAttribute("disabled");
					document.getElementById("button_change").removeAttribute("disabled");
					document.getElementById("button_suppress").removeAttribute("disabled");
				}
			}
			else if (isMovingLowerThreshold) {
				isMovingLowerThreshold = false;
			}
			else if (isMovingUpperThreshold) {
				isMovingUpperThreshold = false;
			}
		});
		*/
	});
}

let hmtl = "";

function onExport() {
	html = "<table class='wide'><tr><th>RecruitID</th><th>Event</th><th>"
		+ dataFieldName + "</th></tr>";
	dataArea.selectAll(".data-selected").each(function(d) {
		let row = "<tr><td>" + d.RecruitID + "</td><td>" + d.event + "</td><td>"
			+ d.value + "</td></tr>";
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
				recruitIds += d["RecruitID"];
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

const url = "/rest/anomaly/data/univariate?data_field_id=" + dataFieldId;
renderBoxplot(url, "value", lowerThreshold, upperThreshold, firstQuartile, secondQuartile, thirdQuartile);