package com.rho.rhover.web.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.rho.rhover.common.study.DataLocation;
import com.rho.rhover.common.study.Field;
import com.rho.rhover.common.study.FieldRepository;
import com.rho.rhover.common.study.FieldService;
import com.rho.rhover.common.study.Study;
import com.rho.rhover.common.study.StudyRepository;

@Controller
@RequestMapping("/admin/study")
public class StudyAdminController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private StudyRepository studyRepository;
	
	@Autowired
	private FieldService fieldService;
	
	@Autowired
	private FieldRepository fieldRepository;

	@RequestMapping("/all")
	public String viewAll(Model model) {
		model.addAttribute("studies", studyRepository.findAll());
		return "admin/study/all_studies";
	}
	
	@RequestMapping("/data_locations")
	public String editStudy(
			@RequestParam("study_id") Long studyId,
			Model model) {
		model.addAttribute("study", studyRepository.findOne(studyId));
		return "admin/study/data_locations";
	}
	
	@RequestMapping("/identifying_fields")
	public String selectIdentifyingVariables(@RequestParam("study_id") Long studyId,
			Model model) {
		Study study = studyRepository.findOne(studyId);
		model.addAttribute("study", study);
		model.addAttribute("fields", fieldService.findPotentiallyIdentiableFields(study));
		return "admin/study/identifying_fields";
	}
	
	public String showKeyVariables(
			@RequestParam("study_id") Long studyId,
			Model model) {
		return null;
	}
	
	@RequestMapping("/new_study")
	public String newStudy() {
		
		return "admin/study/new_study";
	}
	
	@RequestMapping(value="/save", method=RequestMethod.POST)
	public String saveNew(
			@RequestParam(name="study_id", required=false, defaultValue="-1") Long studyId,
			@RequestParam("study_name") String studyName,
			@RequestParam("form_field_name") String formFieldName,
			@RequestParam("site_field_name") String siteFieldName,
			@RequestParam("subject_field_name") String subjectFieldName,
			@RequestParam(name="query_file_path", required=false, defaultValue="") String queryFilePath,
			Model model) {
		Study study = null;
		String nextPage = null;
		if (studyId != -1) {
			logger.debug("Updating study: " + studyId);
			study = studyRepository.findOne(studyId);
			nextPage = "admin/study/all";;
		}
		else {
			logger.debug("Saving new study: " + studyName);
			study = new Study();
			nextPage = "admin/study/data_locations";
			String message = "Add one or more data folders to the new study.  Enter the full network path and use forward slashes " +
					"rather than back slashes.  For example, S:/RhoFED/ICAC2/PROSE/Statistics/Data/Complete";
			model.addAttribute("message", message);
		}
		study.setStudyName(studyName);
		study.setFormFieldName(formFieldName);
		study.setSiteFieldName(siteFieldName);
		study.setSubjectFieldName(subjectFieldName);
		if (queryFilePath.length() > 0) {
			study.setQueryFilePath(queryFilePath);
		}
		studyRepository.save(study);
		model.addAttribute("study", study);
		return nextPage;
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
		String message = "The system will process the data files in the new data folder.  You will be notified after processing " +
				"is complete.  Afterwards, you will be able to select key variables and configure data checks for these datasets.";
		model.addAttribute("message", message);
		return "admin/study/data_locations";
	}
	
	@RequestMapping(value="/save_identifying_fields", method=RequestMethod.POST)
	public String saveIdentifyingFields(
			@RequestParam MultiValueMap<String, String> params,
			Model model) {
		Long studyId = Long.parseLong(params.getFirst("study_id"));
		Study study = studyRepository.findOne(studyId);
		List<Long> fieldIds = new ArrayList<>();
		for (String paramName : params.keySet()) {
			logger.debug("Param: " + paramName);
			if (paramName.startsWith("field-")) {
				Long fieldId = Long.parseLong(paramName.substring(6));
				fieldIds.add(fieldId);
			}
		}
		fieldService.setIdentifiableFields(study, fieldIds);
		model.addAttribute("studies", studyRepository.findAll());
		return "/admin/study/all_studies";
	}
}
