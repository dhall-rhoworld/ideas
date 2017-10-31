const CIRCUMFERENCE = 3;
const PADDING = 20;
const BORDER = 25;
const DATA_HEIGHT = 500;
const AXIS_HEIGHT = 25;
const SVG_HEIGHT = BORDER * 2 + PADDING + AXIS_HEIGHT + DATA_HEIGHT;
const SVG_WIDTH = 800;
const AXIS_WIDTH = 75;
const DATA_WIDTH = SVG_WIDTH - 2 * BORDER - AXIS_WIDTH;
const DATA_START_X = BORDER + AXIS_WIDTH;
const X_AXIS_START_Y = BORDER + DATA_HEIGHT + PADDING;

function renderScatterplot(dataUrl, fieldName1, fieldName2) {
	
	d3.csv(dataUrl, function(data) {
		const minX = d3.min(data, function(d) {return d[fieldName1]});
		const maxX = d3.max(data, function(d) {return d[fieldName1]});
		const minY = d3.min(data, function(d) {return d[fieldName2]});
		const maxY = d3.max(data, function(d) {return d[fieldName2]});
		
		const xScale = d3.scaleLinear().domain([minX, maxX]).range([0, DATA_WIDTH]);
		const yScale = d3.scaleLinear().domain([minY, maxY]).range([DATA_HEIGHT, 0]);
		
		const svg = d3.select("svg").attr("width", SVG_WIDTH).attr("height", SVG_HEIGHT);
		
		// Draw X-axis
		const xAxisArea = svg.append("g")
			.attr("transform", "translate(" + DATA_START_X + ", " + X_AXIS_START_Y + ")");
		const xAxis = d3.axisBottom().scale(xScale);
		xAxisArea.call(xAxis);
		
		// Draw data points
		const dataArea = svg.append("g")
		.attr("transform", "translate(" + DATA_START_X + ", " + BORDER + ")");
		
		dataArea.selectAll("circle")
			.data(data)
			.enter()
			.append("circle")
			.attr("cx", function(d) {return xScale(d[fieldName1])})
			.attr("cy", function(d) {return yScale(d[fieldName2])})
			.attr("r", CIRCUMFERENCE)
			.classed("inlier", true);
		
	})
}

const url = "/rest/anomaly/data/bivariate?bivariate_check_id=" + bivariateCheckId;
renderScatterplot(url, fieldName1.replace(/\/|\(|\)/g, "."), fieldName2.replace(/\/|\(|\)/g, "."));