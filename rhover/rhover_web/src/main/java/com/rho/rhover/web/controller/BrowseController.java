package com.rho.rhover.web.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.rho.rhover.common.anomaly.BivariateCheckOld;
import com.rho.rhover.common.anomaly.BivariateCheckRepositoryOld;
import com.rho.rhover.common.study.Dataset;
import com.rho.rhover.common.study.DatasetRepository;
import com.rho.rhover.common.study.Site;
import com.rho.rhover.common.study.SiteRepository;
import com.rho.rhover.common.study.Study;
import com.rho.rhover.common.study.StudyRepository;
import com.rho.rhover.common.study.Subject;
import com.rho.rhover.common.study.SubjectRepository;
import com.rho.rhover.web.dto.AnomalySummary;
import com.rho.rhover.web.dto.AnomalySummaryBuilder;

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
	private BivariateCheckRepositoryOld bivariateCheckRepository;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

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
    	model.addAttribute("dataset", dataset);
		if (siteId == -1 && subjectId == -1) {
			model.addAttribute("univariateSummaries", anomalySummaryBuilder.getUnivariateDataFieldSummaries(datasetId, true));
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
		return "browse/data_fields";
    }
    
    @RequestMapping("/bivariate")
    public String showBivariate(
    		@RequestParam("field_instance_id_1") Long fieldInstanceId1,
    		@RequestParam("field_instance_id_2") String fieldInstanceIdString,
    		@RequestParam(name="page", required=false, defaultValue="0") Integer page,
    		Model model) {
    	model.addAttribute("field_instance_id_1", fieldInstanceId1);
    	String[] fieldInstanceIds = fieldInstanceIdString.split(",");
    	model.addAttribute("field_instance_id_2", fieldInstanceIds[page]);
    	model.addAttribute("page", page);
    	model.addAttribute("num_pages", fieldInstanceIds.length);
    	List<Integer> pages = new ArrayList<>();
    	for (int i = 0; i < fieldInstanceIds.length; i++) {
    		pages.add(i);
    	}
    	model.addAttribute("pages", pages);
    	model.addAttribute("field_instance_id_string", fieldInstanceIdString);
    	return "/browse/bivariate";
    }
}