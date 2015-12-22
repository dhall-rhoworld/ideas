			// Add vertically-stacked data "tracks" for subjects
			sp._chartWidth = width - sp.margin.left - sp.margin.right;
			var track = chart.selectAll("g")
					.data(data)
				.enter().append("g")
					.attr("transform", function(subject, i) {
						return "translate(" + sp.margin.left + ", " + sp._trackY[i] + ")";
					})
					.attr("id", function(subject) {return "track-" + subject.subject_id;});
			track.append("line")
				.attr("x1", 0)
				.attr("y1", 0)
				.attr("x2", sp._chartWidth)
				.attr("y2", 0);
			track.append("text")
				.text(function(subjectData) {return subjectData.subject_id;})
				.attr("x", -sp.size.dataPoint)
				.attr("y", 5)
				.attr("class", "subject-label");
				
			// Add select-all boxes for each specimen type and for all specimen types
			track.append("rect")
				.attr("x", ((-specimenTypes.length - 1) * (sp.size.selectBox + sp.padding.selectBox) - sp.size.dataPoint / 2))
				.attr("y", 10)
				.attr("width", sp.size.selectBox)
				.attr("height", sp.size.selectBox)
				.on("click", function() {sp._toggleTrack(this);})
				.attr("class", "all-specimen-types");
			selectAllGroup = track.append("g")
				.attr("transform", "translate(" + ((-specimenTypes.length) * (sp.size.selectBox + sp.padding.selectBox) - sp.size.dataPoint / 2) + ", 10)")
				.attr("class", "select-all")
				.attr("id", "select-all-" + track.datum().subject_id);
			selectAllGroup.selectAll("rect")
				.data(specimenTypes)
				.enter()
				.append("rect")
				.attr("x", function(d, i) {return i * (sp.size.selectBox + sp.padding.selectBox);})
				.attr("y", 0)
				.attr("width", sp.size.selectBox)
				.attr("height", sp.size.selectBox)
				.on("click", function() {sp._toggleTrack(this);})
				.attr("class", function(d) {return d;});
			
			// Add data "points" for each visit to the tracks
			var visit = track.selectAll("g.visit")
				.data(function(subjectData) {return subjectData.visits;})
				.enter().append("g")
				.attr("transform", function(visit) {return "translate(0, " + (-sp.size.dataPoint * visit.specimens.length / 2) + ")"})
				.attr("class", "visit");
			visit.selectAll("rect")
				.data(function(visit) {return visit.specimens;})
				.enter().append("rect")
				.attr("x", 0)
				.attr("y", function(specimen, i) {return i * (sp.size.dataPoint + sp.padding.selectBox);})
				.attr("height", sp.size.dataPoint)
				.attr("width", sp.size.dataPoint)
				.on("click", function() {sp._toggleSelected(this);})
				.attr("class", function(specimen) {return specimen.type;});
			track.selectAll("g.visit").filter(function(visit) {return visit.type == "surgery";})
				.append("text")
					.text("T")
					.attr("class", "visit-label")
					.attr("x", (sp.size.dataPoint / 2))
					.attr("y", function(visit) {return visit.specimens.length * (sp.size.dataPoint + sp.padding.selectBox) + 8;});