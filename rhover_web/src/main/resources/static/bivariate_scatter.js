const margin = {top: 20, right: 20, bottom: 50, left: 50};
const RADIUS = 3;

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
		//const minX = d3.min(data, xValue);
		const minX = min(data, xLabel);
		//const maxX = d3.max(data, xValue);
		const maxX = max(data, xLabel);
		const xScale = d3.scaleLinear()
			.range([0, chartWidth])
			.domain([minX, maxX]);
		
		console.log("minX: " + minX + ", maxX: " + maxX);
		
		// Set up Y-axis scale
		//const minY = d3.min(data, yValue);
		const minY = min(data, yLabel);
		//const maxY = d3.max(data, yValue);
		const maxY = max(data, yLabel);
		const yScale = d3.scaleLinear()
			.range([chartHeight, 0])
			.domain([minY, maxY]);
		
		console.log("minY: " + minY + ", maxY: " + maxY);
		
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
		const canvas = svg.append("g");
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
		
		//canvas.append("text").attr("x", chartWidth).attr("y", 100).style("text-anchor", "end").text("Hello!");
		
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
	});
}



$(function() {
	renderBivariatePlot(url, "chart", 600, 600);
});