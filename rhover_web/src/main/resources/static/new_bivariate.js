$(function() {
	$("#dataset_x").change(function() {
		const datasetId = $("#dataset_x").val();
		if (datasetId == 0) {
			$("#variable_x").html("<option value='0'>---Select Variable</option>");
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
	
	$("#dataset_y").change(function() {
		const datasetId = $("#dataset_y").val();
		if (datasetId == 0) {
			$("#variable_y").html("<option value='0'>---Select Variable</option>");
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
				$("#variable_y").html(html);
			})
			.fail(function() {
				console.log("Error");
			});
	});
	
	$("#variable_x").change(function() {
		setSubmitButtonState();
	});
	
	$("#variable_y").change(function() {
		setSubmitButtonState();
	});
	
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
	
	function setSubmitButtonState() {
		const disabled =
			$("#variable_x").val() == 0
			|| $("#variable_y").val() == 0
			|| (
					!$("#cb_defaults").is(":checked")
					&& (
							$("#text_sd-residual").val().trim().length == 0
							|| $("#text_sd-density").val().trim().length == 0
						)
				);
		if (disabled) {
			$("#button_submit").prop("disabled", true);
		}
		else {
			$("#button_submit").removeAttr("disabled");
		}
	}
});