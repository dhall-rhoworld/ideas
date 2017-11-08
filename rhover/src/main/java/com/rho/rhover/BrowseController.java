package com.rho.rhover;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.rho.rhover.anomaly.AnomalySummary;
import com.rho.rhover.anomaly.AnomalySummaryBuilder;
import com.rho.rhover.anomaly.BivariateCheck;
import com.rho.rhover.anomaly.BivariateCheckRepository;
import com.rho.rhover.study.Dataset;
import com.rho.rhover.study.DatasetRepository;
import com.rho.rhover.study.Site;
import com.rho.rhover.study.SiteRepository;
import com.rho.rhover.study.Study;
import com.rho.rhover.study.StudyRepository;
import com.rho.rhover.study.Subject;
import com.rho.rhover.study.SubjectRepository;

@Controller
@RequestMapping("browse")
public class BrowseController {
	
	@Autowired
	private AnomalySummaryBuilder anomalySummaryBuilder;
	
	@Autowired
	private StudyRepository studyRepository;
	
	@Autowired
	private SiteRepository siteRepository;
	
	@Autowired
	private DatasetRepository datasetRepository;
	
	@Autowired
	private SubjectRepository subjectRepository;
	
	@Autowired
	private BivariateCheckRepository bivariateCheckRepository;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping("/studies")
    public String studies(Model model) {
		model.addAttribute("summaries", anomalySummaryBuilder.getStudySummaries());
		return "browse/studies";
    }
    
    @RequestMapping("/datasets")
    public String studyDatasets(
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
		return "browse/datasets";
    }
    
    @RequestMapping("/sites")
    public String sites(
    		@RequestParam("study_id") Long studyId,
    		Model model) {
		Study study = studyRepository.findOne(studyId);
		model.addAttribute("summaries", anomalySummaryBuilder.getSiteSummaries(studyId));
		model.addAttribute("study", study);
    	return "browse/sites";
    }
    
    @RequestMapping("/subjects")
    public String subjects(
    		@RequestParam("study_id") Long studyId,
    		@RequestParam(name="limit", required=false, defaultValue="100") Integer limit,
    		@RequestParam(name="offset", required=false, defaultValue="0") Integer offset,
    		Model model) {
    	Study study = studyRepository.findOne(studyId);
    	model.addAttribute("study", study);
    	List<AnomalySummary> summaries = anomalySummaryBuilder.getSubjectSummaries(studyId, limit, offset);
    	model.addAttribute("summaries", summaries);
    	model.addAttribute("limit", limit);
    	model.addAttribute("offset", offset);
    	model.addAttribute("from", offset + 1);
    	model.addAttribute("to", offset + summaries.size());
    	int numSubjects = anomalySummaryBuilder.numSubjectsWithAnomalies(studyId);
    	model.addAttribute("total", numSubjects);
    	//logger.debug("Offset: " + offset);
    	if (offset > 0) {
    		int previousOffset = offset - limit;
    		//logger.debug("Previous offset: " + previousOffset);
    		if (previousOffset < 0) {
    			previousOffset = 0;
    		}
    		model.addAttribute("previous_offset", previousOffset);
    	}
		int nextOffset = offset + limit;
		//logger.debug("Next offset: " + nextOffset);
		if (nextOffset < numSubjects - 1) {
			model.addAttribute("next_offset", nextOffset);
		}
    	return "browse/subjects";
    }
    
    @RequestMapping("/data_fields")
    public String dataField(
    			@RequestParam("dataset_id") Long datasetId,
    			@RequestParam(name="site_id", required=false, defaultValue="-1") Long siteId,
    			@RequestParam(name="subject_id", required=false, defaultValue="-1") Long subjectId,
    			Model model) {
    	Dataset dataset = datasetRepository.findOne(datasetId);
    	List<BivariateCheck> checks = new ArrayList<BivariateCheck>();
    	checks.addAll(bivariateCheckRepository.findByDataset1(dataset));
    	checks.addAll(bivariateCheckRepository.findByDataset2(dataset));
		model.addAttribute("dataset", dataset);
		model.addAttribute("bivariate_checks", checks);
		if (siteId == -1 && subjectId == -1) {
			model.addAttribute("summaries", anomalySummaryBuilder.getDataFieldSummaries(datasetId));
		}
		if (siteId != -1) {
			Site site = siteRepository.findOne(siteId);
			model.addAttribute("site", site);
			model.addAttribute("summaries", anomalySummaryBuilder.getDataFieldSummaries(datasetId, site));
		}
		if (subjectId != -1) {
			Subject subject = subjectRepository.findOne(subjectId);
			model.addAttribute("subject", subject);
			model.addAttribute("summaries", anomalySummaryBuilder.getDataFieldSummaries(datasetId, subject));
		}
		return "browse/data_fields";
    }
}
