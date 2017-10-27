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
	//console.log(x + ", " + y);
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

function renderBeeswarm(dataUrl, fieldName, lowerThresh, upperThresh) {
	
	d3.csv(dataUrl, function(data) {
		//console.log(data);
		
		// Set extent of data and chart areas on the screen
		const min = data[0][fieldName];
		const max = data[data.length - 1][fieldName];
		const dataAreaWidth = SVG_WIDTH - 2 * BORDER;
		const xScale = d3.scaleLinear()
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
		
		// Draw data points
		dataArea.selectAll("circle")
			.data(data)
			.enter()
			.append("circle")
			.attr("cx", function(d) {return xScale(d[fieldName]);})
			.attr("cy", function(d) {return getY(xScale(d[fieldName]));})
			.attr("r", CIRCUMFERENCE)
			.classed("outlier", function(d) {
				return d["is_anomaly"] == 1 && (d["value"] < lowerThresh || d["value"] > upperThresh);
			})
			.classed("inlier", function(d) {
				return d["is_anomaly"] == 0 || (d["value"] >= lowerThresh && d["value"] <= upperThresh)
			});
		
		// Draw threshold lines
		let xLower = xScale(lowerThresh) + BORDER;
		let xUpper = xScale(upperThresh) + BORDER;
		let y1 = BORDER;
		let y2 = BORDER + dataHeight;
		svg.append("line")
			.attr("x1", xLower).attr("y1", y1).attr("x2", xLower).attr("y2", y2)
			.attr("stroke", "black").attr("stroke-width", 1).attr("stroke-dasharray", "5, 5");
		svg.append("line")
			.attr("x1", xUpper).attr("y1", y1).attr("x2", xUpper).attr("y2", y2)
			.attr("stroke", "black").attr("stroke-width", 1).attr("stroke-dasharray", "5, 5");
		
		// Add selection event handler
		svg.on("mousedown", function() {
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
			if (isDragging) {
				let x = d3.mouse(this)[0];
				let y = d3.mouse(this)[1];
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
			isDragging = false;
			mouseUpX = d3.mouse(this)[0];
			mouseUpY = d3.mouse(this)[1];
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
		});
	});
}

let hmtl = "";

function onExport() {
	html = "<table class='wide'><tr><th>RecruitID</th><th>Event</th><th>"
		+ dataFieldName + "</th></tr>";
	dataArea.selectAll(".data-selected").each(function(d) {
		console.log(dataFieldName + ", " + d.RecruitID + ", " + d.event + ", " + d.value);
		let row = "<tr><td>" + d.RecruitID + "</td><td>" + d.event + "</td><td>"
			+ d.value + "</td></tr>";
		html += row;
	});
	hmtl += "</table>"
	$("#dialog_data").html(html);
	$("#dialog_data").dialog("open");
}

function onChange() {
	
}

const url = "/rest/anomaly/data?data_field_id=" + dataFieldId;
renderBeeswarm(url, "value", lowerThreshold, upperThreshold);