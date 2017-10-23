package com.rho.rhover.anomaly;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.rho.rhover.study.StudyDataRepository;

@Controller
@RequestMapping("/anomaly")
public class AnomalyController {
	
	@Autowired
	private AnomalySummaryBuilder anomalySummaryBuilder;
	
	@Autowired
	private AnomalyRepository anomalyRepository;
	
	@Autowired
	private StudyDataRepository studyDataRepository;
	
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
    
    @RequestMapping("/table")
    public String anomalyTable(
    			@RequestParam("data_field_id") Long dataFieldId,
    			@RequestParam("data_field_name") String dataFieldName,
    			Model model) {
    	model.addAttribute("anomalies", anomalyRepository.getCurrentAnomalies(dataFieldId));
    	model.addAttribute("data_field_name", dataFieldName);
    	return "anomaly/table";
    }
    
    @RequestMapping("/chart")
    public String anomalyChart(
		    @RequestParam("data_field_id") Long dataFieldId,
			@RequestParam("data_field_name") String dataFieldName,
			Model model) {
    	model.addAttribute("study_data", studyDataRepository.getAllDataFieldValues(dataFieldId));
    	model.addAttribute("data_field_name", dataFieldName);
    	return "anomaly/chart";
    }
}
