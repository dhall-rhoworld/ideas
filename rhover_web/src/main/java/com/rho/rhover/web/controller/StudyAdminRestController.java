package com.rho.rhover.web.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.rho.rhover.common.check.BivariateCheck;
import com.rho.rhover.common.check.BivariateCheckRepository;
import com.rho.rhover.common.check.Check;
import com.rho.rhover.common.check.CheckParam;
import com.rho.rhover.common.check.CheckParamRepository;
import com.rho.rhover.common.check.CheckRepository;
import com.rho.rhover.common.study.Dataset;
import com.rho.rhover.common.study.DatasetRepository;
import com.rho.rhover.common.study.DatasetVersion;
import com.rho.rhover.common.study.DatasetVersionRepository;
import com.rho.rhover.common.study.Field;
import com.rho.rhover.common.study.FieldInstance;
import com.rho.rhover.common.study.FieldInstanceRepository;
import com.rho.rhover.common.study.FieldRepository;
import com.rho.rhover.common.study.Study;
import com.rho.rhover.common.study.StudyRepository;
import com.rho.rhover.web.dto.CheckParamDto;
import com.rho.rhover.web.dto.CorrDatasetDto;
import com.rho.rhover.web.dto.FieldDtoGroup;
import com.rho.rhover.web.service.CorrDatasetDtoService;

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
	
	@Autowired
	private DatasetVersionRepository datasetVersionRepository;
	
	@Autowired
	private CorrDatasetDtoService corrDatasetDtoService;
	
	@Autowired
	private BivariateCheckRepository bivariateCheckRepository;
	
	@Autowired
	private FieldInstanceRepository fieldInstanceRepository;
	
	@Value("${checker.url}")
	private String checkerUrl;

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
	
	@RequestMapping("/fields")
	public List<FieldDtoGroup> getFields(@RequestParam("dataset_id") Long datasetId) {
		Dataset dataset = datasetRepository.findOne(datasetId);
		DatasetVersion datasetVersion = datasetVersionRepository.findByDatasetAndIsCurrent(dataset, Boolean.TRUE);
		return FieldDtoGroup.toDtoGroups(datasetVersion.getFields());
	}
	
	@RequestMapping("/check_study")
	public ResponseEntity<String> runChecksOnStudy(@RequestParam("study_id") Long studyId) {
		logger.debug("Running checks on study " + studyId);
		String url = checkerUrl + "/rest/check_study?study_id=" + studyId;
		RestTemplate restTemplate = new RestTemplate();
		return new ResponseEntity<>("OK", HttpStatus.OK);
	}
	
	@RequestMapping("/correlations")
	public Collection<CorrDatasetDto> getCorrelationData(@RequestParam("study_id") Long studyId) {
		Study study = studyRepository.findOne(studyId);
		return corrDatasetDtoService.getCorrDatasetDtos(study);
	}
	
	@RequestMapping(value = "/save_bivariates_correlated", method = RequestMethod.POST)
	public String saveBivariateChecksForCorrelatedFields(
			@RequestParam MultiValueMap<String, String> params,
			@RequestParam("source_field_instance_id") Long sourceFieldInstanceId
	) {
		Check check = checkRepository.findByCheckName("BIVARIATE_OUTLIER");
		logger.debug("Saving bivariate checks for field instance: " + sourceFieldInstanceId);
		FieldInstance source = fieldInstanceRepository.findOne(sourceFieldInstanceId);
		for (String key : params.keySet()) {
			if (key.equals("source_field_instance_id")) {
				continue;
			}
			Long targetFieldInstanceId = new Long(key.substring("target-".length()));
			logger.debug("Target field instance: " + targetFieldInstanceId);
			FieldInstance target = fieldInstanceRepository.findOne(targetFieldInstanceId);
			BivariateCheck biCheck = bivariateCheckRepository.findByXFieldInstanceAndYFieldInstance(source, target);
			if (biCheck == null) {
				biCheck = new BivariateCheck(source, target, check, source.getDataset().getStudy());
				bivariateCheckRepository.save(biCheck);
			}
		}
		return "OK";
	}
}
