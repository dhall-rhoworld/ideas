package com.rho.rhover.anomaly;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.rho.rhover.study.DataField;
import com.rho.rhover.study.DataFieldRepository;

@Controller
@RequestMapping("/anomaly")
public class AnomalyController {
	
	@Autowired
	private AnomalyRepository anomalyRepository;
	
	@Autowired
	private DataFieldRepository dataFieldRepository;
    
    @RequestMapping("/table")
    public String anomalyTable(
    			@RequestParam("data_field_id") Long dataFieldId,
    			Model model) {
    	DataField dataField = dataFieldRepository.findOne(dataFieldId);
    	model.addAttribute("anomalies", anomalyRepository.getCurrentAnomalies(dataFieldId));
    	model.addAttribute("data_field", dataField);
    	return "anomaly/table";
    }
    
    @RequestMapping("/beeswarm")
    public String beeswarm(
		    @RequestParam("data_field_id") Long dataFieldId,
			Model model) {
    	DataField dataField = dataFieldRepository.findOne(dataFieldId);
    	model.addAttribute("data_field", dataField);
    	return "anomaly/beeswarm";
    }
    
    @RequestMapping("/boxplot")
    public String boxplot(
		    @RequestParam("data_field_id") Long dataFieldId,
			Model model) {
    	DataField dataField = dataFieldRepository.findOne(dataFieldId);
    	model.addAttribute("data_field", dataField);
    	return "anomaly/boxplot";
    }
}
