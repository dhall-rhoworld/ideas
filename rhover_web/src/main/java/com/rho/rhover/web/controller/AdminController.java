package com.rho.rhover.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.rho.rhover.web.admin.LoggingEventRepository;

@Controller
@RequestMapping("/admin")
public class AdminController {
	
	@Autowired
	private LoggingEventRepository loggingEventRepository;
	
	@RequestMapping("/home")
	public String adminHome() {
		return "/admin/home";
	}

	@RequestMapping("/log")
	public String showLog(Model model) {
		model.addAttribute("events", loggingEventRepository.findAll());
		return "/admin/log";
	}

}
