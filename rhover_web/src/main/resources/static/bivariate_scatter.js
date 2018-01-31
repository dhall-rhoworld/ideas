const margin = {top: 20, right: 20, bottom: 50, left: 50};
const RADIUS = 3;

let minX = 0;
let minY = 0;
let maxX = 0;
let maxY = 0;

let xScale = null;
let yScale = null;

let canvas = null;

function min(data, colName) {
	if (data.length == 0) {
		return 0;
	}
	let min = parseFloat(data[0][colName]);
	for (let i = 1; i < data.length; i++) {
		let temp = parseFloat(data[i][colName]);
		if (temp < min) {
			min = temp;
		}
	}
	return min;
}

function max(data, colName) {
	if (data.length == 0) {
		return 0;
	}
	let max = parseFloat(data[0][colName]);
	for (let i = 1; i < data.length; i++) {
		let temp = parseFloat(data[i][colName]);
		if (temp > max) {
			max = temp;
		}
	}
	return max;
}

function renderBivariatePlot(url, divId, width, height) {
	
	const chartWidth = width - margin.left - margin.right;
	const chartHeight = height - margin.top - margin.bottom;
	
	d3.csv(url, function(error, data) {
		console.log(data);
		
		// Extract X and Y axis labels
		const xLabel = data.columns[4];
		const yLabel = data.columns[5];
		
		// Create functions to extract X and Y values
		const xValue = function(record) {
			return record[xLabel];
		};
		const yValue = function(record) {
			return record[yLabel];
		};
		
		// Set up X-axis scale
		minX = min(data, xLabel);
		maxX = max(data, xLabel);
		xScale = d3.scaleLinear()
			.range([0, chartWidth])
			.domain([minX, maxX]);
		
		// Set up Y-axis scale
		minY = min(data, yLabel);
		maxY = max(data, yLabel);
		yScale = d3.scaleLinear()
			.range([chartHeight, 0])
			.domain([minY, maxY]);
		
		// Create functions to map X and Y coordinates to chart
		const mapX = function(record) {
			return xScale(xValue(record));
		}
		const mapY = function(record) {
			return yScale(yValue(record));
		}
		
		// Create SVG element
		const svg = d3.select("#" + divId).append("svg");
		svg.attr("width", width);
		svg.attr("height", height);
		
		// Create canvas area within SVG
		canvas = svg.append("g");
		canvas.attr("transform", "translate(" + margin.left + ", " + margin.top + ")");
		
		// Draw data points
		canvas.selectAll(".data-point")
				.data(data)
			.enter().append("circle")
				.attr("class", "data-point")
				.attr("r", RADIUS)
				.attr("cx", mapX)
				.attr("cy", mapY)
				.classed("outlier", function(d) {
					return d["anomaly_id"] > 0;
				})
				.classed("inlier", function(d) {
					return d["anomaly_id"] == 0;
				});
		
		// Draw X-axis
		const xAxis = d3.axisBottom().scale(xScale);
		canvas.append("g")
			.attr("transform", "translate(0, " + chartHeight + ")")
			.call(xAxis)
		canvas.append("text")
			.attr("x", chartWidth / 2)
			.attr("y", chartHeight + 40)
			.style("text-anchor", "middle")
			.text(xLabel);
		
		// Draw Y-axis
		const yAxis = d3.axisLeft().scale(yScale);
		canvas.append("g")
			.attr("class", "y-axis")
			.call(yAxis);
		canvas.append("text")
			.attr("transform", "rotate(-90)")
			.attr("x", -chartHeight / 2)
			.attr("y", -40)
			.style("text-anchor", "end")
			.text(yLabel);
		
		drawNormalResidualThresholds();

	});
}

function computeX(y, yIntercept) {
	return (y - yIntercept) / slope;
}

function computeY(x, yIntercept) {
	return yIntercept + slope * x;
}

function drawLine(yIntercept) {
	const coords = {};
	
	// Compute coordinates of point where line enters plotting area
	let tempY = computeY(minX, yIntercept);
	if (tempY >= minY && tempY <= maxY) {
		coords.x1 = minX;
		coords.y1 = tempY;
	}
	else if (tempY > maxY && slope < 0){
		coords.x1 = computeX(maxY, yIntercept);
		coords.y1 = maxY;
	}
	else if (tempY < minY && slope > 0) {
		coords.x1 = computeX(minY, yIntercept);
		coords.y1 = minY;
	}
	
	// Compute coordinates of point where line exits plotting area
	tempY = computeY(maxX, yIntercept);
	if (tempY >= minY && tempY <= maxY) {
		coords.x2 = maxX;
		coords.y2 = tempY;
	}
	else if (tempY > maxY && slope > 0) {
		coords.x2 = computeX(maxY, yIntercept);
		coords.y2 = maxY;
	}
	else if (tempY < minY && slope < 0) {
		coords.x2 = computeX(minY, yIntercept);
		coords.y2 = minY;
	}
	
	// Draw line
	if ("x1" in coords && "y1" in coords && "x2" in coords && "y2" in coords) {
		canvas.append("line")
		.attr("x1", xScale(coords.x1))
		.attr("y1", yScale(coords.y1))
		.attr("x2", xScale(coords.x2))
		.attr("y2", yScale(coords.y2))
		.style("stroke-width", 1)
		.style("stroke", "black");
	}
}

function drawNormalResidualThresholds() {
	drawLine(intercept - cutoffResidual);
	drawLine(intercept + cutoffResidual);
}

$(function() {
	renderBivariatePlot(url, "chart", 600, 600);
});