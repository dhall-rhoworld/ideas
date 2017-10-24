const CIRCUMFERENCE = 5;
		
const laneIndex = new Array(500);
const laneMax = new Array(500);
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
	console.log(x + ", " + y);
	return y;
}

function renderBeeswarm(dataUrl) {
	const viewport = d3.select("svg")
		.append("g")
		.attr("transform", "translate(40, 40)");
	
	const midPoint = viewport.append("g").attr("transform", "translate(0, 175)")
	
	d3.tsv(dataUrl, function(data) {
		console.log(data);
		
		laneMax[0] = d3.min(data, function(d) {return d["value"]}) - 100;
		
		const xScale = d3.scaleLinear()
		.domain([d3.min(data, function(d) {return d["value"]}), d3.max(data, function(d) {return d["value"]})])
		.range([0, 900]);
	
		midPoint.selectAll("circle")
			.data(data)
			.enter()
			.append("circle")
			.attr("cx", function(d) {return xScale(d["value"]);})
			.attr("cy", function(d) {return getY(xScale(d["value"]));})
			.attr("r", CIRCUMFERENCE)
			.style("fill", "purple");
		
		const xAxis = d3.axisBottom().scale(xScale);
		const xAxisGroup = viewport.append("g")
			.attr("transform", "translate(0, 300)")
			.call(xAxis);
	});
}