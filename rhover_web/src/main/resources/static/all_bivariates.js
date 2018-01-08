$(function() {
	$(".cb_defaults").click(function() {
		biCheckId = this.dataset.bivariate_check_id;
		divId = "div_" + biCheckId;
		if (this.checked) {
			$("#" + divId).remove();
		}
		else {
			let html =
				"<div id='" + divId + "'>" +
				"<input type='text' size='3' title='Standard deviations from the regression line'/> STDEV from reg. line<br/>" +
				"<input type='text' size='3' title='Standard deviations from mean distance to neighbors'/> STDEV below mean density" +
				"</div>";
			tdId = "#td_" + biCheckId;
			$(tdId).append(html);
		}
		$("#td_" + biCheckId).prop("edited");
	});
	
	$(".cb_select").click(function() {
		if ($(".cb_select:checked").length == 0) {
			$("#button_delete").prop("disabled", "true");
		}
		else {
			$("#button_delete").removeAttr("disabled");
		}
	});
	
	$("#button_delete").click(function() {
		let checkIds = "";
		let count = 0;
		$(".cb_select:checked").each(function() {
			count++;
			if (count > 1) {
				checkIds += ",";
			}
			checkIds += this.dataset.bivariate_check_id;
		});
		const url = "/rest/admin/study/delete_bivariate_checks?check_ids=" + checkIds;
		$.post(url)
			.done(function() {
				$(".cb_select:checked").parent().parent().remove();
			})
			.fail(function() {
				console.log("Error");
			})
	});
});