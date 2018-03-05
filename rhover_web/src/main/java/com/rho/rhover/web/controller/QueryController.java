package com.rho.rhover.web.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.rho.rhover.common.study.Study;
import com.rho.rhover.common.study.StudyRepository;
import com.rho.rhover.web.query.QueryCandidate;
import com.rho.rhover.web.query.QueryCandidateRepository;
import com.rho.rhover.web.service.QueryFilterHelper;

@Controller
@RequestMapping("/query")
public class QueryController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private QueryCandidateRepository queryCandidateRepository;
	
	@Autowired
	private StudyRepository studyRepository;
	
	@Autowired
	private QueryFilterHelper queryFilterHelper;

	@RequestMapping("/all_queries")
	public String allQueries(Model model,
			@RequestParam(name="study_id", required=false, defaultValue="0") Long studyId,
			@RequestParam(name="filters", required=false, defaultValue="") String filterJSON) {
		model.addAttribute("allStudies", studyRepository.findAll());
		if (studyId != 0) {
			Study study = studyRepository.findOne(studyId);
			model.addAttribute("selectedStudy", study);
			List<QueryCandidate> candidates = queryCandidateRepository.findByStudy(study);
			model.addAttribute("candidates", candidates);
			model.addAttribute("filterOptions", queryFilterHelper.getFilterOptionsAsJSON(study));
			if (filterJSON.length() == 0) {
				model.addAttribute("filters", "{statuses: [], datasets: [], sites: [], phases: [], subjects: [], fields: []}");
			}
			else {
				model.addAttribute("filters", filterJSON);
				applyFilter(candidates, filterJSON);
			}
		}
		else {
			model.addAttribute("filters", "{}");
			model.addAttribute("filterOptions", "{}");
		}
		return "/query/all_queries";
	}
	
	private Map<String, List<Long>> parseJSON(String json) {
		Map<String, List<Long>> map = new HashMap<String, List<Long>>();
		Pattern pattern = Pattern.compile("[a-z]+:\\[[0-9,]*\\]");
		Matcher matcher = pattern.matcher(json);
		while (matcher.find()) {
			matcher.toMatchResult();
			String token = json.substring(matcher.start(), matcher.end());
			int p = json.indexOf(":", matcher.start());
			String fieldName = json.substring(matcher.start(), p);
			p = json.indexOf("[", p);
			int q = json.indexOf("]", p);
			String valueStr = json.substring(p + 1, q);
			if (valueStr.length() > 0) {
				List<Long> ids = new ArrayList<Long>();
				String[] idArray = valueStr.split(",");
				for (String id : idArray) {
					ids.add(new Long(id));
				}
				map.put(fieldName, ids);
			}
		}
		return map;
	}
	
	private void applyFilter(List<QueryCandidate> candidates, String filterJSON) {
		Map<String, List<Long>> filterCriteria = parseJSON(filterJSON);
		Iterator<QueryCandidate> it = candidates.iterator();
		while (it.hasNext()) {
			if (!passesFilter(it.next(), filterCriteria)) {
				it.remove();
			}
		}
	}

	private boolean passesFilter(QueryCandidate candidate, Map<String, List<Long>> filterCriteria) {
		return
				passesFilter(candidate, filterCriteria, "statuses", candidate.getQueryStatus().getQueryStatusId()) &&
				passesFilter(candidate, filterCriteria, "datasets", candidate.getAnomaly().getFieldInstance().getDataset().getDatasetId()) &&
				passesFilter(candidate, filterCriteria, "sites", candidate.getAnomaly().getSite().getSiteId()) &&
				passesFilter(candidate, filterCriteria, "phases", candidate.getAnomaly().getPhase().getPhaseId()) &&
				passesFilter(candidate, filterCriteria, "subjects", candidate.getAnomaly().getSubject().getSubjectId()) &&
				passesFilter(candidate, filterCriteria, "fields", candidate.getAnomaly().getField().getFieldId());
	}
	
	private boolean passesFilter(QueryCandidate candidate, Map<String, List<Long>> filterCriteria, String fieldName, Long fieldValue) {
		List<Long> ids = filterCriteria.get(fieldName);
		if (ids == null) {
			return true;
		}
		return ids.contains(fieldValue);
	}
}
