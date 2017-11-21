function populateFields(data) {
	for (let i = 0; i < data.length; i++) {
		const paramName = data[i].paramName;
		const paramValue = data[i].paramValue;
		const checkName = data[i].checkName;
		const inputName = checkName + "-" + paramName;
		const type = $("[name='" + inputName + "']").attr("type");
		if (type == "text") {
			$("[name='" + inputName + "']").val(paramValue);
		}
		else if (type == "radio") {
			$("input[name='" + inputName + "'][value='" + paramValue + "']").attr("checked", "checked");
		}
	}
}

function onSave() {
	const data = $("form").serialize();
	$.post("/rest/admin/study/save_check_params", data)
		.done(function(data) {
			console.log("Saved " + data + " parameters");
		})
		.fail(function() {
			console.log("Problem");
		});
}

function fetchChecks() {
	const studyId = $("#study_id").val();
	const datasetId = $("#select_dataset").val();
	const fieldId = $("#select_field").val();
	const url = "/rest/admin/study/check_params?study_id=" + studyId + "&dataset_id=" + datasetId + "&field_id=" + fieldId;
	$.get(url)
		.done(function(data) {
			populateFields(data);
		})
		.fail(function(error) {
			console.log(error);
		});
}

/**
 * jQuery init function
 */
$(function() {

	$("#button_save").click(function() {
		onSave();
	});
	fetchChecks();
	
});