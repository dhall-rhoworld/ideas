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

import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.rho.rhover.common.check.BivariateCheck;
import com.rho.rhover.common.check.BivariateCheckRepository;
import com.rho.rhover.common.check.Check;
import com.rho.rhover.common.check.CheckParam;
import com.rho.rhover.common.check.CheckParamRepository;
import com.rho.rhover.common.check.CheckParamService;
import com.rho.rhover.common.check.CheckRepository;
import com.rho.rhover.common.session.Event;
import com.rho.rhover.common.session.EventRepository;
import com.rho.rhover.common.session.UserSession;
import com.rho.rhover.common.session.UserSessionRepository;
import com.rho.rhover.common.study.DataLocation;
import com.rho.rhover.common.study.DataLocationRepository;
import com.rho.rhover.common.study.Dataset;
import com.rho.rhover.common.study.DatasetRepository;
import com.rho.rhover.common.study.DatasetVersion;
import com.rho.rhover.common.study.DatasetVersionRepository;
import com.rho.rhover.common.study.Field;
import com.rho.rhover.common.study.FieldInstance;
import com.rho.rhover.common.study.FieldInstanceRepository;
import com.rho.rhover.common.study.FieldRepository;
import com.rho.rhover.common.study.MergeField;
import com.rho.rhover.common.study.MergeFieldRepository;
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
	
	@Autowired
	private FieldInstanceRepository fieldInstanceRepository;
	
	@Autowired
	private BivariateCheckRepository bivariateCheckRepository;
	
	@Autowired
	private MergeFieldRepository mergeFieldRepository;
	
	@Autowired
	private DataSource dataSource;
	
	@Autowired
	private UserSessionRepository userSessionRepository;
	
	@Autowired
	private EventRepository eventRepository;
	
	@Autowired
	private DataLocationRepository dataLocationRepository;

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
		model.addAttribute("sd", checkParamService.getCheckParam(check, "sd", study));
		model.addAttribute("min_univariate", checkParamService.getCheckParam(check, "min-univariate", study));
		return "admin/study/study_univariate";
	}
	
	@RequestMapping("/critical_datasets")
	public String showCriticalDatasets (
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
		return "admin/study/critical_datasets";
	}
	
	@RequestMapping(value="/save_study_univariate", method=RequestMethod.POST)
	public String saveStudyUnivariate(
			@RequestParam MultiValueMap<String, String> requestParams,
			Model model, HttpSession session) {
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
		UserSession userSession = userSessionRepository.findByWebSessionId(session.getId());
		checkConfigurationService.saveStudyCheckConfiguration(study, check, studyParams, datasetIds, userSession);
		model.addAttribute("message", "Study parameters saved");
		return "forward:/admin/study/study_univariate";
	}
	
	@RequestMapping(value="/save_critical_datasets", method=RequestMethod.POST)
	public String saveCriticalDatasets(
			@RequestParam MultiValueMap<String, String> requestParams,
			Model model) {
		
		// Un-flag as critical all datasets in study
		Long studyId = Long.parseLong(requestParams.getFirst("study_id"));
		Study study = studyRepository.findOne(studyId);
		Iterable<Dataset> datasets = datasetRepository.findByStudy(study);
		for (Dataset dataset : datasets) {
			dataset.setIsCritical(Boolean.FALSE);
			datasetRepository.save(dataset);
		}
		
		// Flag all selected datasets as critical
		for (String key : requestParams.keySet()) {
			if (key.startsWith("check_dataset-")) {
				Long datasetId = Long.parseLong(key.substring(14));
				Dataset dataset = datasetRepository.findOne(datasetId);
				dataset.setIsCritical(Boolean.TRUE);
				datasetRepository.save(dataset);
			}
		}

		model.addAttribute("message", "Critical datasets flagged");
		return "forward:/admin/study/critical_datasets";
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
			model.addAttribute("min_univariate", checkParamService.getCheckParam(check, "min-univariate", dataset));
			model.addAttribute("sd", checkParamService.getCheckParam(check, "sd", dataset));
			
			model.addAttribute("study_data_types", checkParamService.getCheckParam(check, "data_types", study));
			model.addAttribute("study_min_univariate", checkParamService.getCheckParam(check, "min-univariate", study));
			model.addAttribute("study_sd", checkParamService.getCheckParam(check, "sd", study));
			
			model.addAttribute("use_study_defaults", checkParamRepository.findByCheckAndDatasetAndIsCurrent(check, dataset, Boolean.TRUE).size() == 0);
		}
		return "admin/study/dataset_univariate";
	}
	
	@RequestMapping(value="/save_dataset_univariate", method=RequestMethod.POST)
	public String saveDatasetUnivariate(
			@RequestParam("use_study_defaults") Boolean useStudyDefaults,
			@RequestParam("dataset_id") Long datasetId,
			@RequestParam MultiValueMap<String, String> requestParams,
			Model model, HttpSession session) {
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
		UserSession userSession = userSessionRepository.findByWebSessionId(session.getId());
		checkConfigurationService.saveDatasetCheckConfiguration(dataset, check, useStudyDefaults, datasetParams, fieldParams, skipList, userSession);
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
			@RequestParam("phase_field_name") String phaseFieldName,
			@RequestParam("record_id_field_name") String recordIdFieldName,
			@RequestParam(name="query_file_path", required=false, defaultValue="") String queryFilePath,
			Model model, HttpSession session) {
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
		study.setRecordIdFieldName(recordIdFieldName);
		if (queryFilePath.length() > 0) {
			study.setQueryFilePath(queryFilePath);
		}
		study.setFormFieldName(formFieldName);
		study.setSiteFieldName(siteFieldName);
		study.setSubjectFieldName(subjectFieldName);
		study.setPhaseFieldName(phaseFieldName);
		logger.info("Session ID: " + session.getId());
		UserSession userSession = userSessionRepository.findByWebSessionId(session.getId());
		study.setUserSession(userSession);
		studyRepository.save(study);
		
		// Form field
		Long fieldId = nextFieldPkValue();
		Field formField = new Field(fieldId++, formFieldName, formFieldName, study, "String");
		fieldRepository.save(formField);
		study.setFormField(formField);
		
		// Site field
		Field siteField = new Field(fieldId++, siteFieldName, siteFieldName, study, "String");
		fieldRepository.save(siteField);
		study.setSiteField(siteField);
		
		// Subject field
		Field subjectField = new Field(fieldId++, subjectFieldName, subjectFieldName, study, "String");
		fieldRepository.save(subjectField);
		study.setSubjectField(subjectField);
		
		// Phase field
		Field phaseField = new Field(fieldId++, phaseFieldName, phaseFieldName, study, "String");
		fieldRepository.save(phaseField);
		study.setPhaseField(phaseField);
		
		// Record ID field
		Field recordIdField = new Field(fieldId++, recordIdFieldName, recordIdFieldName, study, "String");
		fieldRepository.save(recordIdField);
		study.setRecordIdField(recordIdField);
		
		studyRepository.save(study);
		model.addAttribute("study", study);
		Event event = Event.newAddStudyEvent(userSession, study);
		eventRepository.save(event);
		return nextPage;
	}
	
	@RequestMapping(value="/add_data_folder", method=RequestMethod.POST)
	public String saveDataFolder(
			@RequestParam("study_id") Long studyId,
			@RequestParam("folder_path") String folderPath,
			@RequestParam(name="include_sas", required=false, defaultValue="off") String includeSasFiles,
			@RequestParam(name="include_csv", required=false, defaultValue="off") String includeCsvFiles,
			Model model, HttpSession session) {
		logger.debug("studyId: " + studyId);
		logger.debug("folderPath: " + folderPath);
		Study study = studyRepository.findOne(studyId);
		DataLocation location = new DataLocation();
		location.setFolderPath(folderPath);
		location.setStudy(study);
		UserSession userSession = userSessionRepository.findByWebSessionId(session.getId());
		location.setUserSession(userSession);
		if (includeSasFiles.equals("on")) {
			location.setIncludeSasFiles(Boolean.TRUE);
		}
		else {
			location.setIncludeSasFiles(Boolean.FALSE);
		}
		if (includeCsvFiles.equals("on")) {
			location.setIncludeCsvFiles(Boolean.TRUE);
		}
		else {
			location.setIncludeCsvFiles(Boolean.FALSE);
		}
		dataLocationRepository.save(location);
		study.getDataLocations().add(location);
		studyRepository.save(study);
		model.addAttribute("study", study);
		Event event = Event.newAddDataLocationEvent(userSession, location);
		eventRepository.save(event);
		String message = "The system will process the data files in the new data folder.  You will be notified after processing " +
				"is complete.  Afterwards, you will be able to select key variables and configure data checks for these datasets.";
		model.addAttribute("message", message);
		return "admin/study/data_locations";
	}
	
	@RequestMapping("/anomaly_settings")
	public String anomalySettings(
			@RequestParam(name="study_id") Long studyId,
			Model model) {
		Study study = studyRepository.findOne(studyId);
		model.addAttribute("datasets", datasetRepository.findByStudy(study));
		return "/admin/study/anomaly_settings";
	}
	
	@RequestMapping("/correlations")
	public String correlations(
			@RequestParam(name="study_id") Long studyId,
			Model model) {
		model.addAttribute("study", studyRepository.findOne(studyId));
		return "/admin/study/correlations";
	}
	
	@RequestMapping("/bivariates")
	public String showBivariates(
			@RequestParam(name="study_id") Long studyId,
			Model model) {
		Study study = studyRepository.findOne(studyId);
		List<BivariateCheck> checks = bivariateCheckRepository.findByStudy(study);
		model.addAttribute("study", study);
		model.addAttribute("checks", checks);
		return "/admin/study/all_bivariates";
	}
	
	@RequestMapping("/add_bivariate")
	public String addBivariate(
			@RequestParam(name="study_id") Long studyId,
			Model model
	) {
		Study study = studyRepository.findOne(studyId);
		model.addAttribute("study", study);
		model.addAttribute("datasets", datasetRepository.findByStudy(study));
		return "/admin/study/new_bivariate";
	}
	
	@RequestMapping(value="/save_bivariate", method=RequestMethod.POST)
	public String saveBivariate(
		@RequestParam(name="variable_x") Long fieldInstanceIdX,
		@RequestParam(name="dataset_y", required=false, defaultValue="") Long datasetYId,
		@RequestParam(name="variable_y", required=false, defaultValue="") String fieldInstanceIdsYStr,
		@RequestParam(name="y_type", required=false, defaultValue="none") String yType,
		@RequestParam(name="use_study_defaults", required=false, defaultValue="off") String useDefaultParams,
		@RequestParam(name="param_sd-residual", required=false, defaultValue="") String sdResidual,
		@RequestParam(name="param_sd-density", required=false, defaultValue="") String sdDensity,
		@RequestParam MultiValueMap<String, String> params,
		Model model, HttpSession session
	) {
		
		// Fetch X field instance
		FieldInstance fieldInstanceX = fieldInstanceRepository.findOne(fieldInstanceIdX);
		
		// Build map to help create any new merge field mappings.
		// Keys are dataset name and values are field IDs select by user
		Map<String, List<Long>> mergeFieldIds = new HashMap<>();
		for (String key : params.keySet()) {
			if (key.startsWith("cb_merge")) {
				logger.debug("Found merge field parameter: " + key);
				String[] tokens = key.split("_");
				String datasetName = tokens[2];
				logger.debug("Dataset name: " + datasetName);
				List<Long> ids = mergeFieldIds.get(datasetName);
				if (ids == null) {
					ids = new ArrayList<>();
					mergeFieldIds.put(datasetName, ids);
				}
				ids.add(new Long(tokens[3]));
			}
		}
		
		// Save any new merge field mappings
		Study study = fieldInstanceX.getField().getStudy();
		for (String datasetName : mergeFieldIds.keySet()) {
			Dataset dataset = datasetRepository.findByStudyAndDatasetName(study, datasetName);
			List<Long> fieldIds = mergeFieldIds.get(datasetName);
			for (Long fieldId : fieldIds) {
				Field field = fieldRepository.findOne(fieldId);
				FieldInstance fieldInstance1 = fieldInstanceRepository.findByFieldAndDataset(field, fieldInstanceX.getDataset());
				FieldInstance fieldInstance2 = fieldInstanceRepository.findByFieldAndDataset(field, dataset);
				logger.debug("Saving merge field for " + fieldInstance1.getDataset().getDatasetName()
						+ " (" + fieldInstance1.getField().getDisplayName() + ") to " + fieldInstance2.getDataset().getDatasetName()
						+ " (" + fieldInstance2.getField().getDisplayName() + ")");
				
				MergeField mergeField = new MergeField();
				mergeField.setFieldInstance1(fieldInstance1);
				mergeField.setFieldInstance2(fieldInstance2);
				mergeFieldRepository.save(mergeField);
			}
		}
		
		// Fetch Y field instance(s)
		List<FieldInstance> fieldInstancesY = new ArrayList<>();
		
		// ----- Case: User searched for variable for selected from multi-select
		if (fieldInstanceIdsYStr.length() > 0) {
			String[] ids = fieldInstanceIdsYStr.split(",");
			for (int i = 0; i < ids.length; i++) {
				Long id = new Long(ids[i]);
				fieldInstancesY.add(fieldInstanceRepository.findOne(id));
			}
		}
		
		// ----- Case: User selected all continuous or all numeric variables from dataset
		else {
			Dataset dataset = datasetRepository.findOne(datasetYId);
			fieldInstancesY.addAll(fieldInstanceRepository.findByDatasetAndDataType(dataset, "Double"));
			if (yType.equals("numeric")) {
				fieldInstancesY.addAll(fieldInstanceRepository.findByDatasetAndDataType(dataset, "Integer"));
			}
		}
		
		// Save checks
		Check check = checkRepository.findByCheckName("BIVARIATE_OUTLIER");
		List<BivariateCheck> duplicates = new ArrayList<>();
		for (FieldInstance instanceY : fieldInstancesY) {
			
			// Case: Check already exists
			BivariateCheck bivariateCheck = bivariateCheckRepository.findByXFieldInstanceAndYFieldInstance(fieldInstanceX, instanceY);
			if (bivariateCheck != null) {
				duplicates.add(bivariateCheck);
				logger.info("Duplicate check: "
						+ fieldInstanceX.getField().getDisplayName() + " and "
						+ instanceY.getField().getDisplayName());
				continue;
			}
			
			bivariateCheck = new BivariateCheck(fieldInstanceX, instanceY, check, study);
			bivariateCheckRepository.save(bivariateCheck);
			
			// Save parameters
			if (useDefaultParams.equals("off")) {
				UserSession userSession = userSessionRepository.findByWebSessionId(session.getId());
				
				// sd-residual
				CheckParam param = new CheckParam("sd-residual", "BIVARIATE", check, userSession);
				param.setBivariateCheck(bivariateCheck);
				param.setParamValue(sdResidual);
				checkParamRepository.save(param);
				bivariateCheck.getCheckParams().put("sd-residual", param);
				
				// sd-density
				param = new CheckParam("sd-density", "BIVARIATE", check, userSession);
				param.setBivariateCheck(bivariateCheck);
				param.setParamValue(sdDensity);
				checkParamRepository.save(param);
				bivariateCheck.getCheckParams().put("sd-density", param);
				
				bivariateCheckRepository.save(bivariateCheck);
			}
		}
		
		// Report any existing checks back to user
		if (duplicates.size() > 0) {
			List<String> duplicateNames = new ArrayList<>();
			for (BivariateCheck biCheck : duplicates) {
				duplicateNames.add(
					biCheck.getxFieldInstance().getField().getTruncatedDisplayName()
					+ " and "
					+ biCheck.getyFieldInstance().getField().getTruncatedDisplayName());
			}
			model.addAttribute("duplicates", duplicateNames);
		}
		
		return "forward:/admin/study/bivariates?study_id=" + study.getStudyId();
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
	
	private Long nextFieldPkValue() {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		String sql = "select max(field_id) from field";
		Long max = jdbcTemplate.queryForObject(sql, Long.class);
		if (max == null) {
			max = 0L;
		}
		return max + 1;
	}
}
