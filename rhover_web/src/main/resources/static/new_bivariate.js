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
				let html = "<select id='variable_x'><option value='0'>--- Select Variable ---</option>";
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
				$("#td_variable_x").html(html);
				$("#variable_x").change(function() {
					setSubmitButtonState();
				});
			})
			.fail(function() {
				console.log("Error");
			});
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