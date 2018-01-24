package com.rho.rhover.web.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
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
import com.rho.rhover.common.check.Correlation;
import com.rho.rhover.common.check.CorrelationFinder;
import com.rho.rhover.common.study.CsvDataService;
import com.rho.rhover.common.study.Dataset;
import com.rho.rhover.common.study.DatasetRepository;
import com.rho.rhover.common.study.DatasetVersion;
import com.rho.rhover.common.study.DatasetVersionRepository;
import com.rho.rhover.common.study.Field;
import com.rho.rhover.common.study.FieldInstance;
import com.rho.rhover.common.study.FieldInstanceRepository;
import com.rho.rhover.common.study.FieldRepository;
import com.rho.rhover.common.study.FieldService;
import com.rho.rhover.common.study.MergeField;
import com.rho.rhover.common.study.MergeFieldRepository;
import com.rho.rhover.common.study.Study;
import com.rho.rhover.common.study.StudyRepository;
import com.rho.rhover.web.dto.BivariateCheckParamsDto;
import com.rho.rhover.web.dto.CheckParamDto;
import com.rho.rhover.web.dto.CorrDatasetDto;
import com.rho.rhover.web.dto.FieldDto;
import com.rho.rhover.web.dto.FieldDtoGroup;
import com.rho.rhover.web.dto.FieldInstanceDto;
import com.rho.rhover.web.dto.JqueryUiAutocompleteDto;
import com.rho.rhover.web.dto.MergeFieldSetupInfo;
import com.rho.rhover.web.dto.MergeTestResults;
import com.rho.rhover.web.service.AutocompleteHelperService;
import com.rho.rhover.web.service.CorrDatasetDtoService;

@RestController
@RequestMapping("/rest/admin/study")
public class StudyAdminRestController {
	
	private static final double MIN_CORRELATION_COEF = 0.5;
	
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
	
	@Autowired
	private AutocompleteHelperService autocompleteHelperService;
	
	@Autowired
	private CorrelationFinder correlationFinder;
	
	@Autowired
	private MergeFieldRepository mergeFieldRepository;
	
	@Autowired
	private FieldService fieldService;
	
	@Autowired
	private CsvDataService csvDataService;
	
	@Value("${checker.url}")
	private String checkerUrl;

	// TODO: Move this business logic to a service bean
	// TODO: Not sure if this is used any more.  Consider deleting.
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
		List<FieldInstance> fieldInstances = fieldInstanceRepository.findByDataset(dataset);
		return FieldDtoGroup.toDtoGroups(fieldInstances);
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
	
	@RequestMapping("/get_matching_field_instances")
	private List<JqueryUiAutocompleteDto> getMatchingFieldInstances(
			@RequestParam("study_id") Long studyId,
			@RequestParam("term") String term
	) {
		return autocompleteHelperService.findMatchingFieldInstances(term, studyId);
	}
	
	@RequestMapping("/fetch_variable_instance_id")
	private Long fetchVariableInstanceId(
			@RequestParam("study_id") Long studyId,
			@RequestParam("variable_name") String variableName,
			@RequestParam("dataset_name") String datasetName
	) {
		Study study = studyRepository.findOne(studyId);
		Dataset dataset = datasetRepository.findByStudyAndDatasetName(study, datasetName);
		Field field = fieldRepository.findByStudyAndFieldName(study, variableName);
		FieldInstance fieldInstance = fieldInstanceRepository.findByFieldAndDataset(field, dataset);
		return fieldInstance.getFieldInstanceId();
	}
	
	
	// TODO: Move this to the checker component when that is turned into
	// a service so that this potentially compute-intensive method can be
	// offloaded to a beefy server
	@RequestMapping("/find_correlated_fields")
	public List<FieldInstanceDto> findCorrelatedFields(
			@RequestParam("field_instance_id") Long fieldInstanceId) {
		FieldInstance fieldInstance = fieldInstanceRepository.findOne(fieldInstanceId);
		List<FieldInstanceDto> dtos = new ArrayList<>();
		List<Correlation> correlations = correlationFinder.findAllCorrelatedFields(fieldInstance, MIN_CORRELATION_COEF);
		for (Correlation correlation : correlations) {
			FieldInstance fieldInstance2 = correlation.getFieldInstance2();
			FieldInstanceDto dto = new FieldInstanceDto();
			dto.setDataSetName(fieldInstance2.getDataset().getDatasetName());
			dto.setDataTypeDisplayName(fieldInstance2.getField().getDisplayDataType());
			Field field = fieldInstance2.getField();
			if (field.getFieldLabel() == null || field.getFieldLabel().length() == 0) {
				dto.setFieldDisplayName(field.getFieldName() + " ("
						+ field.getFieldName() + ")");
			}
			else {
				dto.setFieldDisplayName(field.getFieldLabel() + " ("
						+ field.getFieldName() + ")");
			}
			dto.setFieldInstanceId(fieldInstance2.getFieldInstanceId());
			dtos.add(dto);
		}
		return dtos;
	}
	
	@RequestMapping(value="/delete_bivariate_checks", method=RequestMethod.POST)
	public Integer deleteBivariateChecks(@RequestParam("check_ids") String bivariateCheckIds) {
		String[] checkIdArray = bivariateCheckIds.split(",");
		for (String id : checkIdArray) {
			bivariateCheckRepository.delete(new Long(id));
		}
		return checkIdArray.length;
	}
	
	@RequestMapping(value="save_bivariate_check_edits", method=RequestMethod.POST)
	public Integer saveBivariateCheckEdits(@RequestBody List<BivariateCheckParamsDto> dtos) {
		for (BivariateCheckParamsDto dto : dtos) {
			BivariateCheck biCheck = bivariateCheckRepository.findOne(dto.getBivariateCheckId());
			
			// Case: Use default params
			if (dto.getUseDefaults().equals(Boolean.TRUE)) {
				logger.debug("Removing any existing params for bivariate check " + dto.getBivariateCheckId());
				Iterator<String> keyIterator = biCheck.getCheckParams().keySet().iterator();
				while (keyIterator.hasNext()) {
					String key = keyIterator.next();
					logger.debug("Removing param " + key);
					CheckParam param = biCheck.getCheckParams().get(key);
					checkParamRepository.delete(param);
					keyIterator.remove();
				}
			}
			
			// Case: Use custom params
			else {
				logger.debug("Saving custom params for bivariate check " + dto.getBivariateCheckId());
				Check check = checkRepository.findByCheckName("BIVARIATE_OUTLIER");
				
				// Save 'sd-residual' parameter
				CheckParam sdResidual = biCheck.getCheckParams().get("sd-residual");
				if (sdResidual == null) {
					logger.debug("Creating new 'sd-residual' parameter");
					sdResidual = new CheckParam("sd-residual", "BIVARIATE", check);
					sdResidual.setBivariateCheck(biCheck);
					biCheck.getCheckParams().put("sd-residual", sdResidual);
				}
				else {
					logger.debug("Updating 'sd-residual' parameter");
				}
				sdResidual.setParamValue(dto.getSdResidual().toString());
				checkParamRepository.save(sdResidual);
				
				// Save 'sd-density' parameter
				CheckParam sdDensity = biCheck.getCheckParams().get("sd-density");
				if (sdDensity == null) {
					logger.debug("Creating new 'sd-density' parameter");
					sdDensity = new CheckParam("sd-density", "BIVARIATE", check);
					sdDensity.setBivariateCheck(biCheck);
					biCheck.getCheckParams().put("sd-density", sdDensity);
				}
				else {
					logger.debug("Updating 'sd-density' parameter");
				}
				sdDensity.setParamValue(dto.getSdDensity().toString());
				checkParamRepository.save(sdDensity);
			}
			
			bivariateCheckRepository.save(biCheck);
		}
		return 0;
	}
	
	@RequestMapping("/get_merge_field_info")
	public List<MergeFieldSetupInfo> getMergeFieldInfo(
			@RequestParam("variable_x") Long fieldInstanceIdX,
			@RequestParam(name="variable_y", required=false, defaultValue="") String fieldInstanceIdsY,
			@RequestParam(name="dataset_y", required=false, defaultValue="") Long datasetYId
	) {
		
		// Get X dataset
		FieldInstance fieldInstanceX = fieldInstanceRepository.findOne(fieldInstanceIdX);
		Dataset xDataset = fieldInstanceX.getDataset();
		
		// Assemble set of Y datasets
		Set<Dataset> yDatasets = new HashSet<>();
		if (fieldInstanceIdsY.length() > 0) {
			String[] tokens = fieldInstanceIdsY.split(",");
			for (String token : tokens) {
				Long fieldIdY = new Long(token);
				FieldInstance fieldInstanceY = fieldInstanceRepository.findOne(fieldIdY);
				yDatasets.add(fieldInstanceY.getDataset());
			}
		}
		else {
			yDatasets.add(datasetRepository.findOne(datasetYId));
		}
		
		// Iterate over each pair of datasets
		List<MergeFieldSetupInfo> dtos = new ArrayList<>();
		for (Dataset yDataset : yDatasets) {
			if (xDataset.equals(yDataset)) {
				continue;
			}
			if (mergeFieldRepository.findByDataset1AndDataset2(xDataset, yDataset).size() == 0) {
				
				// Get common fields
				List<Field> fields = fieldService.findPotentialMergeFields(xDataset, yDataset);
				
				// Build a new DTO
				MergeFieldSetupInfo mergeInfo = new MergeFieldSetupInfo();
				mergeInfo.setDatasetName1(xDataset.getDatasetName());
				mergeInfo.setDatasetName2(yDataset.getDatasetName());
				for (Field field : fields) {
					FieldDto fieldDto = new FieldDto();
					fieldDto.setFieldLabel(field.getFieldLabel());
					fieldDto.setFieldName(field.getFieldName());
					fieldDto.setFieldId(field.getFieldId());
					mergeInfo.getFields().add(fieldDto);
				}
				
				dtos.add(mergeInfo);
			}
		}
		
		return dtos;
	}
	
	@RequestMapping("/test_merge")
	public MergeTestResults testMerge(
			@RequestParam("field_ids") String fieldIdStr,
			@RequestParam("dataset_name_1") String datasetName1,
			@RequestParam("dataset_name_2") String datasetName2,
			@RequestParam("study_id") Long studyId,
			@RequestParam("variable_x") Long xFieldInstanceId,
			@RequestParam(name="variable_y", required=false, defaultValue="") String yFieldInstanceIds
	) {
		Study study = studyRepository.findOne(studyId);
		Dataset dataset1 = datasetRepository.findByStudyAndDatasetName(study, datasetName1);
		Dataset dataset2 = datasetRepository.findByStudyAndDatasetName(study, datasetName2);
		List<MergeField> mergeFields = new ArrayList<>();
		String[] tokens = fieldIdStr.split(",");
		for (String token : tokens) {
			Long fieldId = new Long(token);
			Field field = fieldRepository.findOne(fieldId);
			FieldInstance fi1 = fieldInstanceRepository.findByFieldAndDataset(field, dataset1);
			FieldInstance fi2 = fieldInstanceRepository.findByFieldAndDataset(field, dataset2);
			MergeField mergeField = new MergeField();
			mergeField.setFieldInstance1(fi1);
			mergeField.setFieldInstance2(fi2);
			mergeFields.add(mergeField);
		}
		List<FieldInstance> dataFields = new ArrayList<>();
		dataFields.add(fieldInstanceRepository.findOne(xFieldInstanceId));
		if (yFieldInstanceIds.length() > 0) {
			tokens = yFieldInstanceIds.split(",");
			for (String token : tokens) {
				Long fieldInstanceId = new Long(token);
				FieldInstance fieldInstance = fieldInstanceRepository.findOne(fieldInstanceId);
				dataFields.add(fieldInstance);
			}
		}
		String csvString = csvDataService.mergeToCsv(mergeFields, dataFields, Boolean.FALSE, Boolean.FALSE);
		MergeTestResults results = new MergeTestResults();
		results.setDatasetName1(datasetName1);
		results.setDatasetName2(datasetName2);
		results.setNumRecords1(fieldService.getNumRecords(mergeFields.get(0).getFieldInstance1()));
		results.setNumRecords2(fieldService.getNumRecords(mergeFields.get(0).getFieldInstance2()));
		results.setNumMergedRecords((int)csvString.chars().filter(ch -> ch == '\n').count() - 1);
		return results;
	}
}
