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
    var chartWidth = width - margin.left - margin.right;
    var chartHeight;
    
    // Create SVG canvas
    var svg = d3.select(divId)
        .append("svg")
            .attr("width", width);
    
    // Create SVG group for the chart
    ev.chart = svg.append("g")
            .attr("transform", "translate(" + margin.left + "," +  margin.top + ")");
    
    // Create scales for X and Y coordinates
    ev.x = d3.time.scale()
        .range([0, chartWidth]);
    var y = d3.scale.ordinal();
    
    // X-axis
    var xAxis = d3.svg.axis().orient("bottom").scale(ev.x);
    
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
            
        // Set scale domains
        var minDate = d3.min(data, function(data) {
            return d3.min(data.events, function(data) {
                return parser.parse(data.date);
            });
        });
        var maxDate = d3.max(data, function(data) {
            return d3.max(data.events, function(data) {
                return parser.parse(data.date);
            });
        });
        ev.x.domain([minDate, maxDate]);
        y.domain(data.map(function(data) {return data.subject;}));
            
        // X-axis
        svg.append("g")
            .attr("class", "x axis")
            .attr("transform", "translate(" + margin.left + ", " + (chartHeight + ev.trackHeight / 2) + ")")
            .call(xAxis);
            
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
            .attr("x2", chartWidth)
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
        layoutX();
    });
};

ev.setAnchor = function(anchor) {
    console.log(anchor);
};

function update() {
    d3.selectAll(("rect"))
        .transition()
        .duration(500)
    .attr("x", function(data) {return ev.x(parser.parse(data.date));});
};

function layoutX() {
    update();
}

var parser = d3.time.format("%m/%d/%Y");

