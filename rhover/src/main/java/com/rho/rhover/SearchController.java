package com.rho.rhover;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.rho.rhover.study.StudyRepository;

@Controller
@RequestMapping("/search")
public class SearchController {
	
	@Autowired
	private StudyRepository studyRepository;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@RequestMapping("/form")
	public String searchForm(Model model) {
		model.addAttribute("studies", studyRepository.findAll());
		return "search/form";
	}
}
