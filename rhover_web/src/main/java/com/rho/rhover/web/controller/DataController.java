package com.rho.rhover.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.rho.rhover.web.dto.AnomalySummaryBuilder;

@Controller
@RequestMapping("/")
public class DataController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@RequestMapping("/")
	public String home(Model model) {
		logger.debug("Debug message");
		logger.info("Info message");
		logger.warn("Warn message");
		logger.error("Error message");
		return "home";
	}
	
	@RequestMapping("/test_ui")
	public String testUi() {
		return "test_ui";
	}
}
