package com.rho.rhover.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;
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
import com.rho.rhover.web.dto.CheckParamDto;

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

	// TODO: Move this business logic to a service bean
	@RequestMapping("/check_params")
	public List<CheckParamDto> getCheckParams(
			@RequestParam("study_id") Long studyId,
			@RequestParam("dataset_id") Long datasetId,
			@RequestParam("field_id") Long fieldId) {
		List<CheckParamDto> params = new ArrayList<>();
		Iterable<Check> checks = checkRepository.findAll();
		if (fieldId != -1) {
			Field field = fieldRepository.findOne(fieldId);
			logger.debug("Fetching check parameters for field " + field.getFieldName());
			for (Check check : checks) {
				params.addAll(CheckParamDto.toCheckParamDtos(checkParamRepository.findByCheckAndField(check, field)));
			}
		}
		else if (datasetId != -1) {
			Dataset dataset = datasetRepository.findOne(datasetId);
			logger.debug("Fetching check parameters for dataset " + dataset.getDatasetName());
			for (Check check : checks) {
				params.addAll(CheckParamDto.toCheckParamDtos(checkParamRepository.findByCheckAndDataset(check, dataset)));
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
				params.addAll(CheckParamDto.toCheckParamDtos(checkParams));
			}
		}
		else {
			logger.debug("Fetching global check parameters");
			for (Check check : checks) {
				params.addAll(CheckParamDto.toCheckParamDtos(checkParamRepository.findByCheckAndParamScope(check, "GLOBAL")));
			}
		}
		logger.debug("Found " + params.size() + " check parameters");
		return params;
	}
	
	@RequestMapping(value="/save_check_params", method=RequestMethod.POST)
	public Integer saveCheckParams(@RequestParam MultiValueMap<String, String> params) {
		int numParams = 0;
		Long studyId = Long.parseLong(params.getFirst("study_id"));
		Long datasetId = Long.parseLong(params.getFirst("dataset_id"));
		Long fieldId = Long.parseLong(params.getFirst("field_id"));
		for (String key : params.keySet()) {
			int p = key.indexOf("-");
			if (p > 0) {
				String checkName = key.substring(0, p);
				String paramName = key.substring(p + 1);
				String paramValue = params.getFirst(key);
				logger.debug(checkName + " " + paramName + ": " + paramValue);
				Check check = checkRepository.findByCheckName(checkName);
				CheckParam checkParam = null;
				if (fieldId != -1) {
					Field field = fieldRepository.findOne(fieldId);
					checkParam = checkParamRepository.findByCheckAndFieldAndParamName(check, field, paramName);
					if (checkParam == null) {
						checkParam = new CheckParam(paramName, "FIELD", check);
						checkParam.setField(field);
					}
				}
				else if (datasetId != -1) {
					Dataset dataset = datasetRepository.findOne(datasetId);
					checkParam = checkParamRepository.findByCheckAndDatasetAndParamName(check, dataset, paramName);
					if (checkParam == null) {
						checkParam = new CheckParam(paramName, "DATASET", check);
						checkParam.setDataset(dataset);
					}
				}
				else {
					Study study = studyRepository.findOne(studyId);
					checkParam = checkParamRepository.findByCheckAndStudyAndParamName(check, study, paramName);
					if (checkParam == null) {
						checkParam = new CheckParam(paramName, "STUDY", check);
						checkParam.setStudy(study);
					}
				}
				checkParam.setParamValue(paramValue);
				checkParamRepository.save(checkParam);
				numParams++;
			}
		}
		return numParams;
	}
}
