package com.rho.rhover.web.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.rho.rhover.common.anomaly.Anomaly;
import com.rho.rhover.common.anomaly.AnomalyRepository;
import com.rho.rhover.common.anomaly.AnomalyRepositoryOld;
import com.rho.rhover.common.anomaly.BivariateCheckRepositoryOld;
import com.rho.rhover.common.anomaly.DataPropertyRepository;
import com.rho.rhover.common.anomaly.UniAnomalyDtoRepository;
import com.rho.rhover.common.check.Check;
import com.rho.rhover.common.check.CheckRepository;
import com.rho.rhover.common.check.CheckRun;
import com.rho.rhover.common.check.CheckRunRepository;
import com.rho.rhover.common.check.ParamUsedRepository;
import com.rho.rhover.common.study.Field;
import com.rho.rhover.common.study.FieldRepository;
import com.rho.rhover.common.study.DataFieldRepository;
import com.rho.rhover.common.study.Dataset;
import com.rho.rhover.common.study.DatasetRepository;
import com.rho.rhover.common.study.DatasetVersion;
import com.rho.rhover.common.study.Site;
import com.rho.rhover.common.study.SiteRepository;
import com.rho.rhover.common.study.Study;
import com.rho.rhover.common.study.StudyDataRepository;
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
	private AnomalyRepository anomalyRepository;
	
	@Autowired
	private DataPropertyRepository dataPropertyRepository;
	
	@Autowired
	private ParamUsedRepository paramUsedRepository;
	
	@Autowired
	private AnomalyRepositoryOld anomalyRepositoryOld;
	
	@Autowired
	private DataFieldRepository dataFieldRepository;
	
	@Autowired
	private BivariateCheckRepositoryOld bivariateCheckRepository;
	
	@Autowired
	private StudyDataRepository studyDataRepository;
	
	@Autowired
	private SiteRepository siteRepository;
	
	@Autowired
	private SubjectRepository subjectRepository;
	
	@Autowired
	private DatasetRepository datasetRepository;
    
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
    	model.addAttribute("id_fields", field.getStudy().getUniqueIdentifierFields());
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
    	model.addAttribute("field_name", field.getDisplayName());
    	Study study = datasetVersion.getDataset().getStudy();
    	model.addAttribute("subject_field_name", study.getSubjectField().getDisplayName());
    	model.addAttribute("site_field_name", study.getSiteField().getDisplayName());
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
    	return "anomaly/beeswarm";
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
