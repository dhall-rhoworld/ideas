package com.rho.rhover.web.controller;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.rho.rhover.common.anomaly.BivariateAnomalyDtoRepository;
import com.rho.rhover.common.anomaly.UniAnomalyDtoRepository;
import com.rho.rhover.common.check.BivariateCheck;
import com.rho.rhover.common.check.BivariateCheckRepository;
import com.rho.rhover.common.check.Check;
import com.rho.rhover.common.check.CheckRepository;
import com.rho.rhover.common.check.CheckRun;
import com.rho.rhover.common.check.CheckRunRepository;
import com.rho.rhover.common.study.Field;
import com.rho.rhover.common.study.FieldInstance;
import com.rho.rhover.common.study.FieldInstanceRepository;
import com.rho.rhover.common.study.FieldRepository;
import com.rho.rhover.common.study.DataFieldRepository;
import com.rho.rhover.common.study.Dataset;
import com.rho.rhover.common.study.DatasetRepository;
import com.rho.rhover.common.study.DatasetVersion;
import com.rho.rhover.common.study.DatasetVersionRepository;
import com.rho.rhover.common.study.Site;
import com.rho.rhover.common.study.SiteRepository;
import com.rho.rhover.common.study.Study;
import com.rho.rhover.common.study.StudyRepository;
import com.rho.rhover.common.study.Subject;
import com.rho.rhover.common.study.SubjectRepository;
import com.rho.rhover.web.dto.AnomalySummaryBuilder;

/**
 * Controller for pages under the Anomalies menu item
 * @author dhall
 *
 */
@Controller
@RequestMapping("/anomaly")
public class AnomalyController {
	
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
	private AnomalySummaryBuilder anomalySummaryBuilder;
	
	@Autowired
	private StudyRepository studyRepository;
	
	/**
	 * Fetches anomaly summary encompassing all studies
	 * @param model Model
	 * @return Page name
	 */
	@RequestMapping("/global_summary")
    public String getGlobalAnomalySummary(Model model, Principal user, HttpServletRequest request) {
		model.addAttribute("summaries", anomalySummaryBuilder.getStudySummaries());
		return "anomaly/global_summary";
    }
	
	/**
	 * Fetches anomaly summary for a particular study.  Note that only one of the following
	 * three ID parameters should be set by referring link.
	 * @param studyId If set, fetches summary of all anomalies in study
	 * @param siteId If set, fetches summary of anomalies from the study limited to this site
	 * @param subjectId If set, fetches summary of anomalies from the study limited to this subject
	 * @param model Model
	 * @return Page name
	 */
    @RequestMapping("/study_summary")
    public String getStudyAnomalySummary(
	    		@RequestParam(name="study_id", required=false, defaultValue="-1") Long studyId,
	    		@RequestParam(name="site_id", required=false, defaultValue="-1") Long siteId,
	    		@RequestParam(name="subject_id", required=false, defaultValue="-1") Long subjectId,
	    		Model model) {
    	if (studyId != -1) {
    		Study study = studyRepository.findOne(studyId);
    		model.addAttribute("study", study);
        	model.addAttribute("summaries", anomalySummaryBuilder.getDatasetSummaries(study));
    	}
    	if (siteId != -1) {
    		Site site = siteRepository.findOne(siteId);
    		model.addAttribute("site", site);
    		model.addAttribute("summaries", anomalySummaryBuilder.getDatasetSummaries(site));
    	}
    	if (subjectId != -1) {
    		Subject subject = subjectRepository.findOne(subjectId);
    		model.addAttribute("subject", subject);
    		model.addAttribute("summaries", anomalySummaryBuilder.getDatasetSummaries(subject));
    	}
		return "anomaly/study_summary";
    }
    
	/**
	 * Fetches summary of anomalies for fields in a particular dataset.  Note that only one of the following
	 * three ID parameters should be set by referring link.
	 * @param datasetId If set, fetches summary of all anomalies
	 * @param siteId If set, fetches summary of anomalies from the given site
	 * @param subjectId If set, fetches summary of anomalies from the given subject
	 * @param model Model
	 * @return Page name
	 */
    @RequestMapping("/dataset_summary")
    public String getDatasetAnomalySummary(
    			@RequestParam("dataset_id") Long datasetId,
    			@RequestParam(name="site_id", required=false, defaultValue="-1") Long siteId,
    			@RequestParam(name="subject_id", required=false, defaultValue="-1") Long subjectId,
    			Model model) {
    	Dataset dataset = datasetRepository.findOne(datasetId);
    	model.addAttribute("dataset", dataset);
		if (siteId == -1 && subjectId == -1) {
			model.addAttribute("univariateSummaries", anomalySummaryBuilder.getUnivariateDataFieldSummaries(datasetId));
			model.addAttribute("bivariateSummaries", anomalySummaryBuilder.getBivariateDataFieldSummaries(datasetId));
		}
		if (siteId != -1) {
			Site site = siteRepository.findOne(siteId);
			model.addAttribute("site", site);
			model.addAttribute("univariateSummaries", anomalySummaryBuilder.getUnivariateDataFieldSummaries(datasetId, site));
			//model.addAttribute("bivariateSummaries", anomalySummaryBuilder.getBivariateDataFieldSummaries(datasetId, site));
		}
		if (subjectId != -1) {
			Subject subject = subjectRepository.findOne(subjectId);
			model.addAttribute("subject", subject);
			model.addAttribute("univariateSummaries", anomalySummaryBuilder.getUnivariateDataFieldSummaries(datasetId, subject));
			//model.addAttribute("bivariateSummaries", anomalySummaryBuilder.getBivariateDataFieldSummaries(datasetId, subject));
		}
		return "anomaly/dataset_summary";
    }
    
    /**
     * Fetches individual anomalies for a given field.  Note that only one of siteId and subjecId should
     * be set.
     * @param fieldId ID of field
     * @param siteId If set, fetches anomalies associated with site and field only
     * @param subjectId If set, fetches anomalies associated with site and subject only
     * @param datasetId ID of dataset
     * @param model Model
     * @return Page name
     */
    @RequestMapping("/univariate_table")
    public String univariateAnomalyTable(
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
    	return "anomaly/univariate_table";
    }
    
    /**
     * Fetches individual anomalies for a pair of field
     * @param fieldInstanceId1 ID of first field instance
     * @param fieldInstanceId2 ID of second field instance
     * @param siteId If set, only fetches anomalies associated with given site
     * @param subjectId If set, only fetches anomalies associated with given subject
     * @param datasetId ID of dataset
     * @param model Model
     * @return Page name
     */
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
