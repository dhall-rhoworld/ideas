package com.rho.rhover.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.rho.rhover.common.study.Study;
import com.rho.rhover.common.study.StudyRepository;
import com.rho.rhover.web.dto.AnomalySummaryBuilder;

@Controller
@RequestMapping("/data")
public class DataController {
	
	@Autowired
	private StudyRepository studyRepository;
	
	@Autowired
	private AnomalySummaryBuilder anomalySummaryBuilder;
	
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
}
