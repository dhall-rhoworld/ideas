package com.rho.rhover.web.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.rho.rhover.common.study.Study;
import com.rho.rhover.web.query.QueryCandidate;
import com.rho.rhover.web.query.QueryCandidateRepository;

@Controller
@RequestMapping("/query")
public class QueryController {
	
	@Autowired
	private QueryCandidateRepository queryCandidateRepository;

	@RequestMapping("/all_queries")
	public String allQueries(Model model) {
		
		// Keyed on study name
		Map<String, List<QueryCandidate>> queryMap = new HashMap<String, List<QueryCandidate>>();
		Set<Study> studySet = new HashSet<>();
		
		Iterable<QueryCandidate> candidates = queryCandidateRepository.findAll();
		for (QueryCandidate qc : candidates) {
			Study study = qc.getAnomaly().getField().getStudy();
			studySet.add(study);
			List<QueryCandidate> list = queryMap.get(study.getStudyName());
			if (list == null) {
				list = new ArrayList<QueryCandidate>();
				queryMap.put(study.getStudyName(), list);
			}
			list.add(qc);
		}
		model.addAttribute("studySet", studySet);
		model.addAttribute("queryMap", queryMap);
		return "/query/all_queries";
	}

}
