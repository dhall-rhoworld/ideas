var ev = {

	// Attributes
	trackHeight: 40,
	boxSize: 14,
	margin: {top: 20, right: 30, bottom: 30, left: 40},
	
	// Private attributes
	_anchor: "date",
	_chartWidth: 0,
	_data: {},
	
	/**
	 * Set the event type that will anchor the timeline.
	 * @param {string} eventType - An event type. 
	 */
	setAnchor: function(eventType) {
		ev.anchor = eventType;
		layoutX();
	}
};

ev.render = function(divId, dataUrl, width, anchor) {
    ev.anchor = anchor;
    
    // Create SVG canvas
    var svg = d3.select(divId)
        .append("svg")
            .attr("width", width);
    
    // Create SVG group for the chart
    ev.chart = svg.append("g")
            .attr("transform", "translate(" + ev.margin.left + "," +  ev.margin.top + ")");
        
    // Retrieve data and lay out chart
    d3.json(dataUrl, function(error, data) {
    
    	// Handle error
        if (error) {
            console.log("Error: " + error);
            ev.chart.append("text")
            	.text(error)
            	.attr("class", "errorMsg");
            return;
        }
        
        ev._data = data;
            
        // Initialize height and y-axis variables and attributes
        var height = ev.trackHeight * data.length + ev.margin.top + ev.margin.bottom;
        var chartHeight = height - ev.margin.top - ev.margin.bottom;
        svg.attr("height", height);
        var y = d3.scale.ordinal()
        	.rangeRoundBands([0, chartHeight], 0.1)
    		.domain(data.map(function(data) {return data.subject;}));
            
        // Add vertically-stacked data "tracks" for subjects
        ev._chartWidth = width - ev.margin.left - ev.margin.right;
        var track = ev.chart.selectAll("g")
                .data(data)
            .enter().append("g")
                .attr("transform", function(data) {
                      return "translate(0," + y(data.subject) + ")";
                });
        track.append("line")
            .attr("x1", 0)
            .attr("y1", ev.trackHeight / 2)
            .attr("x2", ev._chartWidth)
            .attr("y2", ev.trackHeight / 2);
        track.append("text")
            .text(function(subjectData) {return subjectData.subject;})
            .attr("x", 0)
            .attr("y", ev.trackHeight / 2 - ev.boxSize / 2 - 2);
            
        // Add data "points" for each event to the tracks
        track.selectAll("rect")
            .data(function(subjectData) {return subjectData.events;})
            .enter().append("rect")
            .attr("x", 0)
            .attr("y", ev.trackHeight / 2 - ev.boxSize / 2)
            .attr("height", ev.boxSize)
            .attr("width", ev.boxSize)
            .attr("class", function(event) {return event.type;});
            
        // Add x-axis
		ev.xAxis = d3.svg.axis().orient("bottom");
        ev.xAxisGroup = svg.append("g")
            .attr("class", "x axis")
            .attr("transform", "translate(" + ev.margin.left + ", " + (chartHeight + ev.trackHeight / 2) + ")");
        
        // Layout data along the x-axis
        layoutX();
    });
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
    	var minDate = d3.min(ev._data, function(d) {
        	return d3.min(d.events, function(event) {
            	return parser.parse(event.date);
        	});
    	});
    	var maxDate = d3.max(ev._data, function(d) {
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
    	var minDay = d3.min(ev._data, function(d) {
        	return d3.min(d.events, function(event) {
            	return event.relativeDate;
        	});
    	});
    	var maxDay = d3.max(ev._data, function(d) {
        	return d3.max(d.events, function(event) {
            	return event.relativeDate;
        	});
    	});
    	ev.x = d3.scale.linear().range([0, ev._chartWidth]);
    	ev.x.domain([minDay, maxDay]);
    	ev.xAxis.scale(ev.x);
    }
    ev.xAxisGroup.call(ev.xAxis);
    update();
}

function setRelativeDate() {
	var msecInDay = 1000 * 60 * 60 * 24;
	for (var i = 0; i < ev._data.length; i++) {
		var subjectRec = ev._data[i];
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

