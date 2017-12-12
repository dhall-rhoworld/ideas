$(function() {
	
	$("#dialog").dialog({
		autoOpen: false,
		modal: true,
		minWidth: 600,
		buttons: [
			{
				text: "Cancel",
				click: function() {
					$(this).dialog("close");
				}
			},
			{
				text: "OK",
				click: function() {
					const params = $("form").serialize();
					const url = "/rest/admin/study/save_bivariates_correlated";
					$.post(url, params)
						.done(function() {
							$(".cb-target:checked").each(function() {
								let fieldInstanceId = this.dataset.field_instance_id;
								console.log(this);
								console.log(fieldInstanceId);
								if (!targetNode.data.checkFieldInstanceIds.includes(fieldInstanceId)) {
									targetNode.data.checkFieldInstanceIds.push(fieldInstanceId);
								}
							});
						})
						.fail(function() {
							console.log("Error");
						})
						.always(function() {
							$("#dialog").dialog("close");
						});
				}
			}
		]
	});
	
	let targetNode = null;

	var diameter = 800,
	    radius = diameter / 2,
	    innerRadius = radius - 150;
	
	var cluster = d3.cluster()
	    .size([360, innerRadius]);
	
	var line = d3.radialLine()
		.curve(d3.curveBundle.beta(0.85))
		.radius(function(d) { return d.y; })
		.angle(function(d) { return d.x / 180 * Math.PI; });
	
	var svg = d3.select("#chart").append("svg")
		.attr("width", diameter)
		.attr("height", diameter)
		.append("g")
		.attr("transform", "translate(" + radius + "," + radius + ")");
	
	var link = svg.append("g").selectAll(".link"),
		node = svg.append("g").selectAll(".node");
	
	const url = "/rest/admin/study/correlations?study_id=" + studyId;
	//const url = "/flare.json";
	
	d3.json(url, function(error, data) {
		  if (error) {
			  throw error;
		  }
		  //console.log(data);
		  const root = createHierarchy(data).sum(function(d) {
			  let n = 1;
			  if (d.children) {
				 n += d.children.length; 
			  }
			  return n;
		  });
		  cluster(root);
		  
		  link = link
		    .data(createLinks(root.leaves()))
		    .enter().append("path")
		      .each(function(d) { d.source = d[0], d.target = d[d.length - 1]; })
		      .attr("class", "link")
		      .attr("d", line);
		  
		  node = node
		    .data(root.leaves())
		    .enter().append("text")
		      .attr("class", "node")
		      .attr("dy", "0.31em")
		      .attr("transform", function(d) { return "rotate(" + (d.x - 90) + ")translate(" + (d.y + 8) + ",0)" + (d.x < 180 ? "" : "rotate(180)"); })
		      .attr("text-anchor", function(d) { return d.x < 180 ? "start" : "end"; })
		      .text(function(d) {return d.data.name; })
		      .on("mouseover", mouseovered)
		      .on("mouseout", mouseouted)
		      .on("click", handleClick)
		      .classed("has-checks", function(d) {return d.data.checkFieldInstanceIds.length > 0;})
	});
	
	function createHierarchy(datasets) {
		const root = {
			name: "Datasets",
			children: []
		};
		for (let i = 0; i < datasets.length; i++) {
			let dataset = datasets[i];
			let node = {
				name: dataset.datasetName,
				children: []
			}
			root.children.push(node);
			for (let j = 0; j < dataset.fields.length; j++) {
				let field = dataset.fields[j];
				let subNode = {
					name: field.fieldName,
					label: field.fieldLabel,
					fieldInstanceId: field.fieldInstanceId,
					correlatedFieldInstanceIds: field.correlatedFieldInstanceIds,
					parentDatasetId: dataset.datasetId,
					checkFieldInstanceIds: field.checkFieldInstanceIds,
					correlatedFields: []
				}
				node.children.push(subNode);
			}
		}
		return d3.hierarchy(root);
	}
	
	function createLinks(nodes) {
		const map = {};
		let correlates = [];
		
		nodes.forEach(function(d) {
			map[d.data.fieldInstanceId] = d;
		});
		
		nodes.forEach(function(d) {
			if (d.data.correlatedFieldInstanceIds) {
				d.data.correlatedFieldInstanceIds.forEach(function(i) {
					correlates.push(map[d.data.fieldInstanceId].path(map[i]));
					d.data.correlatedFields.push(map[i]);
				});
			}
		});
		return correlates;
	}
	
	function mouseovered(d) {
	  node
	      .each(function(n) { n.target = n.source = false; });

	  link
	      .classed("link--target", function(l) { if (l.target === d) return l.source.source = true; })
	      .classed("link--source", function(l) { if (l.source === d) return l.target.target = true; })
	    .filter(function(l) { return l.target === d || l.source === d; })
	      .raise();

	  node
	      .classed("node--target", function(n) { return n.target; })
	      .classed("node--source", function(n) { return n.source; });
	}

	function mouseouted(d) {
	  link
	      .classed("link--target", false)
	      .classed("link--source", false);

	  node
	      .classed("node--target", false)
	      .classed("node--source", false);
	}
	
	function handleClick(d) {
		targetNode = d;
		$("#h_field").html(d.data.name);
		$("#source_field_instance_id").val(d.data.fieldInstanceId);
		let html = "";
		d.data.correlatedFields.forEach(function(field) {
			html += "<div>";
			html += "<input type='checkbox' class='cb-target' data-field_instance_id='" + field.data.fieldInstanceId + "' name='target-"
				+ field.data.fieldInstanceId + "'";
			if (d.data.checkFieldInstanceIds.includes(field.data.fieldInstanceId)) {
				html += " checked";
			}
			html += "/>";
			html += field.data.label;
			html += "</div>";
		});
		$("#fields_2").html(html);
		$("#dialog").dialog("open");
	}
	
});
