package com.rho.rhover.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.rho.rhover.web.service.ReportingService;

@Controller
@RequestMapping("/reporting")
public class ReportingController {
	
	@Autowired
	private ReportingService reportingService;

	@RequestMapping("/loads")
	public String showLoads(Model model) {
		model.addAttribute("overviews", reportingService.getDataLoadOverviews());
		return "reporting/loads";
	}

}
