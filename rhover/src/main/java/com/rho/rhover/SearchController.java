package com.rho.rhover;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/search")
public class SearchController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@RequestMapping("/form")
	public String searchForm(Model model) {
		logger.debug("Loading search form");
		return "search/form";
	}
}
