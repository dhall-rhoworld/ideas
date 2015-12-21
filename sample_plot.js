var sp = {

	// Attributes
	margin: {top: 20, right: 20, bottom: 20, left: 20},
	border: 5,
	size: {dataPoint: 10, selectBox: 10},
	padding: {track: 5, selectBox: 3, dataPoint: 3},
	section: {
		xAxis: {height: 40},
		legend: {height: 55, colWidth: 80}
	},
	
	// Private attributes
	_anchor: "date",
	_chartWidth: 0,
	_chartHeight: 0,
	_plotWidth: 0,
	_trackY: [],
	_trackHeight: [],
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
		sp._anchor = visitType;
		sp._layoutX();
	},
	
	activateTimeIntervalSelect: function(activate) {
		d3.select("svg").append("line")
			.attr("class", "time-interval-line")
			.attr("x1", 100)
			.attr("y1", 0)
			.attr("x2", 100)
			.attr("y2", sp.margin.top + sp._chartHeight);
		sp._selectTimeInterval = function() {
			return;
		}
	},
	
	/**
	 * Get selected specimens.
	 */
	getSelections: function() {
		var selections = [];
		for (var i = 0; i < sp._data.length; i++) {
			var subject = sp._data[i];
			for (var j = 0; j < subject.visits.length; j++) {
				var visit = subject.visits[j];
				for (var k = 0; k < visit.specimens.length; k++) {
					var specimen = visit.specimens[k];
					if (specimen.selected) {
						selections.push(specimen.id);
					}
				}
			}
		}
		return selections;
	},

	/**
	 * Render an event viewer.
	 * @param {string} divId - ID of DIV element to contain viewer.
	 * @param {string} dataUrl - URL to fetch data.
	 * @param {int} width - Width of viewer in pixels.
	 * @param {string} anchor - The string "date" or a visit type to anchor timeline.
	 */
	render: function(divId, dataUrl, width, anchor) {
		sp._anchor = anchor;
		
		// Retrieve data and lay out chart
		d3.json(dataUrl, function(error, data) {
	
			// Handle error
			if (error) {
				sp._showErrorMessage(divId, error);
				return;
			}
			
			// Save data for later
			sp._data = data;
			
			// Create SVG canvas
			var svg = d3.select(divId)
				.append("svg")
					.attr("width", width);
					
			// Compute bounding boxes so text can be laid out
			var visitLabelBBox = sp._getBBox("T", "visit-label");
			var visitLabelWidth = visitLabelBBox.width;
			var visitLabelHeight = visitLabelBBox.height;
			var trackLabelWidth = 0;
			var trackLabelHeight = 0;
			for (var i = 0; i < data.length; i++) {
				var bbox = sp._getBBox(data[i].subject_id, "subject-label");
				if (bbox.width > trackLabelWidth) {
					trackLabelWidth = bbox.width;
				}
				if (bbox.height > trackLabelHeight) {
					trackLabelHeight = bbox.height;
				}
			}
			
			// Compute height and width of track label containers
			var specimenTypes = sp._uniqueSpecimenTypes(data);
			var labelContainerHeight = visitLabelHeight + sp.size.selectBox
				+ sp.padding.track * 2 + sp.padding.selectBox;
			var labelContainerWidth = specimenTypes.length * sp.size.selectBox +
				(specimenTypes.length - 1) * sp.padding.selectBox;
			if (visitLabelWidth > labelContainerWidth) {
				labelContainerWidth = visitLabelWidth;
			}
	
			// Create overall container for chart
			var chart = svg.append("g")
				.attr("id", "chart-container")
				.attr("transform", "translate(" + (sp.margin.left + sp.border) + ", "
					+ (sp.margin.top + sp.border) + ")");
				
			// Compute dimensions of plotting section as well as track origins
			sp._plotWidth = width - sp.margin.left - sp.margin.right - 2 * sp.border;
			sp._plotHeight = 0;
			for (var i = 0; i < data.length; i++) {
				if (i > 0) {
					sp._plotHeight += sp.border;
				}
				sp._trackY.push(sp._plotHeight);
				var height = sp._computeTrackHeight(data[i], visitLabelHeight, labelContainerHeight);
				sp._trackHeight.push(height);
				sp._plotHeight += height;
			}
			
			// Add container for plotting section
			var plot = chart.append("g")
				.attr("id", "plot-container")
				.attr("transform", "translate(0, 0)");
			
			// Add background to plotting section
			plot.append("rect")
				.attr("class", "plot-bg")
				.attr("x", 0)
				.attr("y", 0)
				.attr("width", sp._plotWidth)
				.attr("height", sp._plotHeight);
				
			// Add track containers
			var tracks = plot.selectAll("g.track-container")
				.data(data)
				.enter()
				.append("g")
				.attr("id", function(subject) {return "track-" + subject.subject_id;})
				.attr("class", "track-container")
				.attr("transform", function(subject, i) {
					return "translate(0, " + sp._trackY[i] + ")";
				});
				
			// Draw track backgrounds
			tracks.append("rect")
				.attr("class", "track-bg")
				.attr("x", 0)
				.attr("y", 0)
				.attr("width", sp._plotWidth)
				.attr("height", function(subject, i) {return sp._trackHeight[i];});
				
			// Add track labels and multi-selects
			var labelContainers = tracks.append("g")
				.attr("class", "label-container")
				.attr("transform", function(visit, i) {
					return "translate(" + sp.padding.track + ","
						+ (sp._trackHeight[i] / 2 - labelContainerHeight / 2) + ")";
				});
			labelContainers.append("text")
				.text(function(subject) {return subject.subject_id;})
				.attr("class", "subject-label")
				.attr("y", trackLabelHeight);
				/*
			var multiSelectContainers = labelContainers.append("g")
				.attr("class", "multi-select-containers")
				.attr("transform", "translate(0, 0");  // Will update after text height and width computed
			multiSelectContainers.selectAll("rect")
				.data(specimenTypes)
				.enter()
				.append("rect")
				.attr("*/
			
			
			// Initialize height and y-axis variables and attributes
			for (var i = 0; i < data.length; i++) {
				var trackHeight = sp._computeTrackHeight(data[i], visitLabelHeight, labelContainerHeight);
				sp._trackY[i] = sp._chartHeight + trackHeight / 2;
				sp._chartHeight += trackHeight;
				if (i < data.length - 1) {
					sp._chartHeight += sp.padding.track;
				}
			}
			var height = sp._chartHeight + sp.margin.top + sp.margin.bottom + sp.section.xAxis.height + sp.section.legend.height;
			svg.attr("height", height);
			
			// *** Remove this ***
			svg.append("rect")
				.attr("x", 0)
				.attr("y", 0)
				.attr("width", sp.margin.left)
				.attr("height", height)
				.attr("class", "section-background");
			svg.append("rect")
				.attr("x", 0)
				.attr("y", 0)
				.attr("width", width)
				.attr("height", sp.margin.top)
				.attr("class", "section-background");
			svg.append("rect")
				.attr("x", width - sp.margin.right)
				.attr("y", 0)
				.attr("width", sp.margin.right)
				.attr("height", height)
				.attr("class", "section-background");
			svg.append("rect")
				.attr("x", 0)
				.attr("y", height - sp.margin.bottom)
				.attr("width", width)
				.attr("height", sp.margin.bottom)
				.attr("class", "section-background");
			// *** Remove above ***
			
			// Add vertically-stacked data "tracks" for subjects
			sp._chartWidth = width - sp.margin.left - sp.margin.right;
			var track = chart.selectAll("g")
					.data(data)
				.enter().append("g")
					.attr("transform", function(subject, i) {
						return "translate(" + sp.margin.left + ", " + sp._trackY[i] + ")";
					})
					.attr("id", function(subject) {return "track-" + subject.subject_id;});
			track.append("line")
				.attr("x1", 0)
				.attr("y1", 0)
				.attr("x2", sp._chartWidth)
				.attr("y2", 0);
			track.append("text")
				.text(function(subjectData) {return subjectData.subject_id;})
				.attr("x", -sp.size.dataPoint)
				.attr("y", 5)
				.attr("class", "subject-label");
				
			// Add select-all boxes for each specimen type and for all specimen types
			track.append("rect")
				.attr("x", ((-specimenTypes.length - 1) * (sp.size.selectBox + sp.padding.selectBox) - sp.size.dataPoint / 2))
				.attr("y", 10)
				.attr("width", sp.size.selectBox)
				.attr("height", sp.size.selectBox)
				.on("click", function() {sp._toggleTrack(this);})
				.attr("class", "all-specimen-types");
			selectAllGroup = track.append("g")
				.attr("transform", "translate(" + ((-specimenTypes.length) * (sp.size.selectBox + sp.padding.selectBox) - sp.size.dataPoint / 2) + ", 10)")
				.attr("class", "select-all")
				.attr("id", "select-all-" + track.datum().subject_id);
			selectAllGroup.selectAll("rect")
				.data(specimenTypes)
				.enter()
				.append("rect")
				.attr("x", function(d, i) {return i * (sp.size.selectBox + sp.padding.selectBox);})
				.attr("y", 0)
				.attr("width", sp.size.selectBox)
				.attr("height", sp.size.selectBox)
				.on("click", function() {sp._toggleTrack(this);})
				.attr("class", function(d) {return d;});
			
			// Add data "points" for each visit to the tracks
			var visit = track.selectAll("g.visit")
				.data(function(subjectData) {return subjectData.visits;})
				.enter().append("g")
				.attr("transform", function(visit) {return "translate(0, " + (-sp.size.dataPoint * visit.specimens.length / 2) + ")"})
				.attr("class", "visit");
			visit.selectAll("rect")
				.data(function(visit) {return visit.specimens;})
				.enter().append("rect")
				.attr("x", 0)
				.attr("y", function(specimen, i) {return i * (sp.size.dataPoint + sp.padding.selectBox);})
				.attr("height", sp.size.dataPoint)
				.attr("width", sp.size.dataPoint)
				.on("click", function() {sp._toggleSelected(this);})
				.attr("class", function(specimen) {return specimen.type;});
			track.selectAll("g.visit").filter(function(visit) {return visit.type == "surgery";})
				.append("text")
					.text("T")
					.attr("class", "visit-label")
					.attr("x", (sp.size.dataPoint / 2))
					.attr("y", function(visit) {return visit.specimens.length * (sp.size.dataPoint + sp.padding.selectBox) + 8;});
			
			// Add x-axis
			sp._xAxis = d3.svg.axis().orient("bottom");
			sp._xAxisGroup = svg.append("g")
				.attr("class", "x axis")
				.attr("transform", "translate(" + sp.margin.left + ", " + (sp._chartHeight + sp.section.xAxis.height / 2) + ")");
			svg.append("text")
				.attr("class", "axis-label")
				.attr("id", "axis-label")
				.attr("x", (sp.margin.left + sp._chartWidth / 2))
				.attr("y", (sp.margin.top + sp._chartHeight + sp.section.xAxis.height))
				.attr("text-anchor", "middle");
				
			// Add legend
			var legendGroup = svg.append("g")
				.attr("transform", "translate(" + (sp.margin.left + 150) + ", " + (sp._chartHeight + sp.section.xAxis.height + sp.section.legend.height) + ")");
			legendGroup.append("rect")
				.attr("class", "legend-border")
				.attr("x", -sp.size.dataPoint * 1.5)
				.attr("y", -sp.size.dataPoint)
				.attr("width", specimenTypes.length * (sp.size.dataPoint + sp.section.legend.colWidth))
				.attr("height", sp.size.dataPoint * 3);
			var legendItems = legendGroup.selectAll("g")
				.data(specimenTypes)
				.enter()
				.append("g")
				.attr("transform", function(d, i) {return "translate(" + (i * (sp.size.dataPoint + sp.section.legend.colWidth)) + ", 0)";});
			legendItems.append("rect")
				.attr("class", function(d) {return d;})
				.on("click", function() {sp._toggleLegend(this);})
				.attr("x", 0)
				.attr("y", 0)
				.attr("width", sp.size.dataPoint)
				.attr("height", sp.size.dataPoint);
			legendItems.append("text")
				.text(function(d) {return d;})
				.attr("class", "legend")
				.attr("x", sp.size.dataPoint + 5)
				.attr("y", 11);
		
			// Layout data along the x-axis
			sp._layoutX();
		});
	},
	
	/*
	 * Print an error message to the page.
	 */
	_showErrorMessage: function(divId, error) {
		var message = "";
		if (error.message) {
			message = error.message;
		}
		else if (error.statusText) {
			message = error.statusText;
		}
		console.log("Error: " + message);
		d3.select(divId).append("h3")
			.text("Error retrieving data")
			.attr("class", "error-msg");
		d3.select(divId).append("p")
			.text("Cause: " + message);
	},
	
	/*
	 * Compute bounding box for text.
	 */
	_getBBox: function(text, cssStyle) {
		var textElement = d3.select("svg")
			.append("text")
			.text(text)
			.attr("class", cssStyle)
			.attr("x", 0)
			.attr("y", 0);
		var bbox = textElement.node().getBBox();
		textElement.remove();
		return bbox;
	},
	
	/*
	 * Toggle whether a given specimen data point is selected.
	 */
	_toggleSelected: function(rect) {
		var specimen = d3.select(rect).datum();
		var selected = specimen.selected;
		if (selected === undefined) {
			selected = true;
		}
		else {
			selected = !selected;
		}
		specimen.selected = selected;
		var style = rect.getAttribute("class");
		var i = style.search("-selected");
		if (i < 0) {
			style = style + "-selected";
		}
		else {
			style = style.substring(0, i);
		}
		rect.setAttribute("class", style);
	},
	
	/*
	 * Toggles selection of all data points of a given specimen type
	 * for an entire track.
	 */
	_toggleTrack: function(rect) {
		var oldClass = d3.select(rect).attr("class");
		var i = oldClass.search("-selected");
		var newClass = "";
		var selected;
		if (i < 0) {
			newClass = oldClass + "-selected"
			selected = true;
		}
		else {
			newClass = oldClass.substring(0, i);
			selected = false;
		}
		d3.select(rect).attr("class", newClass);
		if (oldClass.search("all-specimen-types") >= 0) {
			d3.select(rect.parentNode)
				.selectAll(".visit rect")
				.filter(function(specimen) {return specimen.selected != selected;})
				.attr("class", function(specimen) {
					var c = specimen.type;
					if (selected) {
						c = c + "-selected";
					}
					return c;
				})
				.each(function(specimen) {specimen.selected = selected});
			d3.select(rect.parentNode)
				.selectAll(".select-all rect")
				.attr("class", function(specimenType) {
					var c = specimenType;
					if (selected) {
						c = specimenType + "-selected";
					}
					return c;
				});
		}
		else {
			d3.select(rect.parentNode.parentNode).selectAll(".visit rect." + oldClass)
				.attr("class", newClass)
				.each(function(d) {d.selected = selected});
		}
	},
	
	_toggleLegend: function(rect) {
		var oldClass = d3.select(rect).attr("class");
		var i = oldClass.search("-selected");
		var newClass = "";
		var selected;
		if (i < 0) {
			newClass = oldClass + "-selected"
			selected = true;
		}
		else {
			newClass = oldClass.substring(0, i);
			selected = false;
		}
		d3.select(rect).attr("class", newClass);
		d3.selectAll("g.select-all rect." + oldClass)
			.each(function() {sp._toggleTrack(this);});
	},
	
	/*
	 * Computes how many pixels required to create a track for the given subject.
	 */
	_computeTrackHeight: function(subject, visitLabelHeight, labelContainerHeight) {
		var maxStackedPoints = 1;
		for (var i = 0; i < subject.visits.length; i++) {
			var visit = subject.visits[i];
			if (visit.specimens.length > maxStackedPoints) {
				maxStackedPoints = visit.specimens.length;
			}
		}
		var height = maxStackedPoints * (sp.size.dataPoint + sp.padding.dataPoint)
			+ visitLabelHeight + sp.padding.track * 2;
		if (labelContainerHeight > height) {
			height = labelContainerHeight;
		}
		return height;
	},
	
	/*
	 * Extract unique specimen types from input data.
	 */
	_uniqueSpecimenTypes: function(data) {
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
		return Object.keys(specimenTypesDict);	
	},
	
	/*
	 * Update x-coordinate of data points.
	 */
	_update: function() {
		var visits = d3.selectAll((".visit"))
			.transition()
			.duration(500);
		if (sp._anchor == "date") {
			visits.attr("transform", function(data) {
				var x = sp._x(sp._parser.parse(data.date)) - sp.size.dataPoint / 2;
				var y = -(sp.size.dataPoint * data.specimens.length + sp.padding.selectBox * (data.specimens.length - 1)) / 2;
				return "translate(" + x + ", " + y + ")";
			});
		}
		else {
			visits.attr("transform", function(data) {
				var x = sp._x(data.daysSinceAnchorVisit) - sp.size.dataPoint / 2;
				var y = -(sp.size.dataPoint * data.specimens.length + sp.padding.selectBox * (data.specimens.length - 1)) / 2;
				return "translate(" + x + ", " + y + ")";
			});
		}
	},

	/*
	 * Lay out x-range and x-axis.
	 */
	_layoutX: function() {
		var axisLabel = ""
		if (sp._anchor == "date") {
			axisLabel = "Date"
			var minDate = d3.min(sp._data, function(d) {
				return d3.min(d.visits, function(visit) {
					return sp._parser.parse(visit.date);
				});
			});
			var maxDate = d3.max(sp._data, function(d) {
				return d3.max(d.visits, function(visit) {
					return sp._parser.parse(visit.date);
				});
			});
			sp._x = d3.time.scale()
				.range([0, sp._chartWidth])
				.domain([minDate, maxDate]);
		}
		else {
			axisLabel = "Day relative to " + sp._anchor;
			sp._setDaysSinceAnchorVisit();
			var minDay = d3.min(sp._data, function(d) {
				return d3.min(d.visits, function(visit) {
					return visit.daysSinceAnchorVisit;
				});
			});
			var maxDay = d3.max(sp._data, function(d) {
				return d3.max(d.visits, function(visit) {
					return visit.daysSinceAnchorVisit;
				});
			});
			sp._x = d3.scale.linear()
				.range([0, sp._chartWidth])
				.domain([minDay, maxDay]);
		}
		sp._xAxis.scale(sp._x);
		sp._xAxisGroup.call(sp._xAxis);
		d3.select("#axis-label").text(axisLabel);
		sp._update();
	},

	/*
	 * Set time in days since/till the anchor visit.
	 */
	_setDaysSinceAnchorVisit: function() {
		var msecInDay = 1000 * 60 * 60 * 24;
		for (var i = 0; i < sp._data.length; i++) {
			var subjectRec = sp._data[i];
			var refDate = null;
			for (var j = 0; j < subjectRec.visits.length; j++) {
				var visit = subjectRec.visits[j];
				if (visit.type == sp._anchor) {
					refDate = sp._parser.parse(visit.date);
					break;
				}
			}
			for (var j = 0; j < subjectRec.visits.length; j++) {
				var visit = subjectRec.visits[j];
				var visitDate = sp._parser.parse(visit.date);
				visit.daysSinceAnchorVisit = Math.round((visitDate.getTime() - refDate.getTime()) / msecInDay);
			}
		}
	}
};
