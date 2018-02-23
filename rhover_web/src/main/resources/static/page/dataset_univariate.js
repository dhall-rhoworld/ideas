function activateFieldCheck(fieldId, activated) {
	const rowId = "#field_" + fieldId;
	const checkboxId = "#cb_custom_" + fieldId;
	const textId = "#text_custom_" + fieldId;
	if (activated) {
		$(rowId).removeClass("deactivated");
		$(checkboxId).prop("disabled", false);
		if ($(checkboxId).prop("checked")) {
			$(textId).prop("disabled", false);
		}
	}
	else {
		$(rowId).addClass("deactivated");
		$(checkboxId).prop("disabled", true);
		$(textId).prop("disabled", true);
	}
}

function activateAppropriateFields() {
	const typesToCheck = $("input[name='param_data_types']:checked").val();
	$(".field").each(function() {
		const dataType = this.dataset.data_type;
		const fieldId = this.id.substring(6, this.id.length);
		const skipId = "#cb_skip_" + fieldId;
		const skipped = $(skipId).prop("checked");
		if (dataType == "Double") {
			if (!skipped) {
				activateFieldCheck(fieldId, true);
			}
			$(skipId).prop("disabled", false);
		}
		else if (dataType == "Integer" && typesToCheck == "numeric") {
			if (!skipped) {
				activateFieldCheck(fieldId, true);
			}
			$(skipId).prop("disabled", false);
		}
		else {
			activateFieldCheck(fieldId, false);
			$(skipId).prop("disabled", true);
		}
	});
}

$(function() {
	
	// Add event handlers
	$("#select_dataset").change(function() {
		const studyId = $("#study_id").val();
		const datasetId = $("#select_dataset").val();
		const url = "/admin/study/dataset_univariate?study_id=" + studyId + "\u0026dataset_id=" + datasetId;
		document.location.href = url;
	});
	
	$("#radio_study_level").click(function() {
		const studyDataTypes = $("#study_data_types").val();
		const studyMinUnivariate = $("#study_min_univariate").val();
		const studySd = $("#study_sd").val();
		$("input[name='param_data_types'][value='" + studyDataTypes + "']").prop("checked", "true");
		$("#text_sd").val(studySd);
		$("#text_min_univariate").val(studyMinUnivariate);
		$(".dataset-params").addClass("deactivated");
		$(".dataset-params").prop("disabled", "true");
		activateAppropriateFields();
	});
	
	$("#radio_dataset_level").click(function() {
		$(".dataset-params").removeClass("deactivated");
		$(".dataset-params").removeAttr("disabled");
	});
	
	$(".cb_custom").change(function(event) {
		const id = event.target.id;
		const fieldId = id.substring(10, id.length);
		const textId = "#text_custom_" + fieldId;
		if (event.target.checked) {
			$(textId).removeAttr("disabled");
		}
		else {
			$(textId).prop("disabled", true);
		}
	});
	
	$(".cb_skip").change(function(event) {
		const id = event.target.id;
		const fieldId = id.substring(8, id.length);
		const activate = !event.target.checked;
		activateFieldCheck(fieldId, activate);
	});
	
	$("input[name='param_data_types']").click(function() {
		activateAppropriateFields();
	});
	
	if ($("input[name='use_study_defaults']:checked").val() == "false") {
		$(".dataset-params").removeClass("deactivated");
		$(".dataset-params").removeAttr("disabled");
	}
		
	activateAppropriateFields();

});