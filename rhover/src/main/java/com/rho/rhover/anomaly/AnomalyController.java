package com.rho.rhover.anomaly;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/anomaly")
public class AnomalyController {
	
	@Autowired
	private AnomalySummaryBuilder anomalySummaryBuilder;
	
    @RequestMapping("/study")
    public String study(Model model) {
    		model.addAttribute("summaries", anomalySummaryBuilder.getStudySummaries());
    		return "anomaly/study";
    }

    @RequestMapping("/dataset")
    public String dataset(
	    		@RequestParam("study_id") Long studyId,
	    		@RequestParam("study_name") String studyName,
	    		Model model) {
    		model.addAttribute("summaries", anomalySummaryBuilder.getDatasetSummaries(studyId));
    		model.addAttribute("study_name", studyName);
    		return "anomaly/dataset";
    }
    
    @RequestMapping("/data_field")
    public String dataField(
    			@RequestParam("dataset_id") Long datasetId,
    			@RequestParam("dataset_name") String datasetName,
    			Model model) {
    		model.addAttribute("summaries", anomalySummaryBuilder.getDataFieldSummaries(datasetId));
    		model.addAttribute("dataset_name", datasetName);
    		return "anomaly/data_field";
    }
}
