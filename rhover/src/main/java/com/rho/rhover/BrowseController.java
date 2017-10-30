package com.rho.rhover;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.rho.rhover.anomaly.AnomalySummaryBuilder;
import com.rho.rhover.anomaly.BivariateCheck;
import com.rho.rhover.anomaly.BivariateCheckRepository;
import com.rho.rhover.study.Dataset;
import com.rho.rhover.study.DatasetRepository;
import com.rho.rhover.study.Study;
import com.rho.rhover.study.StudyRepository;

@Controller
@RequestMapping("browse")
public class BrowseController {
	
	@Autowired
	private AnomalySummaryBuilder anomalySummaryBuilder;
	
	@Autowired
	private StudyRepository studyRepository;
	
	@Autowired
	private DatasetRepository datasetRepository;
	
	@Autowired
	private BivariateCheckRepository bivariateCheckRepository;

    @RequestMapping("/studies")
    public String studies(Model model) {
		model.addAttribute("summaries", anomalySummaryBuilder.getStudySummaries());
		return "browse/studies";
    }
    
    @RequestMapping("/datasets")
    public String datasets(
	    		@RequestParam("study_id") Long studyId,
	    		Model model) {
    	Study study = studyRepository.findOne(studyId);
		model.addAttribute("summaries", anomalySummaryBuilder.getDatasetSummaries(studyId));
		model.addAttribute("study", study);
		return "browse/datasets";
    }
    
    @RequestMapping("/data_fields")
    public String dataField(
    			@RequestParam("dataset_id") Long datasetId,
    			Model model) {
    	Dataset dataset = datasetRepository.findOne(datasetId);
    	Iterable<BivariateCheck> bivariateChecks = bivariateCheckRepository.findByDataset(dataset);
		model.addAttribute("summaries", anomalySummaryBuilder.getDataFieldSummaries(datasetId));
		model.addAttribute("dataset", dataset);
		model.addAttribute("bivariate_checks", bivariateChecks);
		return "browse/data_fields";
    }
}
