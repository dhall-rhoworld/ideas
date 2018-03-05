function initializeOptionsDialogFields() {
	
	// Highlighting
	$("#highlight_site").click(function() {
		onHighlightChange();
	});
	
	$("#highlight_phase").click(function() {
		onHighlightChange();
	});
	
	$("#highlight_remove").click(function() {
		$("#highlight_site").val("");
		$("#highlight_phase").val("");
		onHighlightChange();
	});
	
	// Group by
	$("input[name='group_by']").click(function() {
		const value = $("input[name='group_by']:checked").val();
		synchronizeGroupbyBar();
		setGroupBy(value);
	});
}

function synchronizeHighlightsBar(criteria) {
	$("#highlights_bar").empty();
	if (criteria.length > 0) {
		let html = "<span class='filter_label'>Highlights:</span>";
		for (let i = 0; i < criteria.length; i++) {
			let propName = criteria[i].name;
			let propValues = criteria[i].values;
			for (let j = 0; j < propValues.length; j++) {
				let propValue = propValues[j];
				html += "<span class='highlight_button'><img src='/images/close.png' height='20' width='20' onclick=\"removeHighlightFromDialog('";
				html += propName;
				html += "', '";
				html += propValue;
				html += "');\"/>&nbsp;";
				html += propName;
				html += " = ";
				html += propValue;
				html += "</span>";
			}
		}
		$("#highlights_bar").html(html);
	}
}

function synchronizeFiltersBar(criteria) {
	$("#filters_bar").empty();
	if (criteria.length > 0) {
		let html = "<span class='filter_label'>Filters:</span>";
		for (let i = 0; i < criteria.length; i++) {
			let propName = criteria[i].name;
			let propValues = criteria[i].values;
			for (let j = 0; j < propValues.length; j++) {
				let propValue = propValues[j];
				html += "<span class='filter_button'><img src='/images/close.png' height='20' width='20' onclick=\"removeFilterFromDialog('";
				html += propName;
				html += "', '";
				html += propValue;
				html += "');\"/>&nbsp;";
				html += propName;
				html += " = ";
				html += propValue;
				html += "</span>";
			}
		}
		$("#filters_bar").html(html);
	}
}

function synchronizeGroupbyBar() {
	$("#groupby_bar").empty();
	const value = $("input[name='group_by']:checked").val();
	if (value != "none") {
		let html =
			"<span class='filter_label'>Group by:</span>" +
			"<span class='groupby_button'>" +
			"<img src='/images/close.png' height='20' width='20' onclick='removeGroupBy()'/>" +
			value +
			"</span>";
		$("#groupby_bar").html(html);
	}
}

function removeGroupBy() {
	$("input[name='group_by'][value='none']").prop("checked", true);
	$("#groupby_bar").empty();
	setGroupBy("none");
}

function removeHighlightFromDialog(propName, propValue) {
	if (propName === subjectFieldName) {
		const p = highlightedSubjects.indexOf(propValue);
		highlightedSubjects.splice(p, 1);
	}
	else if (propName != recordIdFieldName) {
		const values = $("select[name='highlight-" + propName + "']").val();
		const p = values.indexOf(propValue);
		values.splice(p, 1);
		$("select[name='highlight-" + propName + "']").val(values);
	}
	onHighlightChange();
}

function removeFilterFromDialog(propName, propValue) {
	const values = $("select[name='filter-" + propName + "']").val();
	const p = values.indexOf(propValue);
	values.splice(p, 1);
	$("select[name='filter-" + propName + "']").val(values);
	onFilterChange();
}

function onHighlightChange() {
	const sites = $("#highlight_site").val();
	const phases = $("#highlight_phase").val();
	const criteria = [];
	if (sites.length > 0) {
		let criterion = {};
		criterion.name = siteFieldName;
		criterion.values = sites;
		criteria.push(criterion);
	}
	if (phases.length > 0) {
		let criterion = {};
		criterion.name = phaseFieldName;
		criterion.values = phases;
		criteria.push(criterion);
	}
	if (highlightedSubjects.length > 0) {
		let criterion = {};
		criterion.name = subjectFieldName;
		criterion.values = highlightedSubjects;
		criteria.push(criterion);
	}
	setHighlightCriteria(criteria);
	synchronizeHighlightsBar(criteria);
}

function onFilterChange() {
	const sites = $("#filter_site").val();
	const phases = $("#filter_phase").val();
	const criteria = [];
	if (sites.length > 0) {
		let criterion = {};
		criterion.name = siteFieldName;
		criterion.values = sites;
		criteria.push(criterion);
	}
	if (phases.length > 0) {
		let criterion = {};
		criterion.name = phaseFieldName;
		criterion.values = phases;
		criteria.push(criterion);
	}
	setFilterCriteria(criteria);
	synchronizeFiltersBar(criteria);
}

function isHighlighted(propName, propValue) {
	if (propName === subjectFieldName) {
		return highlightedSubjects.indexOf(propValue) >= 0;
	}
	const values = $("select[name='highlight-" + propName + "']").val();
	return values.indexOf(propValue) >= 0;
}

function onClickHighlightButton(propName, propValue) {
	const selector = "span[data-filter-prop-name='" + propName + "'][data-filter-prop-value='" + propValue + "']";
	const highlighted = $(selector).hasClass("highlight_button_small_clicked");
	if (highlighted) {
		$(selector).removeClass("highlight_button_small_clicked");
		$(selector).addClass("highlight_button_small");
		removeHighlightFromDialog(propName, propValue);
	}
	else {
		if (propName === subjectFieldName) {
			if (highlightedSubjects.indexOf(propValue) == -1) {
				highlightedSubjects.push(propValue);
			}
		}
		else {
			const values = $("select[name='highlight-" + propName + "']").val();
			if (values.indexOf(propValue) == -1) {
				values.push(propValue);
			}
			$("select[name='highlight-" + propName + "']").val(values);
		}
		$(selector).removeClass("highlight_button_small");
		$(selector).addClass("highlight_button_small_clicked");
	}
	onHighlightChange();
}

function initializeFilters() {
	$("#filter_site").click(function() {
		onFilterChange();
	});
	
	$("#filter_phase").click(function() {
		onFilterChange();
	});
	
	$("#filter_remove").click(function() {
		$("#filter_site").val("");
		$("#filter_phase").val("");
		onFilterChange();
	});
}

/**
 * Initialize dialogs
 */
function initializeDialogs() {
	
	// Dialog for displaying user-selected data
	$("#dialog_data").dialog({
		autoOpen: false,
		modal: true,
		minWidth: 800
	});
	
	// Plot contorol dialog
	$("#dialog_controls").dialog({
		autoOpen: false,
		modal: false,
		minWidth: 500,
		title: "Chart Options"
	});
	
	$("#dialog_charts").dialog({
		autoOpen: false,
		modal: true,
		minWidth: 700,
		title: "Other Charts"
	});
}

/**
 * Initialize widgets
 */
function initializeWidgets() {
	$("#img_boundary").tooltip();
	
	$("#button_options").click(function() {
		$("#dialog_controls").dialog("open");
	});
	
	$("#button_charts").click(function() {
		$("#dialog_charts").dialog("open");
	});
	
	$("#spinner_sd").spinner({
		step: 0.25,
		numberFormat: "n",
		stop: function() {
			const sd = $("#spinner_sd").val();
			setThresholdLines(sd);
		}
	});
	
	
	$("#button_show").click(function() {
		onClickShowData();
	});
	
	$("#text_second_field").click(function() {
		if ($("#text_second_field").val() == "Enter second variable") {
			$("#text_second_field").removeClass("input_hint");
			$("#text_second_field").val("");
			
			$("#text_second_field").autocomplete({
				minLength: 4,
				source: function(request, response) {
					const url = "/rest/admin/study/get_matching_field_instances?study_id="
						+ studyId + "\u0026term=" + request.term;
					$.get(url)
						.done(function(data) {
							response(data);
						})
						.fail(function() {
							console.log("Error");
						});
				}
			});
		}
	});
	
	$("#button_scatter").click(function() {
		const value = $("#text_second_field").val();
		const namePatt = new RegExp("\\(.*\\)");
		const fieldNameField = namePatt.exec(value);
		const datasetPatt = new RegExp("\\[.*\\]");
		const datasetField = datasetPatt.exec(value);

		// TODO: Add logic to deal with invalid variables
		const fieldName2 = fieldNameField[0].substring(1, fieldNameField[0].length - 1);
		const datasetName2 = datasetField[0].substring(1, datasetField[0].length - 1);
		
		const url = "/anomaly/bivariate_scatter?" +
				"field_name_1=" + fieldName1 + "\u0026" +
				"dataset_name_1=" + datasetName + "\u0026" +
				"field_name_2=" + fieldName2 + "\u0026" +
				"dataset_name_2=" + datasetName2 + "\u0026" +
				"dataset_id=" + datasetId;
		window.location = url;
	});
}

function onClickShowData() {
	const data = getSelectedData();
	let varNames = Object.keys(data[0]);
	varNames.splice(varNames.indexOf("anomaly_id"), 1);
	varNames.splice(varNames.indexOf("is_an_issue"), 1);
	varNames.splice(varNames.indexOf("query_candidate_id"), 1);
	varNames.splice(varNames.indexOf("__y__"), 1);
	let html = "<tr>";
	for (var i in varNames) {
		html += "<th>";
		html += varNames[i];
		html += "</th>"
	}
	for (var i in data) {
		html += "<tr>";
		for (var j in varNames) {
			let varName = varNames[j];
			let buttonId = "button-" + varName + "---" + data[i][varName];
			html += "<td>";
			if (varName === subjectFieldName || varName === siteFieldName || varName === phaseFieldName) {
				let className = "highlight_button_small";
				if (isHighlighted(varName, data[i][varName])) {
					className = "highlight_button_small_clicked";
				}
				html += "<span title='Click to highlight data' class='";
				html += className;
				html += "' data-filter-prop-name='";
				html += varName;
				html += "' data-filter-prop-value='";
				html += data[i][varName];
				html += "'><img src='/images/highlighter.png' height='15' width='15' onclick='onClickHighlightButton(\"";
				html += varName;
				html += "\", \"";
				html += data[i][varName];
				html += "\");'/></span>&nbsp;&nbsp;";
			}
			html += data[i][varName];
			html += "</td>";
		}
		html += "</tr>";
	}
	html += "</tr>";
	$("#dialog_data_table").html(html);
	$("#dialog_data").dialog("open");
}

function onClickQuery() {
	const data = getSelectedData();
	let anomalyIds = "anomaly_ids=";
	for (let i = 0; i < data.length; i++) {
		if (i > 0) {
			anomalyIds += ","
		}
		anomalyIds += data[i].anomaly_id;
	}
	const url = "/rest/query/add";
	$.post(url, anomalyIds)
		.done(function() {
			switchClass(data, "outlier", "under-query");
			switchClass(data, "outlier-selected", "under-query-selected");
		})
		.fail(function() {
			console.log("Error");
		});
}

function onClickNotIssue() {
	const data = getSelectedData();
	let anomalyIds = "anomaly_ids=";
	for (let i = 0; i < data.length; i++) {
		if (i > 0) {
			anomalyIds += ","
		}
		anomalyIds += data[i].anomaly_id;
	}
	const url = "/rest/anomaly/not_an_issue";
	$.post(url, anomalyIds)
		.done(function() {
			switchClass(data, "outlier", "inlier");
			switchClass(data, "outlier-selected", "inlier-selected");
		})
		.fail(function() {
			console.log("Error");
		});
}

$(function() {
	initializeDialogs();
	initializeWidgets();
	initializeFilters();
	initializeOptionsDialogFields();
	
	$("#tabs").tabs();
	
	let criteria = null;
	if (recordId != -1) {
		criteria = new Array();
		const criterion = new Object();
		criterion.name = recordIdFieldName;
		criterion.values = new Array();
		criterion.values.push(recordId);
		criteria.push(criterion);
		setHighlightCriteria(criteria);
	}
	
	renderBeeswarm(url, fieldName, recordIdFieldName, mean, sd, numSd, function(itemsAreSelected) {
		if (itemsAreSelected) {
			$("#button_show").prop("disabled", false);
			$("#select_issue").prop("disabled", false);
			$("#button_status").prop("disabled", false);
		}
		else {
			$("#button_show").prop("disabled", true);
			$("#select_issue").prop("disabled", true);
			$("#button_status").prop("disabled", true);
		}
	});
	
	if (recordId != -1) {
		synchronizeHighlightsBar(criteria);
	}
	
	$.contextMenu({
		selector: "svg",
		items: {
			"options": {name: "Chart Options", icon: "fa-cogs"},
			"separator_1": {type: "cm_separator"},
			"data": {name: "Show selected data", icon: "fa-table"},
			"query": {name: "Add selected to Query List", icon: "fa-list"},
			"notissue": {name: "Label selected 'Not an Issue'", icon: "fa-check-circle"},
			"separator_2": {type: "cm_separator"},
			"scatter": {name: "Plot relationship with second variable", icon: "fa-chart-line"},
			"longitudinal": {name: "Plot data longitudinally"},
			"violin": {name: "Plot with additional variables"}
		},
		callback: function(key, options) {
			if (key == "options") {
				$("#dialog_controls").dialog("open");
			}
			else if (key == "scatter") {
				$("#dialog_charts").dialog("open");
			}
			else if (key == "data") {
				onClickShowData();
			}
			else if (key == "query") {
				onClickQuery();
			}
			else if (key == "notissue") {
				onClickNotIssue();
			}
		}
	});
});