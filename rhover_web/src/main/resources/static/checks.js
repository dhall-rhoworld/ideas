const dataTypeMappings = {
	Double : "Continuous Variables",
	Integer : "Integer Variables",
	String : "Character Variables",
	Date : "Date Variables",
	MixedType : "Mixed Type Variables",
	UnknownType : "Unknown Type Variables"
};

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

function onChangeDataset() {
	const datasetId = $("#select_dataset").val();
	const url = "/rest/admin/study/fields?dataset_id=" + datasetId;
	$.get(url)
		.done(function(data) {
			console.log(data);
			let html = "<option value='-1'>Dataset Defaults</html>";
			for (let i = 0; i < data.length; i++) {
				const group = data[i];
				const dataType = dataTypeMappings[group.dataType];
				html += "<optgroup label='---" + dataType + "---'>"
				for (let j = 0; j < group.fieldDtos.length; j++) {
					field = group.fieldDtos[j];
					let fieldName = field.fieldName;
					if (field.isIdentifying) {
						fieldName = "[ID] " + field.fieldName;
					}
					html += "<option>" + fieldName + "</option>"
				}
				html += "</optgroup>"
			}
			$("#select_field").html(html);
		})
		.fail(function(error) {
			console.log("Error");
		});
}

function onChangeField() {
	console.log("Changing field");
}

/**
 * jQuery init function
 */
$(function() {

	$("#button_save").click(function() {
		onSave();
	});
	
	$("#select_dataset").change(function() {
		onChangeDataset();
	});
	
	$("#select_field").change(function() {
		onChangeField();
	});
	
	fetchChecks();
	
});