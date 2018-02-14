package com.rho.rhover.web.controller;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.rho.rhover.common.anomaly.BivariateAnomalyDtoRepository;
import com.rho.rhover.common.anomaly.BivariateCheckRepositoryOld;
import com.rho.rhover.common.anomaly.DataPropertyRepository;
import com.rho.rhover.common.anomaly.UniAnomalyDtoRepository;
import com.rho.rhover.common.check.BivariateCheck;
import com.rho.rhover.common.check.BivariateCheckRepository;
import com.rho.rhover.common.check.Check;
import com.rho.rhover.common.check.CheckRepository;
import com.rho.rhover.common.check.CheckRun;
import com.rho.rhover.common.check.CheckRunRepository;
import com.rho.rhover.common.check.ParamUsedRepository;
import com.rho.rhover.common.study.Field;
import com.rho.rhover.common.study.FieldInstance;
import com.rho.rhover.common.study.FieldInstanceRepository;
import com.rho.rhover.common.study.FieldRepository;
import com.rho.rhover.common.study.Phase;
import com.rho.rhover.common.study.PhaseRepository;
import com.rho.rhover.common.study.DataFieldRepository;
import com.rho.rhover.common.study.Dataset;
import com.rho.rhover.common.study.DatasetRepository;
import com.rho.rhover.common.study.DatasetVersion;
import com.rho.rhover.common.study.DatasetVersionRepository;
import com.rho.rhover.common.study.Site;
import com.rho.rhover.common.study.SiteRepository;
import com.rho.rhover.common.study.Study;
import com.rho.rhover.common.study.Subject;
import com.rho.rhover.common.study.SubjectRepository;

@Controller
@RequestMapping("/anomaly")
public class AnomalyController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private FieldRepository fieldRepository;
	
	@Autowired
	private CheckRunRepository checkRunRepository;
	
	@Autowired
	private CheckRepository checkRepository;
	
	@Autowired
	private UniAnomalyDtoRepository uniAnomalyDtoRepository;
	
	@Autowired
	private BivariateAnomalyDtoRepository bivariateAnomalyDtoRepository;
	
	@Autowired
	private DataPropertyRepository dataPropertyRepository;
	
	@Autowired
	private ParamUsedRepository paramUsedRepository;
	
	@Autowired
	private DataFieldRepository dataFieldRepository;
	
	@Autowired
	private BivariateCheckRepository bivariateCheckRepository;
	
	@Autowired
	private SiteRepository siteRepository;
	
	@Autowired
	private SubjectRepository subjectRepository;
	
	@Autowired
	private DatasetRepository datasetRepository;
	
	@Autowired
	private FieldInstanceRepository fieldInstanceRepository;
	
	@Autowired
	private DatasetVersionRepository datasetVersionRepository;
	
	@Autowired
	private PhaseRepository phaseRepository;
    
    @RequestMapping("/table")
    public String anomalyTable(
    			@RequestParam("field_id") Long fieldId,
    			@RequestParam(name="site_id", required=false, defaultValue="-1") Long siteId,
    			@RequestParam(name="subject_id", required=false, defaultValue="-1") Long subjectId,
    			@RequestParam("dataset_id") Long datasetId,
    			Model model) {
    	Field field = fieldRepository.findOne(fieldId);
    	Dataset dataset = datasetRepository.findOne(datasetId);
    	model.addAttribute("field", field);
    	model.addAttribute("study", field.getStudy());
    	DatasetVersion datasetVersion = field.getCurrentDatasetVersion(dataset);
    	model.addAttribute("dataset", datasetVersion.getDataset());
    	Check check = checkRepository.findByCheckName("UNIVARIATE_OUTLIER");
    	CheckRun checkRun = checkRunRepository.findByCheckAndDatasetVersionAndFieldAndIsLatest(check, datasetVersion, field, Boolean.TRUE);
    	if (siteId == -1 && subjectId == -1) {
    		model.addAttribute("anomalies", uniAnomalyDtoRepository.findByCheckRunId(checkRun.getCheckRunId()));
    	}
    	if (siteId != -1) {
    		model.addAttribute("site", siteRepository.findOne(siteId));
    		model.addAttribute("anomalies", uniAnomalyDtoRepository.findByCheckRunIdAndSiteId(checkRun.getCheckRunId(), siteId));
    	}
    	if (subjectId != -1) {
    		model.addAttribute("subject", subjectRepository.findOne(subjectId));
    		model.addAttribute("anomalies", uniAnomalyDtoRepository.findByCheckRunIdAndSubjectId(checkRun.getCheckRunId(), subjectId));
    	}
    	return "anomaly/table";
    }
    
    @RequestMapping("/bivariate_table")
    public String bivariateAnomalyTable(
    		@RequestParam("field_instance_id_1") Long fieldInstanceId1,
    		@RequestParam("field_instance_id_2") Long fieldInstanceId2,
    		@RequestParam(name="site_id", required=false, defaultValue="-1") Long siteId,
			@RequestParam(name="subject_id", required=false, defaultValue="-1") Long subjectId,
			@RequestParam("dataset_id") Long datasetId,
			Model model) {
    	FieldInstance fieldInstance1 = fieldInstanceRepository.findOne(fieldInstanceId1);
    	FieldInstance fieldInstance2 = fieldInstanceRepository.findOne(fieldInstanceId2);
    	BivariateCheck biCheck = bivariateCheckRepository.findByXFieldInstanceAndYFieldInstance(fieldInstance1, fieldInstance2);
    	DatasetVersion datasetVersion1 = datasetVersionRepository.findByDatasetAndIsCurrent(fieldInstance1.getDataset(), Boolean.TRUE);
    	DatasetVersion datasetVersion2 = datasetVersionRepository.findByDatasetAndIsCurrent(fieldInstance2.getDataset(), Boolean.TRUE);
    	CheckRun checkRun = checkRunRepository.findByBivariateCheckAndDatasetVersionAndBivariateDatasetVersion2AndIsLatest(biCheck, datasetVersion1, datasetVersion2, Boolean.TRUE);
    	model.addAttribute("anomalies", bivariateAnomalyDtoRepository.findByCheckRunId(checkRun.getCheckRunId()));
    	model.addAttribute("study", fieldInstance1.getDataset().getStudy());
    	model.addAttribute("fieldInstance1", fieldInstance1);
    	model.addAttribute("fieldInstance2", fieldInstance2);
    	model.addAttribute("dataset", datasetRepository.findOne(datasetId));
    	return "anomaly/bivariate_table";
    }
    
    @RequestMapping("/beeswarm")
    public String beeswarm(
		    @RequestParam("field_id") Long fieldId,
		    @RequestParam(name="site_id", required=false, defaultValue="-1") Long siteId,
		    @RequestParam(name="subject_id", required=false, defaultValue="-1") Long subjectId,
		    @RequestParam("dataset_id") Long datasetId,
			Model model) {
    	logger.debug("siteID: " + siteId);
    	logger.debug("subjectId: " + subjectId);
    	logger.debug("fieldId: " + fieldId);
    	Field field = dataFieldRepository.findOne(fieldId);
    	Dataset dataset = datasetRepository.findOne(datasetId);
    	model.addAttribute("field", field);
    	DatasetVersion datasetVersion = field.getCurrentDatasetVersion(dataset);
    	model.addAttribute("dataset", datasetVersion.getDataset());
    	logger.debug("datasetVersionId: " + datasetVersion.getDatasetVersionId());
    	logger.debug("dataset: " + datasetVersion.getDataset().getDatasetName());
    	Check check = checkRepository.findByCheckName("UNIVARIATE_OUTLIER");
    	logger.debug("check ID: " + check.getCheckId());
    	CheckRun checkRun = checkRunRepository.findByCheckAndDatasetVersionAndFieldAndIsLatest(check, datasetVersion, field, Boolean.TRUE);
    	if (checkRun == null) {
    		logger.debug("checkRun is null");
    	}
    	model.addAttribute("check_run_id", checkRun.getCheckRunId());
    	model.addAttribute("mean", dataPropertyRepository.findByCheckRunAndDataPropertyName(checkRun, "mean").getDataPropertyValue());
    	model.addAttribute("sd", dataPropertyRepository.findByCheckRunAndDataPropertyName(checkRun, "sd").getDataPropertyValue());
    	model.addAttribute("num_sd", paramUsedRepository.findByCheckRunAndParamName(checkRun, "sd").getParamValue());
    	
    	//TODO: Remove this.  Adding whole field object as an attribute below
    	model.addAttribute("field_name", field.getDisplayName());
    	
    	Study study = datasetVersion.getDataset().getStudy();
    	model.addAttribute("subject_field_name", study.getSubjectField().getDisplayName());
    	model.addAttribute("site_field_name", study.getSiteField().getDisplayName());
    	model.addAttribute("field", field);
    	if (siteId == -1 && subjectId == -1) {
    		model.addAttribute("site_name", "-1");
    		model.addAttribute("subject_name", "-1");
    		model.addAttribute("filter_entity", "none");
    		model.addAttribute("filter_value", "");
    	}
    	if (siteId != -1) {
    		Site site = siteRepository.findOne(siteId);
    		model.addAttribute("site_name", site.getSiteName());
    		model.addAttribute("subject_name", "-1");
    		model.addAttribute("site", site);
    		model.addAttribute("filter_entity", "site");
    		model.addAttribute("filter_value", site.getSiteName());
    	}
    	if (subjectId != -1) {
    		Subject subject = subjectRepository.findOne(subjectId);
    		model.addAttribute("subject_name", subject.getSubjectName());
    		model.addAttribute("site_name", "-1");
    		model.addAttribute("subject", subject);
    		model.addAttribute("filter_entity", "subject");
    		model.addAttribute("filter_value", subject.getSubjectName());
    	}
    	
    	model.addAttribute("sites", siteRepository.findByStudy(study));
    	List<Phase> phases = phaseRepository.findByStudy(study);
    	Collections.sort(phases);
    	model.addAttribute("phases", phases);
    	return "anomaly/beeswarm";
    }
    
    @RequestMapping("/bivariate_scatter")
    public String bivariateScatterPlot(
    		@RequestParam(name="field_instance_id_1", required=false, defaultValue="-1") Long fieldInstanceId1,
    		@RequestParam(name="field_instance_id_2", required=false, defaultValue="-1") Long fieldInstanceId2,
    		@RequestParam(name="field_name_1", required=false, defaultValue="") String fieldName1,
    		@RequestParam(name="field_name_2", required=false, defaultValue="") String fieldName2,
    		@RequestParam(name="dataset_name_1", required=false, defaultValue="") String datasetName1,
    		@RequestParam(name="dataset_name_2", required=false, defaultValue="") String datasetName2,
    		@RequestParam(name="site_id", required=false, defaultValue="-1") Long siteId,
			@RequestParam(name="subject_id", required=false, defaultValue="-1") Long subjectId,
			@RequestParam("dataset_id") Long datasetId,
			Model model) {
    	Dataset dataset = datasetRepository.findOne(datasetId);
    	Study study = dataset.getStudy();
    	FieldInstance fieldInstance1 = null;
    	if (fieldInstanceId1.equals(-1L)) {
    		Field field1 = fieldRepository.findByStudyAndFieldName(study, fieldName1);
    		Dataset dataset1 = datasetRepository.findByStudyAndDatasetName(study, datasetName1);
    		fieldInstance1 = fieldInstanceRepository.findByFieldAndDataset(field1, dataset1);
    	}
    	else {
    		fieldInstance1 = fieldInstanceRepository.findOne(fieldInstanceId1);
    	}
    	FieldInstance fieldInstance2 = null;
    	if (fieldInstanceId2.equals(-1L)) {
    		Field field2 = fieldRepository.findByStudyAndFieldName(study, fieldName2);
    		Dataset dataset2 = datasetRepository.findByStudyAndDatasetName(study, datasetName2);
    		fieldInstance2 = fieldInstanceRepository.findByFieldAndDataset(field2, dataset2);
    	}
    	else {
    		fieldInstance2 = fieldInstanceRepository.findOne(fieldInstanceId2);
    	}
    	BivariateCheck biCheck = bivariateCheckRepository.findByXFieldInstanceAndYFieldInstance(fieldInstance1, fieldInstance2);
    	DatasetVersion datasetVersion1 = datasetVersionRepository.findByDatasetAndIsCurrent(fieldInstance1.getDataset(), Boolean.TRUE);
    	DatasetVersion datasetVersion2 = datasetVersionRepository.findByDatasetAndIsCurrent(fieldInstance2.getDataset(), Boolean.TRUE);
    	CheckRun checkRun = checkRunRepository.findByBivariateCheckAndDatasetVersionAndBivariateDatasetVersion2AndIsLatest(biCheck, datasetVersion1, datasetVersion2, Boolean.TRUE);
    	model.addAttribute("check_run_id", checkRun.getCheckRunId());
    	model.addAttribute("study", fieldInstance1.getDataset().getStudy());
    	model.addAttribute("fieldInstance1", fieldInstance1);
    	model.addAttribute("fieldInstance2", fieldInstance2);
    	model.addAttribute("dataset", dataset);
    	model.addAttribute("field_name_1", fieldInstance1.getField().getDisplayName());
    	model.addAttribute("field_name_2", fieldInstance2.getField().getDisplayName());
    	model.addAttribute("slope", dataPropertyRepository.findByCheckRunAndDataPropertyName(checkRun, "slope").getDataPropertyValue());
    	model.addAttribute("intercept", dataPropertyRepository.findByCheckRunAndDataPropertyName(checkRun, "intercept").getDataPropertyValue());
    	model.addAttribute("cutoff_residual", dataPropertyRepository.findByCheckRunAndDataPropertyName(checkRun, "cutoff_residual").getDataPropertyValue());  	
    	//model.addAttribute("mean_res", dataPropertyRepository.findByCheckRunAndDataPropertyName(checkRun, "mean_res").getDataPropertyValue());
    	model.addAttribute("sd_res", dataPropertyRepository.findByCheckRunAndDataPropertyName(checkRun, "sd_res").getDataPropertyValue());
    	String heteroschedastic = dataPropertyRepository.findByCheckRunAndDataPropertyName(checkRun, "heteroschedastic").getDataPropertyValue();
    	model.addAttribute("heteroschedastic", heteroschedastic);
    	if (heteroschedastic.equals("TRUE")) {
    		model.addAttribute("lambda", dataPropertyRepository.findByCheckRunAndDataPropertyName(checkRun, "lambda").getDataPropertyValue());
    	}
    	else {
    		model.addAttribute("lambda", "null");
    	}
    	model.addAttribute("sd_residual", paramUsedRepository.findByCheckRunAndParamName(checkRun, "sd-residual").getParamValue());
    	model.addAttribute("num_nearest_neighbors", paramUsedRepository.findByCheckRunAndParamName(checkRun, "num-nearest-neighbors").getParamValue());
    	model.addAttribute("sd_density", paramUsedRepository.findByCheckRunAndParamName(checkRun, "sd-density").getParamValue());
    	return "anomaly/bivariate_scatter";
    }
    
    @RequestMapping("/boxplot")
    public String boxplot(
		    @RequestParam("data_field_id") Long dataFieldId,
		    @RequestParam(name="site_id", required=false, defaultValue="-1") Long siteId,
		    @RequestParam(name="subject_id", required=false, defaultValue="-1") Long subjectId,
			Model model) {
    	Field dataField = dataFieldRepository.findOne(dataFieldId);
    	model.addAttribute("data_field", dataField);
    	if (siteId == -1 && subjectId == -1) {
    		model.addAttribute("site_name", "-1");
    	}
    	if (siteId != -1) {
    		Site site = siteRepository.findOne(siteId);
    		model.addAttribute("site", site);
    		model.addAttribute("site_name", site.getSiteName());
    		model.addAttribute("subject_name", "-1");
    	}
    	if (subjectId != -1) {
    		Subject subject = subjectRepository.findOne(subjectId);
    		model.addAttribute("subject", subject);
    		model.addAttribute("subject_name", subject.getSubjectName());
    		model.addAttribute("site_name", "-1");
    	}
    	return "anomaly/boxplot";
    }
    
    @RequestMapping("/scatterplot")
    public String scatterPlot(
    		@RequestParam("bivariate_check_id") Long bivariateCheckId,
    		Model model) {
    	model.addAttribute("bivariate_check", bivariateCheckRepository.findOne(bivariateCheckId));
    	return "anomaly/scatterplot";
    }
}
