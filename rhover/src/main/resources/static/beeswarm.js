const CIRCUMFERENCE = 3;
const AXIS_HEIGHT = 25;
const PADDING = 20;
const BORDER = 25;
const SVG_WIDTH = 800;
		
let laneIndex = new Array(500);
let laneMax = new Array(500);
let numLanes = 1;
laneIndex[0] = 0;

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
		console.log(data);
		
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
		const dataArea = svg.append("g")
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
			.style("stroke", function(d) {
				if (d["is_anomaly"] == 1) {
					return "red";
				}
				return "blue";
			})
			.style("stroke-width", "1")
			.style("fill", "none");
		
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
	});
}

const url = "/rest/anomaly/data?data_field_id=" + dataFieldId;
renderBeeswarm(url, "value", lowerThreshold, upperThreshold);