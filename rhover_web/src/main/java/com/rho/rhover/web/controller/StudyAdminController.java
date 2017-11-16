package com.rho.rhover.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.rho.rhover.common.study.DataLocation;
import com.rho.rhover.common.study.Study;
import com.rho.rhover.common.study.StudyRepository;

@Controller
@RequestMapping("/admin/study")
public class StudyAdminController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private StudyRepository studyRepository;

	@RequestMapping("/all")
	public String viewAll(Model model) {
		model.addAttribute("studies", studyRepository.findAll());
		return "admin/study/all_studies";
	}
	
	@RequestMapping("/edit_study")
	public String editStudy(
			@RequestParam("study_id") Long studyId,
			Model model) {
		model.addAttribute("study", studyRepository.findOne(studyId));
		return "admin/study/edit_study";
	}
	
	@RequestMapping("/new_study")
	public String newStudy() {
		
		return "admin/study/new_study";
	}
	
	@RequestMapping(value="/save_new", method=RequestMethod.POST)
	public String saveNew(
			@RequestParam("study_name") String studyName,
			@RequestParam("form_field_name") String formFieldName,
			Model model) {
		logger.debug("Saving new study: " + studyName);
		Study study = new Study();
		study.setStudyName(studyName);
		study.setFormFieldName(formFieldName);
		studyRepository.save(study);
		model.addAttribute("study", study);
		return "admin/study/edit_study";
	}
	
	@RequestMapping(value="/add_data_folder", method=RequestMethod.POST)
	public String saveDataFolder(
			@RequestParam("study_id") Long studyId,
			@RequestParam("folder_path") String folderPath,
			Model model) {
		logger.debug("studyId: " + studyId);
		logger.debug("folderPath: " + folderPath);
		Study study = studyRepository.findOne(studyId);
		DataLocation location = new DataLocation();
		location.setFolderPath(folderPath);
		location.setStudy(study);
		study.getDataLocations().add(location);
		studyRepository.save(study);
		model.addAttribute("study", study);
		return "admin/study/edit_study";
	}
}
