package com.rho.rhover.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.rho.rhover.web.dto.AnomalySummaryBuilder;

@Controller
@RequestMapping("/")
public class DataController {
	
	@Autowired
	private AnomalySummaryBuilder anomalySummaryBuilder;

	@RequestMapping("/")
	public String home(Model model) {
		model.addAttribute("summaries", anomalySummaryBuilder.getStudySummaries());
		return "home";
	}
	
	@RequestMapping("/test_ui")
	public String testUi() {
		return "test_ui";
	}
}
