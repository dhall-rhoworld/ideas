
// Variables to help keep track of form state
let xIsSelected = false;
let yIsSelected = false;

$(function() {
	
	// ------------------
	// --- X VARIABLE ---
	// ------------------
	
	// --- BROWSE X VARIABLE ---
	
	//
	// User selects X variable dataset
	//
	$("#select_dataset_x").change(function() {
		const datasetId = $("#select_dataset_x").val();
		console.log(datasetId);
		
		// Special case: user selects no dataset
		if (datasetId == 0) {
			let html = "<option value='0'>---Select Variable---</option>";
			$("#select_variable_x").html(html);
			xIsSelected = false;
			setSubmitButtonState();
			return;
		}
		
		// Fetch variables in selected dataset
		const url = "/rest/admin/study/fields?dataset_id=" + datasetId;
		$.get(url)
		
			// REST call is successful
			.done(function(fieldGroups) {
				
				// Populate x variable drop-down selector
				let html = "<option value='0'>---Select Variable---</option>";
				for (let i = 0; i < fieldGroups.length; i++) {
					
					// Add new optgroup for each data type.  Options are disabled for
					// all non-numeric fields
					fieldGroup = fieldGroups[i];
					let enabled = (fieldGroup.dataType == "CONTINUOUS" || fieldGroup.dataType == "INTEGER");
					html += "<optgroup label='" + fieldGroup.dataType + " VARIABLES'>";
					
					// Add an option for each field
					let fields = fieldGroup.fieldDtos;
					for (let j = 0; j < fields.length; j++) {
						let field = fields[j];
						html += "<option value='" + field.fieldInstanceId + "'"
						if (!enabled) {
							html += " disabled";
						}
						html += ">";
						html += field.fieldLabel;
						html += "</option>";
					}
					html += "</optgroup>";
				}
				$("#select_variable_x").html(html);
			})
			
			// REST error
			.fail(function() {
				console.log("Error");
			});
	});
	
	//
	// User selects X variable
	//
	$("#select_variable_x").change(function() {
		$("#tr_search_x").empty();
		if ($("#select_variable_x").val() == 0) {
			xIsSelected = false;
			$("#button_correlated").prop("disabled", "true");
		}
		else {
			xIsSelected = true;
			$("#button_correlated").removeAttr("disabled");
		}
		setSubmitButtonState();
	});
	
	// --- SEARCH X VARIABLE ---
	
	//
	// Configure jqueryui autocomplete for X variable search box
	//
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
	
	//
	// User clicks "OK" button for X variable search box
	//
	// NOTE: Prior to this button being clicked, the autocomplete should have
	// populated the corresponding text input field with a string formatted as such:
	//
	//      "VARIABLE_LABEL (VARIABLE_NAME) [DATASET]"
	//
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
		
		// Case: Text in form box not formatted as required
		if (nameField == null || datasetField == null) {
			$("#message_error").html("Unknown variable");
			$("#dialog_error").dialog("open");
			return;
		}
		
		// Case: Correct formatting
		const name = nameField[0].substring(1, nameField[0].length - 1);
		const dataset = datasetField[0].substring(1, datasetField[0].length - 1);
		
		// Fetch variable instance ID
		const url = "/rest/admin/study/fetch_variable_instance_id?"
			+ "study_id=" + studyId
			+ "&variable_name=" + name
			+ "&dataset_name=" + dataset;
		$.get(url)
		
			// REST call successful
			.done(function(data) {
				let html = "<input type='hidden' name='variable_x' value='" + data + "'/>"
				$("form").append(html);
				$("#tr_browse_x").remove();
				$("#button_search_x").prop("disabled", "true");
				$("#text_search_x").prop("disabled", "true");
				$("#button_correlated").removeAttr("disabled");
				xIsSelected = true;
				setSubmitButtonState();
			})
			
			// REST call failed
			.fail(function(data) {
				console.log("Error");
			});
	});
	
	// ------------------
	// --- Y VARIABLE ---
	// ------------------
	
	// --- BROWSE Y VARIABLES ---
	
	//
	// User selects a Y variable dataset
	//
	$("#select_dataset_y").change(function() {
		const datasetId = $("#select_dataset_y").val();
		
		// Special case: User selects no dataset
		if (datasetId == 0) {
			$("#td_variable_y").empty();
			yIsSelected = false;
			setSubmitButtonState();
			return;
		}
		
		// Fetch fields associated with selected dataset
		const url = "/rest/admin/study/fields?dataset_id=" + datasetId;
		$.get(url)
		
			// Successful REST call
			.done(function(fieldGroups) {
				
				// Update state of submit button
				yIsSelected = true;
				setSubmitButtonState();
				
				// Add new radio buttons and select field
				let html =
					"<input type='radio' name='y_type' id='y_type_continuous' value='continuous' checked/>&nbsp; All continuous variables<br/>" +
					"<input type='radio' name='y_type' id='y_type_numeric' value='numeric'/>&nbsp; All numeric variables<br/>" +
					"<input type='radio' name='y_type' id='y_type_custom' value='custom'/>&nbsp; Select individual variables<br/><br/>" +
					"<select name='variable_y' id='select_variable_y' multiple size='6' disabled>";
				for (let i = 0; i < fieldGroups.length; i++) {
					fieldGroup = fieldGroups[i];
					let enabled = (fieldGroup.dataType == "CONTINUOUS" || fieldGroup.dataType == "INTEGER");
					html += "<optgroup label='" + fieldGroup.dataType + " VARIABLES'>";
					let fields = fieldGroup.fieldDtos;
					for (let j = 0; j < fields.length; j++) {
						let field = fields[j];
						html += "<option value='" + field.fieldInstanceId + "'"
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
				
				// Add event handlers for new radio buttons and select field
				$("#td_variable_y").html(html);
				$("#y_type_continuous").click(function() {
					yIsSelected = true;
					$("#select_variable_y").prop("disabled", "true");
					setSubmitButtonState();
				});
				$("#y_type_numeric").click(function() {
					yIsSelected = true;
					$("#select_variable_y").prop("disabled", "true");
					setSubmitButtonState();
				});
				$("#y_type_custom").click(function() {
					yIsSelected = false;
					$("#select_variable_y").removeAttr("disabled");
					setSubmitButtonState();
				});
				$("#select_variable_y").change(function() {
					if ($("#select_variable_y").val().length > 0) {
						yIsSelected = true;
					}
					else {
						yIsSelected = false;
					}
					setSubmitButtonState();
				});
				
				// Remove UI controls for browsing correlated variables and searching
				$("#tr_correlated").empty();
				$("#tr_search_y").empty();
			})
			
			// Failed REST call
			.fail(function() {
				console.log("Error");
			});
	});
	
	// --- BROWSE CORRELATED Y VARIABLE(S)
	
	//
	// User clicks Show Correlated Variables button
	//
	$("#button_correlated").click(function() {
		
		// Fetch variables correlated with X variable and show progress popup
		const url = "/rest/admin/study/find_correlated_fields?field_instance_id="
			+ $("[name='variable_x']").val();
		$("#dialog_progress").html("<img src='/images/progress_wheel.gif'/>");
		$("#dialog_progress").dialog("open");
		$.get(url)
		
			// Succesful REST call
			.done(function(data) {
				//console.log(data);
				
				// Remove UI controls for browsing and searching
				$("#tr_browse_y").remove();
				$("#tr_search_y").remove();
				
				// Add new multi-select box
				let html = "<select multiple size='6' id='select_correlated' name='variable_y'>";
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
				
				// Add event handler for new multi-select box
				$("#select_correlated").change(function() {
					if ($("#select_correlated").val().length > 0) {
						yIsSelected = true;
					}
					else {
						yIsSelected = false;
					}
					setSubmitButtonState();
				});
			})
			
			// Failed REST call
			.fail(function() {
				console.log("Error");
			})
			
			// Close progress popup
			.always(function() {
				$("#dialog_progress").dialog("close");
			});
	});
	
	// --- SEARCH Y VARIABLE ---
	
	//
	// Configure search box jqueryui auto-complete
	//
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
	
	//
	// User clicks OK button associated with Y variable search box.
	//
	// NOTE: Prior to this button being clicked, the autocomplete should have
	// populated the corresponding text input field with a string formatted as such:
	//
	//      "VARIABLE_LABEL (VARIABLE_NAME) [DATASET]"
	//
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
				let html = "<input type='hidden' name='variable_y' value='" + data + "'/>"
				$("form").append(html);
				$("#tr_browse_y").remove();
				$("#tr_correlated").remove();
				$("#button_search_y").prop("disabled", "true");
				$("#text_search_y").prop("disabled", "true");
				yIsSelected = true;
				setSubmitButtonState();
			})
			.fail(function(data) {
				console.log("Error");
			});
	});
	
	// -----------------------------
	// --- DATA CHECK PARAMETERS ---
	// -----------------------------
	
	//
	// User clicks "Use study-level defaults" checkbox
	//
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
	
	//
	// Event handler for parameter input text fields
	//
	$(".input-params").on("keypress keyup blur", function(event) {
		let numericOnly = $(this).val().replace(/[^0-9.]/g,"");
		$(this).val(numericOnly);
		setSubmitButtonState();
	});
	
	//
	// Tests whether data check parameter form fields are complete
	//
	function parametersComplete() {
		let complete = $("#cb_defaults").is(":checked");
		if (!complete) {
			complete = $("#text_sd-residual").val().trim().length != 0 && $("#text_sd-density").val().trim().length != 0;
		}
		return complete;
	}
	
	//
	// Activates/de-activates submit button depending on the completeness of
	// form fields
	//
	function setSubmitButtonState() {
		const disabled = !(xIsSelected && yIsSelected && parametersComplete());
		if (disabled) {
			$("#button_submit").prop("disabled", true);
		}
		else {
			$("#button_submit").removeAttr("disabled");
		}
	}
		
	// ---------------------------------
	// --- ADDITIONAL INITIALIZATION ---
	// ---------------------------------
	
	//
	// Initialize jqueryui error dialog
	//
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
	
	//
	// Initialize jqueryui progress dialog
	//
	$("#dialog_progress").dialog({
		autoOpen: false,
		modal: true,
		resizable: false,
		title: "Finding Correlated Variables"
	});
	
});