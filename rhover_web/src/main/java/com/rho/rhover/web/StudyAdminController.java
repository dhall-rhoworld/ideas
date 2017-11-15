package com.rho.rhover.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.rho.rhover.common.study.StudyRepository;

@Controller
@RequestMapping("/admin/study")
public class StudyAdminController {
	
	@Autowired
	private StudyRepository studyRepository;

	@RequestMapping("/all")
	public String viewAll(Model model) {
		model.addAttribute("studies", studyRepository.findAll());
		return "admin/study/all_studies";
	}
}
