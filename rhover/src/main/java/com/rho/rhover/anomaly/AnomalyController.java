package com.rho.rhover.anomaly;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.rho.rhover.study.DataField;
import com.rho.rhover.study.DataFieldRepository;
import com.rho.rhover.study.Site;
import com.rho.rhover.study.SiteRepository;
import com.rho.rhover.study.StudyDataRepository;

@Controller
@RequestMapping("/anomaly")
public class AnomalyController {
	
	@Autowired
	private AnomalyRepository anomalyRepository;
	
	@Autowired
	private DataFieldRepository dataFieldRepository;
	
	@Autowired
	private BivariateCheckRepository bivariateCheckRepository;
	
	@Autowired
	private StudyDataRepository studyDataRepository;
	
	@Autowired
	private SiteRepository siteRepository;
    
    @RequestMapping("/table")
    public String anomalyTable(
    			@RequestParam("data_field_id") Long dataFieldId,
    			@RequestParam(name="site_id", required=false, defaultValue="-1") Long siteId,
    			Model model) {
    	DataField dataField = dataFieldRepository.findOne(dataFieldId);
    	
    	model.addAttribute("data_field", dataField);
    	if (siteId == -1) {
    		model.addAttribute("anomalies", anomalyRepository.getCurrentAnomalies(dataFieldId));
    	}
    	else {
    		Site site = siteRepository.findOne(siteId);
    		model.addAttribute("site", site);
    		model.addAttribute("anomalies", anomalyRepository.getCurrentAnomalies(dataFieldId, siteId));
    	}
    	studyDataRepository.markAnomaliesAsViewed(dataFieldId);
    	return "anomaly/table";
    }
    
    @RequestMapping("/beeswarm")
    public String beeswarm(
		    @RequestParam("data_field_id") Long dataFieldId,
		    @RequestParam(name="site_id", required=false, defaultValue="-1") Long siteId,
			Model model) {
    	DataField dataField = dataFieldRepository.findOne(dataFieldId);
    	model.addAttribute("data_field", dataField);
    	if (siteId == -1) {
    		model.addAttribute("site_name", "-1");
    	}
    	else {
    		Site site = siteRepository.findOne(siteId);
    		model.addAttribute("site_name", site.getSiteName());
    		model.addAttribute("site", site);
    	}
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
    
    @RequestMapping("/scatterplot")
    public String scatterPlot(
    		@RequestParam("bivariate_check_id") Long bivariateCheckId,
    		Model model) {
    	model.addAttribute("bivariate_check", bivariateCheckRepository.findOne(bivariateCheckId));
    	return "anomaly/scatterplot";
    }
}
