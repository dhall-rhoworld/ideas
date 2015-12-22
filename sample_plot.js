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
	_xAxisX: 0,
	_xAxisWidth: 0,
	
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
			
			var specimenTypes = sp._uniqueSpecimenTypes(data);
			
			// Compute coordinates only needed for initial layout
			var tempCoords = sp._computeTempCoordinates(specimenTypes);
					
			// Compute coordinates that we will need for initial layout and updates
			sp._plotWidth = width - sp.margin.left - sp.margin.right - 2 * sp.border;
			sp._plotHeight = 0;
			for (var i = 0; i < data.length; i++) {
				if (i > 0) {
					sp._plotHeight += sp.border;
				}
				sp._trackY.push(sp._plotHeight);
				var height = sp._computeTrackHeight(data[i], tempCoords.visitLabelHeight,
					tempCoords.labelContainerHeight);
				sp._trackHeight.push(height);
				sp._plotHeight += height;
			}
			sp._xAxisX = sp.padding.track * 2 + tempCoords.labelContainerWidth;
			sp._xAxisWidth = sp._plotWidth - sp._xAxisX - sp.padding.track;
			
			// Create overall container for chart
			var chart = svg.append("g")
				.attr("id", "chart-container")
				.attr("transform", "translate(" + (sp.margin.left + sp.border) + ", "
					+ (sp.margin.top + sp.border) + ")");
			
			// Add plot section
			sp._layoutPlot(chart, tempCoords, specimenTypes);
			
			// Add x-axis section
			
			// Add legend section
			
			
			// *** Review
			var height = sp._plotHeight + sp.margin.top + sp.margin.bottom + sp.section.xAxis.height + sp.section.legend.height;
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
	 * Compute "temporary" coordinates only needed for initial layout.
	 */
	_computeTempCoordinates: function(specimenTypes) {
		var visitLabelBBox = sp._getBBox("T", "visit-label");
		var coords = {
			visitLabelWidth: visitLabelBBox.width,
			visitLabelHeight: visitLabelBBox.height,
			trackLabelWidth: 0,
			trackLabelHeight: 0,
			labelContainerWidth: 0,
			labelContainerHeight: 0
		};
		for (var i = 0; i < sp._data.length; i++) {
			var bbox = sp._getBBox(sp._data[i].subject_id, "subject-label");
			if (bbox.width > coords.trackLabelWidth) {
				coords.trackLabelWidth = bbox.width;
			}
			if (bbox.height > coords.trackLabelHeight) {
				coords.trackLabelHeight = bbox.height;
			}
		}
		coords.labelContainerHeight = coords.trackLabelHeight + sp.size.selectBox
			+ sp.padding.track * 2 + sp.padding.selectBox;
		var multiSelectContainerWidth = (specimenTypes.length + 1) * sp.size.selectBox +
			specimenTypes.length * sp.padding.selectBox;
		coords.labelContainerWidth = multiSelectContainerWidth;
		if (coords.visitLabelWidth > coords.labelContainerWidth) {
			coords.labelContainerWidth = coords.visitLabelWidth;
		}
		return coords;
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
	
	_layoutPlot: function(chart, tempCoords, specimenTypes) {

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
			
		// Layout tracks
		sp._layoutTracks(plot, tempCoords, specimenTypes);
	},
	
	_layoutTracks: function(plot, tempCoords, specimenTypes) {
			
		// Add track containers
		var tracks = plot.selectAll("g.track-container")
			.data(sp._data)
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
			
		// Add track label sections
		sp._layoutTrackLabelSections(tracks, tempCoords, specimenTypes);
		
		// Add track data sections
		sp._layoutTrackDataSections(tracks, tempCoords);
	},
	
	_layoutTrackLabelSections: function(tracks, tempCoords, specimenTypes) {
		var labelContainers = tracks.append("g")
			.attr("class", "label-container")
			.attr("transform", function(visit, i) {
				return "translate(" + sp.padding.track + ","
					+ (sp._trackHeight[i] / 2 - tempCoords.labelContainerHeight / 2) + ")";
			});
		labelContainers.append("text")
			.text(function(subject) {return subject.subject_id;})
			.attr("class", "subject-label")
			.attr("y", tempCoords.trackLabelHeight);
		labelContainers.append("rect")
			.attr("class", "all-specimen-types")
			.attr("x", 0)
			.attr("y", tempCoords.trackLabelHeight + sp.padding.selectBox)
			.attr("width", sp.size.selectBox)
			.attr("height", sp.size.selectBox);
		var multiSelectContainers = labelContainers.append("g")
			.attr("class", "multi-select-container")
			.attr("transform", "translate(" + (sp.size.selectBox + sp.padding.selectBox)
				+ ", " + (tempCoords.trackLabelHeight + sp.padding.selectBox) + ")");
		multiSelectContainers.selectAll("rect")
			.data(specimenTypes)
			.enter()
			.append("rect")
			.attr("class", function(specimenType) {return specimenType;})
			.attr("x", function(specimenType, i) {return i * (sp.size.selectBox + sp.padding.selectBox);})
			.attr("y", 0)
			.attr("width", sp.size.selectBox)
			.attr("height", sp.size.selectBox);
	},
	
	_layoutTrackDataSections: function(tracks, tempCoords) {
			
		// Add container for data points
		var dataContainer = tracks.append("g")
			.attr("class", "data-container")
			.attr("transform", "translate(" + sp._xAxisX + ", " + sp.padding.track + ")");
			
		// Add containers for visit data points group
		var visitContainer = dataContainer.selectAll("g.visit-container")
			.data(function(subject, i) {
				return subject.visits;
				
				// Hack to keep track of track index
				sp._trackNo = i;
			})
			.enter()
			.append("g")
			.attr("class", "visit-container")
			.attr("transform", function(visit) {
				var n = visit.specimens.length;
				var stackHeight = n * sp.size.dataPoint + (n - 1) * sp.padding.dataPoint;
				if (visit.type == "surgery") {
					stackHeight += tempCoords.visitLabelHeight + sp.padding.dataPoint;
				}
				return "translate(0, " + (sp._trackHeight[sp._trackNo] / 2
					- stackHeight / 2) + ")";
			});
			
		// Add data points
		visitContainer.selectAll("rect")
			.data(function(visit) {return visit.specimens;})
			.enter()
			.append("rect")
			.attr("class", function(specimen) {return specimen.type;})
			.attr("x", 0)
			.attr("y", function(specimen, i) {return i * (sp.size.dataPoint + sp.padding.dataPoint);})
			.attr("width", sp.size.dataPoint)
			.attr("height", sp.size.dataPoint);
			
		// Add visit label
		dataContainer.selectAll("g.visit-container")
			.filter(function(visit) {return visit.type == "surgery";})
			.append("text")
			.text("T")
			.attr("class", "visit-label")
			.attr("x", sp.size.dataPoint / 2)
			.attr("y", function(visit, i) {
				return visit.specimens.length * (sp.size.dataPoint
					+ sp.padding.dataPoint) + tempCoords.visitLabelHeight;
			});	
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
		var maxHeight = 0;
		for (var i = 0; i < subject.visits.length; i++) {
			var visit = subject.visits[i];
			var n = visit.specimens.length;
			var height = n * sp.size.dataPoint + (n - 1) * sp.padding.dataPoint;
			if (visit.type == "surgery") {
				height += visitLabelHeight + sp.padding.dataPoint;
			}
			if (height > maxHeight) {
				maxHeight = height;
			}
		}
		if (labelContainerHeight > maxHeight) {
			maxHeight = labelContainerHeight;
		}
		return maxHeight + 2 * sp.padding.track;
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
