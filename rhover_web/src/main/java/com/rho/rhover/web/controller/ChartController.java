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

import com.rho.rhover.common.anomaly.DataPropertyRepository;
import com.rho.rhover.common.check.Check;
import com.rho.rhover.common.check.CheckRepository;
import com.rho.rhover.common.check.CheckRun;
import com.rho.rhover.common.check.CheckRunRepository;
import com.rho.rhover.common.check.ParamUsedRepository;
import com.rho.rhover.common.study.DataFieldRepository;
import com.rho.rhover.common.study.Dataset;
import com.rho.rhover.common.study.DatasetRepository;
import com.rho.rhover.common.study.DatasetVersion;
import com.rho.rhover.common.study.Field;
import com.rho.rhover.common.study.Phase;
import com.rho.rhover.common.study.PhaseRepository;
import com.rho.rhover.common.study.Site;
import com.rho.rhover.common.study.SiteRepository;
import com.rho.rhover.common.study.Study;
import com.rho.rhover.common.study.Subject;
import com.rho.rhover.common.study.SubjectRepository;

/**
 * Controller for displaying data charts
 * @author dhall
 *
 */
@Controller
@RequestMapping("/chart")
public class ChartController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private DatasetRepository datasetRepository;
	
	@Autowired
	private CheckRepository checkRepository;
	
	@Autowired
	private CheckRunRepository checkRunRepository;
	
	@Autowired
	private SiteRepository siteRepository;
	
	@Autowired
	private SubjectRepository subjectRepository;
	
	@Autowired
	private DataFieldRepository dataFieldRepository;
	
	@Autowired
	private DataPropertyRepository dataPropertyRepository;
	
	@Autowired
	private ParamUsedRepository paramUsedRepository;
	
	@Autowired
	private PhaseRepository phaseRepository;
	
	/**
	 * Fetch data for creating a univariate beeswarm chart.
	 * @param fieldId ID of field to plot
	 * @param siteId
	 * @param subjectId
	 * @param datasetId ID of dataset
	 * @param model Model
	 * @return Page name
	 */
    @RequestMapping("/univariate_beeswarm")
    public String univariateBeeswarm(
		    @RequestParam("field_id") Long fieldId,
		    @RequestParam(name="site_id", required=false, defaultValue="-1") Long siteId,
		    @RequestParam(name="subject_id", required=false, defaultValue="-1") Long subjectId,
		    @RequestParam("dataset_id") Long datasetId,
			Model model) {
    	Field field = dataFieldRepository.findOne(fieldId);
    	Dataset dataset = datasetRepository.findOne(datasetId);
    	model.addAttribute("field", field);
    	DatasetVersion datasetVersion = field.getCurrentDatasetVersion(dataset);
    	model.addAttribute("dataset", datasetVersion.getDataset());
    	Check check = checkRepository.findByCheckName("UNIVARIATE_OUTLIER");
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
    	model.addAttribute("phase_field_name", study.getPhaseField().getDisplayName());
    	model.addAttribute("record_id_field_name", study.getRecordIdField().getDisplayName());
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
    	return "chart/univariate_beeswarm";
    }

}
