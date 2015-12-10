var ev = {};

ev.trackHeight = 40;
ev.boxSize = 14;
ev.fontSize = "10pt" 

ev.render = function(divId, dataUrl, width, anchor) {
    ev.anchor = anchor;
    
    // Define base geometric properties
    var margin = {
        top: 20,
        right: 30,
        bottom: 30,
        left: 40
    };
    var height;
    ev.chartWidth = width - margin.left - margin.right;
    var chartHeight;
    
    // Create SVG canvas
    var svg = d3.select(divId)
        .append("svg")
            .attr("width", width);
    
    // Create SVG group for the chart
    ev.chart = svg.append("g")
            .attr("transform", "translate(" + margin.left + "," +  margin.top + ")");
    
    // Create scales for X and Y coordinates
    var y = d3.scale.ordinal();
    
    // Retrieve data and finish drawing chart
    d3.json(dataUrl, function(error, data) {
        if (error) {
            console.log("Error: " + error);
        }
            
        // Set height-related properties
        height = ev.trackHeight * data.length + margin.top + margin.bottom;
        svg.attr("height", height);
        chartHeight = height - margin.top - margin.bottom;
        y.rangeRoundBands([0, chartHeight], 0.1);
            
        y.domain(data.map(function(data) {return data.subject;}));
            
        // Draw tracks
        var track = ev.chart.selectAll("g")
                .data(data)
            .enter().append("g")
                .attr("transform", function(data) {
                      return "translate(0," + y(data.subject) + ")";
                });
        track.append("line")
            .attr("x1", 0)
            .attr("y1", ev.trackHeight / 2)
            .attr("x2", ev.chartWidth)
            .attr("y2", ev.trackHeight / 2);
        track.append("text")
            .text(function(subjectData) {return subjectData.subject;})
            .attr("x", 0)
            .attr("y", ev.trackHeight / 2 - ev.boxSize / 2 - 2);
        track.selectAll("rect")
            .data(function(subjectData) {return subjectData.events;})
            .enter().append("rect")
            .attr("x", 0)
            .attr("y", ev.trackHeight / 2 - ev.boxSize / 2)
            .attr("height", ev.boxSize)
            .attr("width", ev.boxSize)
            .attr("class", function(data) {return data.type;});
            
        ev.data = data;
		ev.xAxis = d3.svg.axis().orient("bottom");
        ev.xAxisGroup = svg.append("g")
            .attr("class", "x axis")
            .attr("transform", "translate(" + margin.left + ", " + (chartHeight + ev.trackHeight / 2) + ")");
        
        layoutX();
    });
};

ev.setAnchor = function(anchor) {
    ev.anchor = anchor;
    layoutX();
};

function update() {
    var rects = d3.selectAll(("rect"))
        .transition()
        .duration(500);
    if (ev.anchor == "date") {
    	rects.attr("x", function(data) {return ev.x(parser.parse(data.date)) - ev.boxSize / 2;});
    }
    else {
    	rects.attr("x", function(data) {return ev.x(data.relativeDate) - ev.boxSize / 2;});
    }
};

function layoutX() {
	if (ev.anchor == "date") {
    	var minDate = d3.min(ev.data, function(d) {
        	return d3.min(d.events, function(event) {
            	return parser.parse(event.date);
        	});
    	});
    	var maxDate = d3.max(ev.data, function(d) {
        	return d3.max(d.events, function(event) {
            	return parser.parse(event.date);
        	});
    	});
    	ev.x = d3.time.scale()
        	.range([0, ev.chartWidth]);
    	ev.x.domain([minDate, maxDate]);
    	ev.xAxis.scale(ev.x);
    }
    else {
    	setRelativeDate();
    	var minDay = d3.min(ev.data, function(d) {
        	return d3.min(d.events, function(event) {
            	return event.relativeDate;
        	});
    	});
    	var maxDay = d3.max(ev.data, function(d) {
        	return d3.max(d.events, function(event) {
            	return event.relativeDate;
        	});
    	});
    	ev.x = d3.scale.linear().range([0, ev.chartWidth]);
    	ev.x.domain([minDay, maxDay]);
    	ev.xAxis.scale(ev.x);
    }
    ev.xAxisGroup.call(ev.xAxis);
    update();
}

function setRelativeDate() {
	var msecInDay = 1000 * 60 * 60 * 24;
	for (var i = 0; i < ev.data.length; i++) {
		var subjectRec = ev.data[i];
		var refDate = null;
		for (var j = 0; j < subjectRec.events.length; j++) {
			var event = subjectRec.events[j];
			if (event.type == ev.anchor) {
				refDate = parser.parse(event.date);
				break;
			}
		}
		for (var j = 0; j < subjectRec.events.length; j++) {
			var event = subjectRec.events[j];
			var eventDate = parser.parse(event.date);
			event.relativeDate = Math.round((eventDate.getTime() - refDate.getTime()) / msecInDay);
		}
	}
}

var parser = d3.time.format("%m/%d/%Y");

