var ev = {

	// Attributes
	trackHeight: 40,
	boxSize: 14,
	margin: {top: 20, right: 30, bottom: 30, left: 40},
	
	// Private attributes
	_anchor: "date",
	_chartWidth: 0,
	_data: {},
	_x: {},
	_xAxis: {},
	_xAxisGroup: {},
	_parser: d3.time.format("%m/%d/%Y"),
	
	/**
	 * Set the visit type that will anchor the timeline.
	 * @param {string} visitType - A visit type. 
	 */
	setAnchor: function(visitType) {
		this._anchor = visitType;
		this._layoutX();
	},

	/**
	 * Render an event viewer.
	 * @param {string} divId - ID of DIV element to contain viewer.
	 * @param {string} dataUrl - URL to fetch data.
	 * @param {int} width - Width of viewer in pixels.
	 * @param {string} anchor - The string "date" or a visit type to anchor timeline.
	 */
	render: function(divId, dataUrl, width, anchor) {
		this._anchor = anchor;
	
		// Create SVG canvas
		var svg = d3.select(divId)
			.append("svg")
				.attr("width", width);
	
		// Create SVG group for the chart
		var chart = svg.append("g")
				.attr("transform", "translate(" + this.margin.left + "," +  this.margin.top + ")");
		
		// Retrieve data and lay out chart
		d3.json(dataUrl, function(error, data) {
	
			// Handle error
			if (error) {
				console.log("Error: " + error);
				chart.append("text")
					.text(error)
					.attr("class", "errorMsg");
				return;
			}
			
			// Save data for later
			ev._data = data;
			
			// Initialize height and y-axis variables and attributes
			var height = ev.trackHeight * data.length + ev.margin.top + ev.margin.bottom;
			var chartHeight = height - ev.margin.top - ev.margin.bottom;
			svg.attr("height", height);
			var y = d3.scale.ordinal()
				.rangeRoundBands([0, chartHeight], 0.1)
				.domain(data.map(function(data) {
					return data.subject_id;
				}));
			
			// Add vertically-stacked data "tracks" for subjects
			ev._chartWidth = width - ev.margin.left - ev.margin.right;
			var track = chart.selectAll("g")
					.data(data)
				.enter().append("g")
					.attr("transform", function(data) {
						  return "translate(0," + y(data.subject_id) + ")";
					});
			track.append("line")
				.attr("x1", 0)
				.attr("y1", ev.trackHeight / 2)
				.attr("x2", ev._chartWidth)
				.attr("y2", ev.trackHeight / 2);
			track.append("text")
				.text(function(subjectData) {return subjectData.subject_id;})
				.attr("x", 0)
				.attr("y", ev.trackHeight / 2 - ev.boxSize / 2 - 2);
			
			// Add data "points" for each visit to the tracks
			var visit = track.selectAll("g")
				.data(function(subjectData) {return subjectData.visits;})
				.enter().append("g")
				.attr("transform", "translate(0, " + (ev.trackHeight / 2 - ev.boxSize / 2) + ")")
				.attr("class", "visit");
			visit.selectAll("rect")
				.data(function(visit) {return visit.specimens;})
				.enter().append("rect")
				.attr("x", 0)
				.attr("y", function(specimen, i) {return i * ev.boxSize;})
				.attr("height", ev.boxSize)
				.attr("width", ev.boxSize)
				.attr("class", function(specimen) {return specimen.type;});
			
			// Add x-axis
			ev._xAxis = d3.svg.axis().orient("bottom");
			ev._xAxisGroup = svg.append("g")
				.attr("class", "x axis")
				.attr("transform", "translate(" + ev.margin.left + ", " + (chartHeight + ev.trackHeight / 2) + ")");
		
			// Layout data along the x-axis
			ev._layoutX();
		});
	},
	
	/*
	 * Update x-coordinate of data points.
	 */
	_update: function() {
		var rects = d3.selectAll((".visit"))
			.transition()
			.duration(500);
		if (this._anchor == "date") {
			rects.attr("transform", function(data) {
				var x = ev._x(ev._parser.parse(data.date)) - ev.boxSize / 2;
				var y = ev.trackHeight / 2 - ev.boxSize * data.specimens.length / 2;
				return "translate(" + x + ", " + y + ")";
			});
		}
		else {
			rects.attr("transform", function(data) {
				var x = ev._x(data.daysSinceAnchorVisit) - ev.boxSize / 2;
				var y = ev.trackHeight / 2 - ev.boxSize * data.specimens.length / 2;
				return "translate(" + x + ", " + y + ")";
			});
		}
	},

	/*
	 * Lay out x-range and x-axis.
	 */
	_layoutX: function() {
		if (this._anchor == "date") {
			var minDate = d3.min(this._data, function(d) {
				return d3.min(d.visits, function(visit) {
					return ev._parser.parse(visit.date);
				});
			});
			var maxDate = d3.max(this._data, function(d) {
				return d3.max(d.visits, function(visit) {
					return ev._parser.parse(visit.date);
				});
			});
			this._x = d3.time.scale()
				.range([0, this._chartWidth])
				.domain([minDate, maxDate]);
		}
		else {
			this._setDaysSinceAnchorVisit();
			var minDay = d3.min(this._data, function(d) {
				return d3.min(d.visits, function(visit) {
					return visit.daysSinceAnchorVisit;
				});
			});
			var maxDay = d3.max(this._data, function(d) {
				return d3.max(d.visits, function(visit) {
					return visit.daysSinceAnchorVisit;
				});
			});
			this._x = d3.scale.linear()
				.range([0, this._chartWidth])
				.domain([minDay, maxDay]);
		}
		this._xAxis.scale(this._x);
		this._xAxisGroup.call(this._xAxis);
		this._update();
	},

	/*
	 * Set time in days since/till the anchor visit.
	 */
	_setDaysSinceAnchorVisit: function() {
		var msecInDay = 1000 * 60 * 60 * 24;
		for (var i = 0; i < this._data.length; i++) {
			var subjectRec = this._data[i];
			var refDate = null;
			for (var j = 0; j < subjectRec.visits.length; j++) {
				var visit = subjectRec.visits[j];
				if (visit.type == this._anchor) {
					refDate = this._parser.parse(visit.date);
					break;
				}
			}
			for (var j = 0; j < subjectRec.visits.length; j++) {
				var visit = subjectRec.visits[j];
				var visitDate = this._parser.parse(visit.date);
				visit.daysSinceAnchorVisit = Math.round((visitDate.getTime() - refDate.getTime()) / msecInDay);
			}
		}
	}
};
