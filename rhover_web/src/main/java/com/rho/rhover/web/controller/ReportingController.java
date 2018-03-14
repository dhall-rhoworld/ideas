package com.rho.rhover.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.rho.rhover.common.anomaly.DatumChangeRepository;
import com.rho.rhover.common.anomaly.ObservationRepository;
import com.rho.rhover.common.study.DatasetVersion;
import com.rho.rhover.common.study.DatasetVersionRepository;
import com.rho.rhover.common.study.FieldInstanceRepository;
import com.rho.rhover.common.study.StudyDbVersion;
import com.rho.rhover.common.study.StudyDbVersionRepository;
import com.rho.rhover.web.service.ReportingService;

@Controller
@RequestMapping("/reporting")
public class ReportingController {
	
	@Autowired
	private ReportingService reportingService;
	
	@Autowired
	private StudyDbVersionRepository studyDbVersionRepository;
	
	@Autowired
	private DatasetVersionRepository datasetVersionRepository;
	
	@Autowired
	private FieldInstanceRepository fieldInstanceRepository;
	
	@Autowired
	private ObservationRepository observationRepository;
	
	@Autowired
	private DatumChangeRepository datumChangeRepository;

	@RequestMapping("/loads")
	public String showLoads(Model model) {
		model.addAttribute("overviews", reportingService.getStudyLoadOverviews());
		return "reporting/loads";
	}

	@RequestMapping("/loaded_datasets")
	public String showLoadedDatasets(Model model,
			@RequestParam("study_db_version_id") Long studyDbVersionId,
			@RequestParam("datasets") String datasets) {
		StudyDbVersion studyDbVersion = studyDbVersionRepository.findOne(studyDbVersionId);
		if (datasets.equals("all")) {
			model.addAttribute("overviews", reportingService.getAllDatasetLoadOverviews(studyDbVersion));
		}
		else if (datasets.equals("new")) {
			model.addAttribute("overviews", reportingService.getNewDatasetLoadOverviews(studyDbVersion));
		}
		else if (datasets.equals("modified")) {
			model.addAttribute("overviews", reportingService.getModifiedDatasetLoadOverviews(studyDbVersion));
		}
		return "reporting/loaded_datasets";
	}
	
	@RequestMapping("/new_fields")
	public String showNewFields(Model model,
			@RequestParam("dataset_version_id") Long datasetVersionId) {
		DatasetVersion datasetVersion = datasetVersionRepository.findOne(datasetVersionId);
		model.addAttribute("fieldInstances", fieldInstanceRepository.findByFirstDatasetVersion(datasetVersion));
		return "reporting/new_fields";
	}
	
	@RequestMapping("/new_records")
	public String showNewRecords(Model model,
			@RequestParam("dataset_version_id") Long datasetVersionId) {
		DatasetVersion datasetVersion = datasetVersionRepository.findOne(datasetVersionId);
		model.addAttribute("observations", observationRepository.findByFirstDatasetVersion(datasetVersion));
		model.addAttribute("study", datasetVersion.getDataset().getStudy());
		return "reporting/new_records";
	}
	
	@RequestMapping("/modified_values")
	public String showModifiedValues(Model model,
			@RequestParam("dataset_version_id") Long datasetVersionId) {
		DatasetVersion datasetVersion = datasetVersionRepository.findOne(datasetVersionId);
		model.addAttribute("study", datasetVersion.getDataset().getStudy());
		model.addAttribute("datumChanges", datumChangeRepository.findByDatasetVersion(datasetVersion));
		return "reporting/modified_values";
	}
}
