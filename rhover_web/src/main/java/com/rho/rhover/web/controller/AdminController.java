package com.rho.rhover.web.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.rho.rhover.web.admin.LoggingEventRepository;

@Controller
@RequestMapping("/admin")
public class AdminController {
	
	private static final int PAGE_SIZE = 50;
	
	private final Sort sort = new Sort(Direction.DESC, "timestamp");
	
	@Autowired
	private LoggingEventRepository loggingEventRepository;
	
	@RequestMapping("/home")
	public String adminHome() {
		return "/admin/home";
	}

	@RequestMapping("/log")
	public String showLog(Model model,
			@RequestParam(name="pageNum", required=false, defaultValue="1") Integer pageNum) {
		PageRequest pageRequest = new PageRequest(pageNum, PAGE_SIZE, sort);
		model.addAttribute("events", loggingEventRepository.findAll(pageRequest));
		int numPages = (int)Math.ceil(loggingEventRepository.count() / PAGE_SIZE);
		List<Integer> pages = new ArrayList<Integer>();
		for (int i = 1; i <= numPages; i++) {
			pages.add(i);
		}
		model.addAttribute("numPages", numPages);
		model.addAttribute("pages", pages);
		model.addAttribute("pageNum", pageNum);
		if (pageNum > 1) {
			model.addAttribute("previousPage", pageNum - 1);
		}
		if (pageNum < numPages) {
			model.addAttribute("nextPage", pageNum + 1);
		}
		return "/admin/log";
	}

}
