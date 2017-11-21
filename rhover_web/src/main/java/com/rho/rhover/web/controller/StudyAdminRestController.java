package com.rho.rhover.web.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rho.rhover.common.check.Check;
import com.rho.rhover.common.check.CheckParam;
import com.rho.rhover.common.check.CheckParamRepository;
import com.rho.rhover.common.check.CheckRepository;
import com.rho.rhover.common.study.Dataset;
import com.rho.rhover.common.study.DatasetRepository;
import com.rho.rhover.common.study.Field;
import com.rho.rhover.common.study.FieldRepository;
import com.rho.rhover.common.study.Study;
import com.rho.rhover.common.study.StudyRepository;

@RestController
@RequestMapping("/rest/admin/study")
public class StudyAdminRestController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private StudyRepository studyRepository;
	
	@Autowired
	private DatasetRepository datasetRepository;
	
	@Autowired
	private FieldRepository fieldRepository;
	
	@Autowired
	private CheckRepository checkRepository;
	
	@Autowired
	private CheckParamRepository checkParamRepository;

	@RequestMapping("/check_params")
	public List<CheckParam> getCheckParams(
			@RequestParam("study_id") Long studyId,
			@RequestParam("dataset_id") Long datasetId,
			@RequestParam("field_id") Long fieldId) {
		List<CheckParam> params = new ArrayList<>();
		Iterable<Check> checks = checkRepository.findAll();
		if (fieldId != -1) {
			Field field = fieldRepository.findOne(fieldId);
			logger.debug("Fetching check parameters for field " + field.getFieldName());
			for (Check check : checks) {
				params.addAll(checkParamRepository.findByCheckAndField(check, field));
			}
		}
		else if (datasetId != -1) {
			Dataset dataset = datasetRepository.findOne(datasetId);
			logger.debug("Fetching check parameters for dataset " + dataset.getDatasetName());
			for (Check check : checks) {
				params.addAll(checkParamRepository.findByCheckAndDataset(check, dataset));
			}
		}
		else if (studyId != -1) {
			Study study = studyRepository.findOne(studyId);
			logger.debug("Fetching check parameters for study " + study.getStudyName());
			for (Check check : checks) {
				List<CheckParam> checkParams = checkParamRepository.findByCheckAndStudy(check, study);
				if (checkParams.size() == 0) {
					logger.debug("No study-level check parameters found for check " + check.getCheckName() + ".  Fetching globals.");
					checkParams = checkParamRepository.findByCheckAndParamScope(check, "GLOBAL");
				}
				params.addAll(checkParams);
			}
		}
		else {
			logger.debug("Fetching global check parameters");
			for (Check check : checks) {
				params.addAll(checkParamRepository.findByCheckAndParamScope(check, "GLOBAL"));
			}
		}
		logger.debug("Found " + params.size() + " check parameters");
		return params;
	}
}
