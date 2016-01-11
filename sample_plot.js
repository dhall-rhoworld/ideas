var sp = {

	// Public attributes
	margin: {top: 20, right: 20, bottom: 20, left: 20},
	border: 5,
	size: {dataPoint: 10, multiSelectBox: 10, legendSelectBox: 15},
	padding: {track: 5, multiSelectBox: 3, dataPoint: 3, axis: 10, legend: 15,
		legendSelectBox_left: 20, legendSelectBox_right: 5, legendSelectBox_bottom: 5},
	
	// Private attributes
	_anchor: "date",
	_data: {},
	_x: {},
	_xAxis: {},
	_xAxisGroup: {},
	_parser: d3.time.format("%m/%d/%Y"),
	_xAxisWidth: 0,
	_hideList: {},
	
	/**
	 * Set the visit type that will anchor the timeline.
	 * @param {string} visitType - A visit type. 
	 */
	setAnchor: function(visitType) {
		sp._anchor = visitType;
		sp._updateXAxis();
	},
	
	/**
	 * Get selected specimens.
	 * @return {array} Selected specimen IDs.
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
				.attr("id", "svgMain")
				.attr("width", width);

			// Compute coordinates only needed for initial layout
			var specimenTypes = sp._uniqueSpecimenTypes(data);
			var tempCoords = sp._computeTempCoordinates(specimenTypes);

			// Compute coordinates that we will need for initial layout and updates
			sp._xAxisWidth = tempCoords.plotWidth - tempCoords.xAxisX - sp.padding.track;
			
			// Create overall container for chart
			var chart = svg.append("g")
				.attr("id", "chart-container")
				.attr("transform", "translate(" + (sp.margin.left + sp.border) + ", "
					+ (sp.margin.top + sp.border) + ")");
			
			// Add plot sections
			sp._layoutPlot(chart, tempCoords, specimenTypes);
			sp._layoutXAxis(chart, tempCoords);
			sp._layoutLegend(chart, specimenTypes, tempCoords);
			
			// Initialize x-axis and position data points
			sp._updateXAxis();

			// Set svg canvas height
			var height = 
				sp.margin.top
				+ sp.border
				+ tempCoords.plotHeight
				+ sp.border
				+ tempCoords.xAxisSectionHeight
				+ sp.border
				+ tempCoords.legendHeight
				+ sp.border
				+ sp.margin.bottom;
			svg.attr("height", height);
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
			plotWidth: d3.select("#svgMain").attr("width") - sp.margin.left
				- sp.margin.right - 2 * sp.border,
			visitLabelWidth: visitLabelBBox.width,
			visitLabelHeight: visitLabelBBox.height,
			plotHeight: 0,
			trackHeight: [],
			trackY: [],
			trackLabelWidth: 0,
			trackLabelHeight: 0,
			labelContainerHeight: 0,
			xAxisX: 0,
			xAxisLabelHeight: sp._getBBox("Date", "axis-label").height,
			xAxisHeight: 0,
			xAxisSectionHeight: 0,
			legendTextHeight: 0,
			legendLinkHeight: 0,
			legendFirstLineHeight: 0,
			legendContentHeight: 0,
			legendHeight: 0
		};
		
		// Track label
		for (var i = 0; i < sp._data.length; i++) {
			var bbox = sp._getBBox(sp._data[i].subject_id, "subject-label");
			if (bbox.width > coords.trackLabelWidth) {
				coords.trackLabelWidth = bbox.width;
			}
			if (bbox.height > coords.trackLabelHeight) {
				coords.trackLabelHeight = bbox.height;
			}
		}
		
		// Track label container
		coords.labelContainerHeight = coords.trackLabelHeight + sp.size.multiSelectBox
			+ sp.padding.track * 2 + sp.padding.multiSelectBox;
		
		// x-axis
		var multiSelectContainerWidth = (specimenTypes.length + 1) * sp.size.multiSelectBox +
			specimenTypes.length * sp.padding.multiSelectBox;
		var trackLabelContainerWidth = multiSelectContainerWidth;
		if (coords.visitLabelWidth > trackLabelContainerWidth) {
			trackLabelContainerWidth = coords.visitLabelWidth;
		}
		coords.xAxisX = sp.padding.track * 2 + trackLabelContainerWidth
			+ sp.size.dataPoint;
		
		// Plot and tracks
		for (var i = 0; i < sp._data.length; i++) {
			if (i > 0) {
				coords.plotHeight += sp.border;
			}
			coords.trackY.push(coords.plotHeight);
			var height = sp._computeTrackHeight(sp._data[i], coords.visitLabelHeight,
				coords.labelContainerHeight);
			coords.trackHeight.push(height);
			coords.plotHeight += height;
		}

		// Axis
		var x = d3.scale.linear()
			.domain([0, 10])
			.range([0, sp._xAxisWidth]);
		var axis = d3.svg.axis().orient("bottom").scale(x);
		var group = d3.select("svg").append("g");
		group.call(axis);
		coords.xAxisHeight = group.node().getBBox().height;
		coords.xAxisSectionHeight =  + coords.xAxisHeight + coords.xAxisLabelHeight
			+ 3 * sp.padding.axis;
		group.remove();
			
		// Legend
		coords.legendTextHeight = sp._getBBox("DNA", "legend").height;
		coords.legendLinkHeight = sp._getBBox("hide", "legend-link").height;
		coords.legendFirstLineHeight = coords.legendTextHeight;
		if (sp.size.legendSelectBox > coords.legendFirstLineHeight) {
			coords.legendFirstLineHeight = sp.size.legendSelectBox;
		}
		coords.legendContentHeight = coords.legendFirstLineHeight
			+ sp.padding.legendSelectBox_bottom + coords.legendLinkHeight;
		coords.legendHeight = coords.legendContentHeight + 2 * sp.padding.legend;
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
			.attr("y", 0)
			.attr("class", "invisible");
		var bbox = textElement.node().getBBox();
		textElement.remove();
		return bbox;
	},
	
	/*
	 * Layout plotting area of chart.
	 */
	_layoutPlot: function(chart, tempCoords, specimenTypes) {

		// Add container for plotting section
		var plot = chart.append("g")
			.attr("id", "plot-container")
			.attr("transform", "translate(0, 0)");
		
		// Add background to plotting section
		plot.append("rect")
			.attr("class", "plot-bg")
			.attr("x", tempCoords.xAxisX)
			.attr("y", 0)
			.attr("width", tempCoords.plotWidth - tempCoords.xAxisX)
			.attr("height", tempCoords.plotHeight);
			
		// Layout tracks
		sp._layoutTracks(plot, tempCoords, specimenTypes);
	},
	
	/*
	 * Lay out individual data tracks, one per subject.
	 */
	_layoutTracks: function(plot, tempCoords, specimenTypes) {
			
		// Add track containers
		var tracks = plot.selectAll("g.track-container")
			.data(sp._data)
			.enter()
			.append("g")
			.attr("id", function(subject) {return "track-" + subject.subject_id;})
			.attr("class", "track-container")
			.attr("transform", function(subject, i) {
				return "translate(0, " + tempCoords.trackY[i] + ")";
			});
			
		// Draw track backgrounds
		tracks.append("rect")
			.attr("class", "track-bg")
			.attr("x", 0)
			.attr("y", 0)
			.attr("width", tempCoords.plotWidth)
			.attr("height", function(subject, i) {return tempCoords.trackHeight[i];});
			
		// Add track label sections
		sp._layoutTrackLabelSections(tracks, tempCoords, specimenTypes);
		
		// Add track data sections
		sp._layoutTrackDataSections(tracks, tempCoords);
	},
	
	/*
	 * Layout left-most track area containing subject names and multi-select boxes.
	 */
	_layoutTrackLabelSections: function(tracks, tempCoords, specimenTypes) {
	
		// Overall container
		var labelContainers = tracks.append("g")
			.attr("class", "label-container")
			.attr("transform", function(visit, i) {
				return "translate(" + sp.padding.track + ","
					+ (tempCoords.trackHeight[i] / 2 - tempCoords.labelContainerHeight / 2) + ")";
			});
			
		// Subject name
		labelContainers.append("text")
			.text(function(subject) {return subject.subject_id;})
			.attr("class", "subject-label")
			.attr("y", tempCoords.trackLabelHeight);
			
		// Select for all specimens on track
		labelContainers.append("g").append("rect")
			.attr("class", "all-specimen-types")
			.attr("x", 0)
			.attr("y", tempCoords.trackLabelHeight + sp.padding.multiSelectBox)
			.attr("width", sp.size.multiSelectBox)
			.attr("height", sp.size.multiSelectBox)
			.on("click", function() {sp._toggleTrackAllMultiSelects(this);});
			
		// Container for specimen type-specific multi-selectors
		var multiSelectContainers = labelContainers.append("g")
			.attr("class", "multi-select-container")
			.attr("transform", "translate(" + (sp.size.multiSelectBox + sp.padding.multiSelectBox)
				+ ", " + (tempCoords.trackLabelHeight + sp.padding.multiSelectBox) + ")");
				
		// Specimen type-specific multi-selectors
		multiSelectContainers.selectAll("rect")
			.data(specimenTypes)
			.enter()
			.append("rect")
			.attr("class", function(specimenType) {return specimenType;})
			.attr("x", function(specimenType, i) {return i * (sp.size.multiSelectBox + sp.padding.multiSelectBox);})
			.attr("y", 0)
			.attr("width", sp.size.multiSelectBox)
			.attr("height", sp.size.multiSelectBox)
			.on("click", function(specimenType) {sp._toggleTrackMultiSelect(specimenType, this);});
	},
	
	/*
	 * Lay out data section of tracks.
	 */
	_layoutTrackDataSections: function(tracks, tempCoords) {
			
		// Add container for data points
		var dataContainer = tracks.append("g")
			.attr("class", "data-container")
			.attr("transform", function(track, i) {
				return "translate(" + tempCoords.xAxisX + "," + (tempCoords.trackHeight[i] / 2) + ")";
			});
			
		// Add containers for visit data points group
		var visitContainer = dataContainer.selectAll("g.visit-container")
			.data(function(subject, i) {return subject.visits;})
			.enter()
			.append("g")
			.attr("class", "visit-container")
			.attr("transform", function(visit) {
				var n = visit.specimens.length;
				var stackHeight = n * sp.size.dataPoint + (n - 1) * sp.padding.dataPoint;
				if (visit.type == "surgery") {
					stackHeight += tempCoords.visitLabelHeight + sp.padding.dataPoint;
				}
				visit.y = -stackHeight / 2;
				return "translate(0, " + visit.y + ")";
			});
			
		// Add data points
		sp._layoutDataPoints(visitContainer);
			
		// Add visit label
		dataContainer.selectAll("g.visit-container")
			.filter(function(visit) {return visit.type == "surgery";})
			.append("text")
			.text("T")
			.attr("class", "visit-label")
			.attr("x", sp.size.dataPoint / 2)
			.attr("y", function(visit, i) {
				var y = tempCoords.visitLabelHeight;
				for (var i = 0; i < visit.specimens.length; i++) {
					if (!sp._hideList[visit.specimens[i].type]) {
						y += sp.size.dataPoint + sp.padding.dataPoint;
					}
				}	
				return y;
			});
	},
	
	/*
	 * Lay out data points for each visit.
	 */
	_layoutDataPoints: function(visitContainer) {
		var dataPoints = visitContainer.selectAll("rect")
			.data(function(visit) {
				var specimens = [];
				for (var i = 0; i < visit.specimens.length; i++) {
					if (!sp._hideList[visit.specimens[i].type]) {
						specimens.push(visit.specimens[i]);
					}
				}
				return specimens;
			}, function(specimen) {return specimen.id;});
		dataPoints.enter()
			.append("rect")
			.attr("class", function(specimen) {
				var rectClass = specimen.type;
				if (specimen.selected) {
					rectClass += "-selected"
				}
				return rectClass;
			})
			.attr("y", function(specimen, i) {return i * (sp.size.dataPoint + sp.padding.dataPoint);})
			.attr("width", sp.size.dataPoint)
			.attr("height", sp.size.dataPoint)
			.on("click", function(specimen) {sp._toggleSpecimenSelection(specimen, this);});
		dataPoints.exit().remove();
	},
	
	/*
	 * Lay out x-axis components.
	 */
	_layoutXAxis: function(chart, tempCoords) {
		
		// Overall container
		var axisContainer = chart.append("g")
			.attr("id", "axis-container")
			.attr("transform", "translate(" + tempCoords.xAxisX + ", " + (tempCoords.plotHeight + sp.border) + ")");
			
		// Axis
		sp._xAxis = d3.svg.axis().orient("bottom");
		
		// Container for axis pieces
		sp._xAxisGroup = axisContainer.append("g")
			.attr("class", "x axis")
			.attr("transform", "translate(0, " + sp.padding.axis + ")");
			
		// Axis label
		sp._xAxisLabel = axisContainer.append("text")
			.attr("id", "axis-label")
			.attr("class", "axis-label")
			.attr("x", sp._xAxisWidth / 2)
			.attr("y", 2 * sp.padding.axis + tempCoords.xAxisHeight + tempCoords.xAxisLabelHeight)
			.attr("text-anchor", "middle");
	},
	
	/*
	 * Lay out legend components.
	 */
	_layoutLegend: function(chart, specimenTypes, tempCoords) {
		
		// Compute legend width and placement of select boxes
		var boxX = [];
		var width = sp.padding.legend;
		for (var i = 0; i < specimenTypes.length; i++) {
			if (i > 0) {
				width += sp.padding.legendSelectBox_left;
			}
			boxX.push(width);
			width += sp.size.legendSelectBox + sp.padding.legendSelectBox_right +
				sp._getBBox(specimenTypes[i], "legend").width;
		}
		width += sp.padding.legend;
		
		// Add overall container for legend
		var x = tempCoords.xAxisX + sp._xAxisWidth / 2 - width / 2;
		var y = tempCoords.plotHeight + sp.border + tempCoords.xAxisSectionHeight + sp.border;
		var legendContainer = chart.append("g")
			.attr("id", "legend-container")
			.attr("transform", "translate(" + x + ", " + y + ")");
		
		// Draw rect around legend
		legendContainer.append("rect")
			.attr("class", "legend-border")
			.attr("x", 0)
			.attr("y", 0)
			.attr("width", width)
			.attr("height", tempCoords.legendHeight);
			
		// Add out container for select boxes, text, and link
		var selectContainers = legendContainer.selectAll("g.legend-select-container")
			.data(specimenTypes)
			.enter()
			.append("g")
			.attr("class", "legend-select-container")
			.attr("transform", function(specimenType, i) {
				var y = tempCoords.legendHeight / 2 - tempCoords.legendContentHeight / 2;
				var translate = "translate(" + boxX[i] + ", " + y + ")";
				return translate;
			});
			
		// Add inner container for rect and text
		var innerContainers = selectContainers.append("g")
			.attr("class", "legend-select-inner-container");
			
		// Add select boxes
		innerContainers.append("rect")
			.attr("class", function(specimenType) {return specimenType;})
			.attr("x", 0)
			.attr("y", 0)
			.attr("width", sp.size.legendSelectBox)
			.attr("height", sp.size.legendSelectBox)
			.on("click", function(specimenType) {sp._toggleLegendMultiSelect(specimenType, this);});
			
		// Add text
		innerContainers.append("text")
			.text(function(specimenType){return specimenType;})
			.attr("class", "legend")
			.attr("x", sp.size.legendSelectBox + sp.padding.legendSelectBox_right)
			.attr("y", tempCoords.legendTextHeight);
		
		// Add link
		selectContainers.append("text")
			.text("hide")
			.attr("class", "legend-link")
			.attr("x", sp.size.legendSelectBox / 2)
			.attr("y", tempCoords.legendFirstLineHeight + sp.padding.legendSelectBox_bottom
				+ tempCoords.legendLinkHeight)
			.on("click", function(specimen) {sp._toggleShowHideSampleType(specimen, this);});
	},
	
	/*
	 * Toggle whether a given specimen data point is selected.
	 */
	_toggleSpecimenSelection: function(specimen, rect) {
		if (specimen.selected === undefined) {
			specimen.selected = true;
		}
		else {
			specimen.selected = !specimen.selected;
		}
		var style = specimen.type;
		if (specimen.selected) {
			style += "-selected";
		}
		rect.setAttribute("class", style);
	},
	
	/*
	 * Toggle selector that selects all specimens for a track.
	 */
	_toggleTrackAllMultiSelects: function(rect) {
		var selected = rect.getAttribute("class") == "all-specimen-types";
		if (selected) {
			rect.setAttribute("class", "all-specimen-types-selected");
		}
		else {
			rect.setAttribute("class", "all-specimen-types");
		}
		d3.select(rect.parentNode.parentNode)
			.selectAll(".multi-select-container rect")
			.filter(function() {
				var typeSelected = this.getAttribute("class").search(/-selected$/) < 0;
				return typeSelected == selected;
			})
			.each(function(specimenType) {sp._toggleTrackMultiSelect(specimenType, this);});
	},
	
	/*
	 * Toggles selection of all data points of a given specimen type
	 * for an entire track.
	 */
	_toggleTrackMultiSelect: function(specimenType, rect) {
	
		// Change style of clicked rect
		var oldClass = rect.getAttribute("class");
		var newClass = "";
		var selected = oldClass.search(/-selected$/) < 0;
		if (selected) {
			newClass = specimenType + "-selected";
		}
		else {
			newClass = specimenType;
		}
		rect.setAttribute("class", newClass);
		
		// Toggle data points
		if (specimenType == "all-specimen-types") {
			d3.select(rect.parentNode.parentNode.parentNode)
				.selectAll(".visit-container rect")
				.filter(function(specimen) {return specimen.selected != selected;})
				.each(function(specimen) {sp._toggleSpecimenSelection(specimen, this);});
		}
		else {
			d3.select(rect.parentNode.parentNode.parentNode)
				.selectAll(".visit-container ." + oldClass)
				.each(function(specimen) {sp._toggleSpecimenSelection(specimen, this);});
		}
	},
	
	/*
	 * Toggle legend multi-select, which has the effect of selecting all specimens
	 * of the given type.
	 */
	_toggleLegendMultiSelect: function(specimenType, rect) {
	
		// Change style of clicked rect
		var oldClass = rect.getAttribute("class");
		var newClass = "";
		var selected = oldClass.search(/-selected$/) < 0;
		if (selected) {
			newClass = specimenType + "-selected";
		}
		else {
			newClass = specimenType;
		}
		rect.setAttribute("class", newClass);
		
		// Toggle individual track multi-selects
		d3.selectAll(".multi-select-container rect")
			.filter(function() {return this.getAttribute("class") == oldClass;})
			.each(function(specimenType) {sp._toggleTrackMultiSelect(specimenType, this);});
	},
	
	/*
	 * Computes how many pixels required to create a track for the given subject.
	 */
	_computeTrackHeight: function(subject, visitLabelHeight, labelContainerHeight) {
		var maxHeight = 0;
		for (var i = 0; i < subject.visits.length; i++) {
			var visit = subject.visits[i];
			var specimens = visit.specimens;
			//var n = visit.specimens.length;
			//var height = n * sp.size.dataPoint + (n - 1) * sp.padding.dataPoint;
			var height = 0;
			var count = 0;
			for (var j = 0; j < specimens.length; j++) {
				if (!sp._hideList[specimens[j].type]) {
					count++;
					if (count > 1) {
						height += sp.padding.dataPoint;
					}
					height += sp.size.dataPoint;
				}
			}
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
	 * Lay out x-range and x-axis.
	 */
	_updateXAxis: function() {
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
				.range([0, sp._xAxisWidth])
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
				.range([0, sp._xAxisWidth])
				.domain([minDay, maxDay]);
		}
		sp._xAxis.scale(sp._x);
		sp._xAxisGroup.call(sp._xAxis);
		sp._xAxisLabel.text(axisLabel);
		sp._updateDataPoints();
	},
	

	/*
	 * Update x-coordinate of data points.
	 */
	_updateDataPoints: function() {
		var visits = d3.selectAll(("g.visit-container"))
			.transition()
			.duration(500);
		if (sp._anchor == "date") {
			visits.attr("transform", function(visit) {
				var x = sp._x(sp._parser.parse(visit.date)) - sp.size.dataPoint / 2;
				return "translate(" + x + ", " + visit.y + ")";
			});
		}
		else {
			visits.attr("transform", function(visit) {
				var x = sp._x(visit.daysSinceAnchorVisit) - sp.size.dataPoint / 2;
				return "translate(" + x + ", " + visit.y + ")";
			});
		}
	},
	
	/*
	 * Toggle whether to show or hide a type of sample.
	 */
	_toggleShowHideSampleType: function(specimenType, text) {
		if (text.textContent == "hide") {
			text.textContent = "show";
			d3.select(text.parentNode).select("g")
				.attr("class", "legend-select-inner-container-disabled");
			d3.select(text.parentNode).select("rect")
				.on("click", function() {});
			sp._hideList[specimenType] = true;
		}
		else {
			text.textContent = "hide";
			d3.select(text.parentNode).select("g")
				.attr("class", "legend-select-inner-container");
			d3.select(text.parentNode).select("rect")
				.on("click", function(specimenType) {sp._toggleLegendMultiSelect(specimenType, this);});
			sp._hideList[specimenType] = false;
		}
		var visitContainers = d3.selectAll("g.visit-container");
		sp._layoutDataPoints(visitContainers);
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
