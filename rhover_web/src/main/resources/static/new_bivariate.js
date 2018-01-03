$(function() {
	$("#dataset_x").change(function() {
		const datasetId = $("#dataset_x").val();
		if (datasetId == 0) {
			$("#td_variable_x").empty();
			return;
		}
		const url = "/rest/admin/study/fields?dataset_id=" + datasetId;
		$.get(url)
			.done(function(fieldGroups) {
				let html = "<option value='0'>---Select Variable---</option>";
				for (let i = 0; i < fieldGroups.length; i++) {
					fieldGroup = fieldGroups[i];
					let enabled = (fieldGroup.dataType == "CONTINUOUS" || fieldGroup.dataType == "INTEGER");
					html += "<optgroup label='" + fieldGroup.dataType + " VARIABLES'>";
					let fields = fieldGroup.fieldDtos;
					for (let j = 0; j < fields.length; j++) {
						let field = fields[j];
						html += "<option value='" + field.fieldId + "'"
						if (!enabled) {
							html += " disabled";
						}
						html += ">";
						html += field.fieldLabel;
						html += "</option>";
					}
					html += "</optgroup>";
				}
				$("#variable_x").html(html);
			})
			.fail(function() {
				console.log("Error");
			});
	});
	
	$("#variable_x").change(function() {
		$("#tr_search_x").empty();
		setSubmitButtonState();
	});
	
	$("#dataset_y").change(function() {
		const datasetId = $("#dataset_y").val();
		if (datasetId == 0) {
			$("#td_variable_y").empty();
			return;
		}
		const url = "/rest/admin/study/fields?dataset_id=" + datasetId;
		$.get(url)
			.done(function(fieldGroups) {
				let html =
					"<input type='radio' name='option' value='continuous' checked/>&nbsp; All continuous variables<br/>" +
					"<input type='radio' name='option' value='integer'/>&nbsp; All numeric variables<br/>" +
					"<input type='radio' name='option' value='custom'/>&nbsp; Select individual variables<br/><br/>" +
					"<select name='variable_y' id='variable_y' multiple size='6' disabled>";
				for (let i = 0; i < fieldGroups.length; i++) {
					fieldGroup = fieldGroups[i];
					let enabled = (fieldGroup.dataType == "CONTINUOUS" || fieldGroup.dataType == "INTEGER");
					html += "<optgroup label='" + fieldGroup.dataType + " VARIABLES'>";
					let fields = fieldGroup.fieldDtos;
					for (let j = 0; j < fields.length; j++) {
						let field = fields[j];
						html += "<option value='" + field.fieldId + "'"
						if (!enabled) {
							html += " disabled";
						}
						html += ">";
						html += field.fieldLabel;
						html += "</option>";
					}
					html += "</optgroup>";
				}
				html += "</select>";
				$("#td_variable_y").html(html);
				$("input[name='option']").click(function() {
					setMultiselectState();
					setSubmitButtonState();
				});
				$("#variable_y").change(function() {
					setSubmitButtonState();
				});
				$("#tr_correlated").empty();
				$("#tr_search_y").empty();
				setSubmitButtonState();
			})
			.fail(function() {
				console.log("Error");
			});
	});
	
	function setMultiselectState() {
		const option = $("input[name='option']:checked").val();
		if (option == 'custom') {
			$("#variable_y").removeAttr("disabled");
		}
		else {
			$("#variable_y").prop("disabled", "true");
		}
	}
	
	$("#cb_defaults").click(function() {
		const checked = this.checked;
		if (checked) {
			$(".dataset-params").addClass("deactivated");
			$(".input-params").prop("disabled", "true");
		}
		else {
			$(".dataset-params").removeClass("deactivated");
			$(".input-params").removeAttr("disabled");
		}
		setSubmitButtonState();
	});
	
	$(".input-params").on("keypress keyup blur", function(event) {
		let numericOnly = $(this).val().replace(/[^0-9.]/g,"");
		$(this).val(numericOnly);
		setSubmitButtonState();
	});
	
	$("#text_search_x").autocomplete({
		minLength: 4,
		source: function(request, response) {
			const url = "/rest/admin/study/get_matching_field_instances?study_id="
				+ studyId + "&term=" + request.term;
			$.get(url)
				.done(function(data) {
					response(data);
				})
				.fail(function() {
					console.log("Error");
				});
		}
	});
	
	$("#text_search_y").autocomplete({
		minLength: 4,
		source: function(request, response) {
			const url = "/rest/admin/study/get_matching_field_instances?study_id="
				+ studyId + "&term=" + request.term;
			$.get(url)
				.done(function(data) {
					response(data);
				})
				.fail(function() {
					console.log("Error");
				});
		}
	});
	
	// Prior to this button being clicked, the autocomplete should have
	// populated the corresponding text input field with a string formatted as such:
	// "VARIABLE_LABEL (VARIABLE_NAME) [DATASET]"
	$("#button_search_x").click(function() {
		
		// Extract variable name and dataset
		const varName = $("#text_search_x").val();
		if (varName.trim().length == 0) {
			return;
		}
		const namePatt = new RegExp("\\(.*\\)");
		const nameField = namePatt.exec(varName);
		const datasetPatt = new RegExp("\\[.*\\]");
		const datasetField = datasetPatt.exec(varName);
		if (nameField == null || datasetField == null) {
			$("#message_error").html("Unknown variable");
			$("#dialog_error").dialog("open");
			return;
		}
		const name = nameField[0].substring(1, nameField[0].length - 1);
		const dataset = datasetField[0].substring(1, datasetField[0].length - 1);
		
		// Fetch variable instance ID
		const url = "/rest/admin/study/fetch_variable_instance_id?"
			+ "study_id=" + studyId
			+ "&variable_name=" + name
			+ "&dataset_name=" + dataset;
		$.get(url)
			.done(function(data) {
				let html = "<input type='hidden' name='variable_instance_x' id='variable_instance_x' value='" + data + "'/>"
				$("form").append(html);
				$("#tr_browse_x").remove();
				$("#button_search_x").prop("disabled", "true");
				$("#text_search_x").prop("disabled", "true");
				$("#button_correlated").removeAttr("disabled");
			})
			.fail(function(data) {
				console.log("Error");
			});
	});
	
	// Prior to this button being clicked, the autocomplete should have
	// populated the corresponding text input field with a string formatted as such:
	// "VARIABLE_LABEL (VARIABLE_NAME) [DATASET]"
	$("#button_search_y").click(function() {
		
		// Extract variable name and dataset
		const varName = $("#text_search_y").val();
		if (varName.trim().length == 0) {
			return;
		}
		const namePatt = new RegExp("\\(.*\\)");
		const nameField = namePatt.exec(varName);
		const datasetPatt = new RegExp("\\[.*\\]");
		const datasetField = datasetPatt.exec(varName);
		if (nameField == null || datasetField == null) {
			$("#message_error").html("Unknown variable");
			$("#dialog_error").dialog("open");
			return;
		}
		const name = nameField[0].substring(1, nameField[0].length - 1);
		const dataset = datasetField[0].substring(1, datasetField[0].length - 1);
		
		// Fetch variable instance ID
		const url = "/rest/admin/study/fetch_variable_instance_id?"
			+ "study_id=" + studyId
			+ "&variable_name=" + name
			+ "&dataset_name=" + dataset;
		$.get(url)
			.done(function(data) {
				let html = "<input type='hidden' name='variable_instance_y' value='" + data + "'/>"
				$("form").append(html);
				$("#tr_browse_y").remove();
				$("#tr_correlated").remove();
				$("#button_search_y").prop("disabled", "true");
				$("#text_search_y").prop("disabled", "true");
			})
			.fail(function(data) {
				console.log("Error");
			});
	});
	
	$("#button_correlated").click(function() {
		const url = "/rest/admin/study/find_correlated_fields?field_instance_id="
			+ $("#variable_instance_x").val();
		$("#dialog_progress").dialog("open");
		$.get(url)
			.done(function(data) {
				console.log(data);
				$("#tr_browse_y").remove();
				$("#tr_search_y").remove();
				let html = "<select multiple size='6'>";
				for (let i = 0; i < data.length; i++) {
					field = data[i];
					html += "<option value='";
					html += field.fieldInstanceId;
					html += "'>";
					html += field.displayValue;
					html += "</option>";
				}
				html += "</select>";
				$("#td_correlated_label").html("Correlated<br/>Variables");
				$("#td_correlated").html(html);
			})
			.fail(function() {
				console.log("Error");
			})
			.always(function() {
				$("#dialog_progress").dialog("close");
			});
	});
	
	$("#dialog_error").dialog({
		autoOpen: false,
		modal: true,
		title: "Error",
		buttons: [
			{
				text: "OK",
				click: function() {
					$(this).dialog("close");
				}
			}
		]
	});
	
	$("#dialog_progress").dialog({
		autoOpen: false,
		modal: true,
		resizable: false,
		title: "Finding Correlated Variables"
	});
	
	function xAxisFieldsComplete() {
		return $("#variable_x").val() != 0;
	}
	
	function yAxisFieldsComplete() {
		if ($("#dataset_y").val() == 0) {
			return false;
		}
		let complete = true;
		const option = $("input[name='option']:checked").val();
		if (option == 'custom') {
			const values = $("#variable_y").val();
			if (values.length == 0) {
				complete = false;
			}
		}
		return complete;
	}
	
	function parametersComplete() {
		let complete = $("#cb_defaults").is(":checked");
		if (!complete) {
			complete = $("#text_sd-residual").val().trim().length != 0 && $("#text_sd-density").val().trim().length != 0;
		}
		return complete;
	}
	
	function setSubmitButtonState() {
		const disabled = !(xAxisFieldsComplete() && yAxisFieldsComplete() && parametersComplete());
		if (disabled) {
			$("#button_submit").prop("disabled", true);
		}
		else {
			$("#button_submit").removeAttr("disabled");
		}
	}
});