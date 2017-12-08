package com.rho.rhover.web.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.rho.rhover.common.check.Check;
import com.rho.rhover.common.check.CheckParam;
import com.rho.rhover.common.check.CheckParamRepository;
import com.rho.rhover.common.check.CheckParamService;
import com.rho.rhover.common.check.CheckRepository;
import com.rho.rhover.common.study.DataLocation;
import com.rho.rhover.common.study.Dataset;
import com.rho.rhover.common.study.DatasetRepository;
import com.rho.rhover.common.study.DatasetVersion;
import com.rho.rhover.common.study.DatasetVersionRepository;
import com.rho.rhover.common.study.Field;
import com.rho.rhover.common.study.FieldRepository;
import com.rho.rhover.common.study.FieldService;
import com.rho.rhover.common.study.Study;
import com.rho.rhover.common.study.StudyDbVersion;
import com.rho.rhover.common.study.StudyDbVersionRepository;
import com.rho.rhover.common.study.StudyRepository;
import com.rho.rhover.web.service.CheckConfigurationService;

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
	
	@Autowired
	private CheckRepository checkRepository;
	
	@Autowired
	private StudyDbVersionRepository studyDbVersionRepository;
	
	@Autowired
	private DatasetVersionRepository datasetVersionRepository;
	
	@Autowired
	private DatasetRepository datasetRepository;
	
	@Autowired
	private CheckParamRepository checkParamRepository;
	
	@Autowired
	private CheckParamService checkParamService;
	
	@Autowired
	private CheckConfigurationService checkConfigurationService;

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
	
	@RequestMapping("/checks")
	public String showChecks(
			@RequestParam(name="study_id") Long studyId,
			Model model) {
		Study study = studyRepository.findOne(studyId);
		model.addAttribute("study", study);
		model.addAttribute("studyDbVersion", studyDbVersionRepository.findByStudyAndIsCurrent(study, Boolean.TRUE));	
		return "admin/study/checks";
	}
	
	@RequestMapping("/study_univariate")
	public String showStudyUnivariate (
			@RequestParam(name="study_id") Long studyId,
			Model model) {
		Study study = studyRepository.findOne(studyId);
		model.addAttribute("study", study);
		StudyDbVersion studyDbVersion = studyDbVersionRepository.findByStudyAndIsCurrent(study, Boolean.TRUE);
		SortedSet<DatasetVersion> datasetVersions = new TreeSet<DatasetVersion>(new Comparator<DatasetVersion>() {
			public int compare(DatasetVersion dv1, DatasetVersion dv2) {
				return dv1.getDataset().getDatasetName().compareTo(dv2.getDataset().getDatasetName());
			}
		});
		datasetVersions.addAll(studyDbVersion.getDatasetVersions());
		model.addAttribute("dataset_versions", datasetVersions);
		Check check = checkRepository.findByCheckName("UNIVARIATE_OUTLIER");
		model.addAttribute("data_types", checkParamService.getCheckParam(check, "data_types", study));
		model.addAttribute("filter_non_key", checkParamService.getCheckParam(check, "filter_non_key", study));
		model.addAttribute("filter_identifying", checkParamService.getCheckParam(check, "filter_identifying", study));
		model.addAttribute("sd", checkParamService.getCheckParam(check, "sd", study));
		return "admin/study/study_univariate";
	}
	
	@RequestMapping(value="/save_study_univariate", method=RequestMethod.POST)
	public String saveStudyUnivariate(
			@RequestParam MultiValueMap<String, String> requestParams,
			Model model) {
		Long studyId = Long.parseLong(requestParams.getFirst("study_id"));
		Study study = studyRepository.findOne(studyId);
		Check check = checkRepository.findByCheckName("UNIVARIATE_OUTLIER");
		Set<CheckParam> allParams = checkParamService.getAllCheckParams(check, study);
		Map<String, String> studyParams = new HashMap<>();
		for (CheckParam param : allParams) {
			String requestParamName = "param_" + param.getParamName();
			String paramValue = null;
			if (requestParams.containsKey(requestParamName)) {
				paramValue = requestParams.getFirst(requestParamName);
			}
			else {
				paramValue = "off";
			}
			studyParams.put(param.getParamName(), paramValue);
		}
		Collection<Long> datasetIds = new ArrayList<>();
		for (String key : requestParams.keySet()) {
			if (key.startsWith("check_dataset-")) {
				Long datasetId = Long.parseLong(key.substring(14));
				datasetIds.add(datasetId);
			}
		}
		checkConfigurationService.saveStudyCheckConfiguration(study, check, studyParams, datasetIds);
		model.addAttribute("message", "Study parameters saved");
		return "forward:/admin/study/study_univariate";
	}
	
	@RequestMapping("/dataset_univariate")
	public String showDatasetUnivariate(
			@RequestParam(name="study_id") Long studyId,
			@RequestParam(name="dataset_id", required=false, defaultValue="-1") Long datasetId,
			Model model) {
		Study study = studyRepository.findOne(studyId);
		model.addAttribute("study", study);
		SortedSet<Dataset> datasets = new TreeSet<>(new IsCheckedComparator());
		StudyDbVersion studyDbVersion = studyDbVersionRepository.findByStudyAndIsCurrent(study, Boolean.TRUE);
		for (DatasetVersion datasetVersion : studyDbVersion.getDatasetVersions()) {
			datasets.add(datasetVersion.getDataset());
		}
		model.addAttribute("datasets", datasets);
		if (datasetId != -1) {
			Dataset dataset = datasetRepository.findOne(datasetId);
			SortedSet<Field> fields = new TreeSet<>(new DataTypeComparator());
			DatasetVersion datasetVersion = datasetVersionRepository.findByDatasetAndIsCurrent(dataset, Boolean.TRUE);
			fields.addAll(datasetVersion.getFields());
			model.addAttribute("fields", fields);
			model.addAttribute("dataset_id", datasetId);
			
			Check check = checkRepository.findByCheckName("UNIVARIATE_OUTLIER");
			model.addAttribute("data_types", checkParamService.getCheckParam(check, "data_types", dataset));
			model.addAttribute("filter_non_key", checkParamService.getCheckParam(check, "filter_non_key", dataset));
			model.addAttribute("filter_identifying", checkParamService.getCheckParam(check, "filter_identifying", dataset));
			model.addAttribute("sd", checkParamService.getCheckParam(check, "sd", dataset));
			
			model.addAttribute("study_data_types", checkParamService.getCheckParam(check, "data_types", study));
			model.addAttribute("study_filter_non_key", checkParamService.getCheckParam(check, "filter_non_key", study));
			model.addAttribute("study_filter_identifying", checkParamService.getCheckParam(check, "filter_identifying", study));
			model.addAttribute("study_sd", checkParamService.getCheckParam(check, "sd", study));
			
			model.addAttribute("use_study_defaults", checkParamRepository.findByCheckAndDataset(check, dataset).size() == 0);
		}
		return "admin/study/dataset_univariate";
	}
	
	@RequestMapping(value="/save_dataset_univariate", method=RequestMethod.POST)
	public String saveDatasetUnivariate(
			@RequestParam("use_study_defaults") Boolean useStudyDefaults,
			@RequestParam("dataset_id") Long datasetId,
			@RequestParam MultiValueMap<String, String> requestParams,
			Model model) {
		Collection<Long> skipList = new ArrayList<>();
		Map<String, String> datasetParams = new HashMap<>();
		Map<Long, Map<String, String>> fieldParams = new HashMap<>();
		logger.debug("Use study defaults: " + useStudyDefaults);
		for (String key : requestParams.keySet()) {
			//logger.debug(key + ": " + requestParams.getFirst(key));
			if (key.startsWith("skip_")) {
				Long fieldId = Long.parseLong(key.substring(5));
				skipList.add(fieldId);
				logger.debug("Skipping field " + fieldId);
			}
			else if (key.startsWith("param_")) {
				String paramName = key.substring(6);
				String paramValue = requestParams.getFirst(key);
				datasetParams.put(paramName, paramValue);
				logger.debug("Saving dataset parameter " + paramName + " = " + paramValue);
			}
			else if (key.startsWith("sd_field_")) {
				Long fieldId = Long.parseLong(key.substring(9));
				String paramValue = requestParams.getFirst(key);
				Map<String, String> fieldMap = fieldParams.get(fieldId);
				if (fieldMap == null) {
					fieldMap = new HashMap<>();
					fieldParams.put(fieldId, fieldMap);
				}
				fieldMap.put("sd", paramValue);
			}
		}
		Dataset dataset = datasetRepository.findOne(datasetId);
		Check check = checkRepository.findByCheckName("UNIVARIATE_OUTLIER");
		checkConfigurationService.saveDatasetCheckConfiguration(dataset, check, useStudyDefaults, datasetParams, fieldParams, skipList);
		model.addAttribute("message", "Parameters saved");
		return "forward:/admin/study/dataset_univariate";
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
			nextPage = "admin/study/all";
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
	
	@RequestMapping("/anomaly_settings")
	public String anomalySettings(
			@RequestParam(name="study_id") Long studyId,
			Model model) {
		Study study = studyRepository.findOne(studyId);
		model.addAttribute("datasets", datasetRepository.findByStudy(study));
		return "/admin/study/anomaly_settings";
	}
	
	private static final class DataTypeComparator implements Comparator<Field> {
		
		private static Map<String, Integer> ORDINALITY = new HashMap<>();
		
		static {
			ORDINALITY.put("Double", 1);
			ORDINALITY.put("Integer", 2);
			ORDINALITY.put("String", 3);
			ORDINALITY.put("Date", 4);
			ORDINALITY.put("Boolean", 5);
			ORDINALITY.put("MixedType", 6);
			ORDINALITY.put("UnknownType", 7);
		}

		@Override
		public int compare(Field f1, Field f2) {
			int val = ORDINALITY.get(f1.getDataType()).compareTo(ORDINALITY.get(f2.getDataType()));
			if (val == 0) {
				val = f1.getFieldName().compareTo(f2.getFieldName());
			}
			return val;
		}
		
	}
	
	private static final class IsCheckedComparator implements Comparator<Dataset> {

		@Override
		public int compare(Dataset ds1, Dataset ds2) {
			int val = 0;
			if (ds1.getIsChecked() && !ds2.getIsChecked()) {
				val = -1;
			}
			else if (!ds1.getIsChecked() && ds2.getIsChecked()) {
				val = 1;
			}
			else {
				val = ds1.getDatasetName().compareTo(ds2.getDatasetName());
			}
			return val;
		}
		
	}
}
