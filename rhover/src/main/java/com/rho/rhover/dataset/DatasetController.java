package com.rho.rhover.dataset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.rho.rhover.study.Study;
import com.rho.rhover.study.StudyRepository;

@Controller
@RequestMapping("/dataset")
public class DatasetController {
	
	@Autowired
	private DatasetRepository datasetRepository;
	
	@Autowired
	private StudyRepository studyRepository;

	@RequestMapping("/all")
	public String all(@RequestParam("study_id") Long studyId, Model model) {
		Study study = studyRepository.findOne(studyId);
		model.addAttribute("datasets", datasetRepository.findByStudy(study));
		model.addAttribute("study", study);
		return "/datasets";
	}
}
