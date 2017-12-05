

function BeeswarmRenderer() {
	
}

BeeswarmRenderer.prototype.renderDataFromUrl = function() {
	$.get(url)
	.done(function(data) {
		console.log(data);
	})
	.fail(function() {
		console.log("Error");
	});
}
