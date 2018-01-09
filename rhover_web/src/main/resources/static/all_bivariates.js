$(function() {
	
	/**
	 * Event handler for "Use defaults" checkbox
	 */
	$(".cb_defaults").click(function() {
		biCheckId = this.dataset.bivariate_check_id;
		divId = "div_" + biCheckId;
		
		// If checked, remove param value text input fields
		if (this.checked) {
			$("#" + divId).remove();
		}
		
		// If unchecked, add param value text input fields
		else {
			let html =
				"<div id='" + divId + "'>" +
				"<input type='text' size='3' title='Standard deviations from the regression line' class='sd-residual'/> STDEV from reg. line<br/>" +
				"<input type='text' size='3' title='Standard deviations from mean distance to neighbors' class='sd-density'/> STDEV below mean density" +
				"</div>";
			tdId = "#td_" + biCheckId;
			$(tdId).append(html);
		}
		
		// Tag as edited and enable save button
		$("#tr_" + biCheckId).addClass("edited");
		$("#button_save").removeAttr("disabled");
	});
	
	$(".cb_select").click(function() {
		if ($(".cb_select:checked").length == 0) {
			$("#button_delete").prop("disabled", "true");
		}
		else {
			$("#button_delete").removeAttr("disabled");
		}
	});
	
	/**
	 * Event handler for "Delete" button
	 */
	$("#button_delete").click(function() {
		
		// Gather check IDs into comma-delimited string
		let checkIds = "";
		let count = 0;
		$(".cb_select:checked").each(function() {
			count++;
			if (count > 1) {
				checkIds += ",";
			}
			checkIds += this.dataset.bivariate_check_id;
		});
		
		// Make REST call to deletion service
		const url = "/rest/admin/study/delete_bivariate_checks?check_ids=" + checkIds;
		$.post(url)
			.done(function() {
				$(".cb_select:checked").parent().parent().remove();
			})
			.fail(function() {
				console.log("Error");
			});
	});
	
	/**
	 * Event handler for param value text input fields
	 */
	$("input[type='text']").on("keypress keyup blur", function() {
		
		// Sqelch non-numeric input
		let numericOnly = $(this).val().replace(/[^0-9.]/g,"");
		$(this).val(numericOnly);
		
		// Tag parameter TR element as edited
		$(this).parent().parent().parent().addClass("edited");
		
		// Enable Save button
		$("#button_save").removeAttr("disabled");
	});
	
	/**
	 * Event handler for "Save Changes" button
	 */
	$("#button_save").click(function() {
		
		// Iterate over edited checks and construct JSON parameter object
		const jsonParams = [];
		$(".edited").each(function() {
			const jsonParam = {};
			jsonParam.bivariateCheckId = this.dataset.bivariate_check_id;
			
			// Get state of "Use defaults" checkbox
			const checked = $(this).find(".cb_defaults").prop("checked")
			jsonParam.useDefaults = checked;
			
			// If "Use defaults" unchecked, fetch parameter values
			if (!checked) {
				jsonParam.sdResidual = $(this).find(".sd-residual").val();
				jsonParam.sdDensity = $(this).find(".sd-density").val();
			}
			
			jsonParams.push(jsonParam);
		});
		
		// Make REST call to save changes
		const url = "/rest/admin/study/save_bivariate_check_edits";
		$.ajax(url, {
			contentType: "application/json",
			data: JSON.stringify(jsonParams),
			method: "POST"
		})
			.done(function() {
				
				// Remove edit tags
				$(".edited").removeClass("edited");
				
				// Disable save button
				$("#button_save").prop("disabled", "true");
			})
			.fail(function() {
				console.log("Error");
			});
	});
	
});