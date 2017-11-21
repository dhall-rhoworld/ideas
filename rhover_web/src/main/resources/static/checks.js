function populateFields(data) {
	for (let i = 0; i < data.length; i++) {
		const paramName = data[i].paramName;
		const paramValue = data[i].paramValue;
		const checkName = data[i].check.checkName;
		const type = $("[name='" + paramName + "']").attr("type");
		if (type == "text") {
			$("[name='" + paramName + "']").val(paramValue);
		}
		else if (type == "radio") {
			$("input[name='" + paramName + "'][value='" + paramValue + "']").attr("checked", "checked");
		}
	}
}

function fetchChecks() {
	const studyId = $("#study_id").val();
	const datasetId = $("#select_dataset").val();
	const fieldId = $("#select_field").val();
	const url = "/rest/admin/study/check_params?study_id=" + studyId + "&dataset_id=" + datasetId + "&field_id=" + fieldId;
	console.log(url);
	$.get(url)
		.done(function(data) {
			populateFields(data);
		})
		.fail(function() {
			console.log("Problem");
		});
}

/**
 * jQuery init function
 */
$(function() {

	fetchChecks();
	
});