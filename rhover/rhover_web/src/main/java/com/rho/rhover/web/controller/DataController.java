package com.rho.rhover.web.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.rho.rhover.common.study.Dataset;
import com.rho.rhover.common.study.DatasetRepository;
import com.rho.rhover.common.study.DatasetVersionRepository;
import com.rho.rhover.common.study.Study;
import com.rho.rhover.common.study.StudyRepository;
import com.rho.rhover.web.dto.AnomalySummary;
import com.rho.rhover.web.dto.AnomalySummaryBuilder;

@Controller
@RequestMapping("/data")
public class DataController {
	
	@Autowired
	private StudyRepository studyRepository;
	
	@Autowired
	private AnomalySummaryBuilder anomalySummaryBuilder;
	
	@Autowired
	private DatasetRepository datasetRepository;
	
	@Autowired
	private DatasetVersionRepository datasetVersionRepository;
	
	@RequestMapping("/all_studies")
	public String allStudies(Model model) {
		model.addAttribute("studies", studyRepository.findAll());
		return "/data/all_studies";
	}
	
	@RequestMapping("/study")
	public String study(Model model, @RequestParam("study_id") Long studyId) {
		Study study = studyRepository.findOne(studyId);
		model.addAttribute("study", study);
    	model.addAttribute("summaries", anomalySummaryBuilder.getDatasetSummaries(study, false));
		return "/data/study";
	}
	
	@RequestMapping("/dataset")
	public String dataset(Model model, @RequestParam("dataset_id") Long datasetId) {
		Dataset dataset = datasetRepository.findOne(datasetId);
		model.addAttribute("dataset", dataset);
		model.addAttribute("summaries", buildAnomalySummaryMap(anomalySummaryBuilder.getUnivariateDataFieldSummaries(datasetId, true)));
		model.addAttribute("datasetVersion", datasetVersionRepository.findByDatasetAndIsCurrent(dataset, Boolean.TRUE));
		return "/data/dataset";
	}
	
	private Map<Long, AnomalySummary> buildAnomalySummaryMap(List<AnomalySummary> summaries) {
		Map<Long, AnomalySummary> map = new HashMap<Long, AnomalySummary>();
		for (AnomalySummary summary : summaries) {
			map.put(summary.getEntityId(), summary);
		}
		return map;
	}
}
