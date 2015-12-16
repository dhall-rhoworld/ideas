var ev = {

	// Attributes
	boxSize: 14,
	margin: {top: 20, right: 30, bottom: 30, left: 60},
	padding: 5,
	xAxisHeight: 40,
	boxPadding: 3,
	legendHeight: 45,
	legendColWidth: 80,
	
	// Private attributes
	_anchor: "date",
	_chartWidth: 0,
	_data: {},
	_x: {},
	_xAxis: {},
	_xAxisGroup: {},
	_parser: d3.time.format("%m/%d/%Y"),
	_trackY: [],
	
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
		var chart = svg.append("g");
		
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
			var chartHeight = 0;
			for (var i = 0; i < data.length; i++) {
				var trackHeight = ev._trackHeight(data[i]);
				ev._trackY[i] = chartHeight + trackHeight / 2;
				chartHeight += trackHeight;
				if (i < data.length - 1) {
					chartHeight += ev.padding;
				}
			}
			var height = chartHeight + ev.margin.top + ev.margin.bottom + ev.xAxisHeight + ev.legendHeight;
			svg.attr("height", height);
			
			// Add vertically-stacked data "tracks" for subjects
			ev._chartWidth = width - ev.margin.left - ev.margin.right;
			var track = chart.selectAll("g")
					.data(data)
				.enter().append("g")
					.attr("transform", function(subject, i) {
						return "translate(" + ev.margin.left + ", " + ev._trackY[i] + ")";
					});
			track.append("line")
				.attr("x1", 0)
				.attr("y1", 0)
				.attr("x2", ev._chartWidth)
				.attr("y2", 0);
			track.append("text")
				.text(function(subjectData) {return subjectData.subject_id;})
				.attr("x", -ev.boxSize)
				.attr("y", 5)
				.attr("class", "subject-label");
			
			// Add data "points" for each visit to the tracks
			var visit = track.selectAll("g")
				.data(function(subjectData) {return subjectData.visits;})
				.enter().append("g")
				.attr("transform", function(visit) {return "translate(0, " + (-ev.boxSize * visit.specimens.length / 2) + ")"})
				.attr("class", "visit");
			visit.selectAll("rect")
				.data(function(visit) {return visit.specimens;})
				.enter().append("rect")
				.attr("x", 0)
				.attr("y", function(specimen, i) {return i * (ev.boxSize + ev.boxPadding);})
				.attr("height", ev.boxSize)
				.attr("width", ev.boxSize)
				.on("click", function() {ev._toggleSelected(this);})
				.attr("class", function(specimen) {return specimen.type;});
			
			// Add x-axis
			ev._xAxis = d3.svg.axis().orient("bottom");
			ev._xAxisGroup = svg.append("g")
				.attr("class", "x axis")
				.attr("transform", "translate(" + ev.margin.left + ", " + (chartHeight + ev.xAxisHeight / 2) + ")");
			svg.append("text")
				.attr("class", "axis-label")
				.attr("id", "axis-label")
				.attr("x", (ev.margin.left + ev._chartWidth / 2))
				.attr("y", (ev.margin.top + chartHeight + ev.xAxisHeight))
				.attr("text-anchor", "middle");
				
			// Add legend
			specimenTypesDict = [];
			for (var i = 0; i < data.length; i++) {
				var subject = data[i];
				for (var j = 0; j < subject.visits.length; j++) {
					var visit = subject.visits[j];
					for (var k = 0; k < visit.specimens.length; k++) {
						var specimen = visit.specimens[k];
						specimenTypesDict[specimen.type] = "";
					}
				}
			}
			var specimenTypes = Object.keys(specimenTypesDict);
			var legendItems = svg.append("g")
				.attr("transform", "translate(" + (ev.margin.left + 150) + ", " + (chartHeight + ev.xAxisHeight + ev.legendHeight) + ")")
				.selectAll("g")
				.data(specimenTypes)
				.enter()
				.append("g")
				.attr("transform", function(d, i) {return "translate(" + (i * (ev.boxSize + ev.legendColWidth)) + ", 0)";});
			legendItems.append("rect")
				.attr("class", function(d) {return d;})
				.attr("x", 0)
				.attr("y", 0)
				.attr("width", ev.boxSize)
				.attr("height", ev.boxSize);
			legendItems.append("text")
				.text(function(d) {return d;})
				.attr("class", "legend")
				.attr("x", ev.boxSize + 5)
				.attr("y", 11);
		
			// Layout data along the x-axis
			ev._layoutX();
		});
	},
	
	/*
	 * Toggle whether a given specimen data point is selected.
	 */
	_toggleSelected: function(element) {
		var specimen = d3.select(element).datum();
		var selected = specimen.selected;
		if (selected === undefined) {
			selected = true;
		}
		else {
			selected = !selected;
		}
		specimen.selected = selected;
		var style = element.getAttribute("class");
		var i = style.search("-selected");
		if (i < 0) {
			style = style + "-selected";
		}
		else {
			style = style.substring(0, i);
		}
		element.setAttribute("class", style);
	},
	
	/*
	 * Computes how many pixels required to create a track for the given subject.
	 */
	_trackHeight: function(subject) {
		var maxStackedPoints = 1;
		for (var i = 0; i < subject.visits.length; i++) {
			var visit = subject.visits[i];
			if (visit.specimens.length > maxStackedPoints) {
				maxStackedPoints = visit.specimens.length;
			}
		}
		return maxStackedPoints * ev.boxSize + ev.padding * 2;
	},
	
	/*
	 * Update x-coordinate of data points.
	 */
	_update: function() {
		var visits = d3.selectAll((".visit"))
			.transition()
			.duration(500);
		if (this._anchor == "date") {
			visits.attr("transform", function(data) {
				var x = ev._x(ev._parser.parse(data.date)) - ev.boxSize / 2;
				var y = -(ev.boxSize * data.specimens.length + ev.boxPadding * (data.specimens.length - 1)) / 2;
				return "translate(" + x + ", " + y + ")";
			});
		}
		else {
			visits.attr("transform", function(data) {
				var x = ev._x(data.daysSinceAnchorVisit) - ev.boxSize / 2;
				var y = -(ev.boxSize * data.specimens.length + ev.boxPadding * (data.specimens.length - 1)) / 2;
				return "translate(" + x + ", " + y + ")";
			});
		}
	},

	/*
	 * Lay out x-range and x-axis.
	 */
	_layoutX: function() {
		var axisLabel = ""
		if (this._anchor == "date") {
			axisLabel = "Date"
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
			axisLabel = "Day relative to " + this._anchor;
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
		d3.select("#axis-label").text(axisLabel);
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
