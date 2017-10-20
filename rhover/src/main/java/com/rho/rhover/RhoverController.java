package com.rho.rhover;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.rho.rhover.study.StudyRepository;

@Controller
public class RhoverController {
	
	@Autowired
	private StudyRepository studyRepository;

    @RequestMapping("/greeting")
    public String greeting(@RequestParam(value="name", required=false, defaultValue="World") String name, Model model) {
        model.addAttribute("name", name);
        return "greeting";
    }

    @RequestMapping("/home")
    public String home(Model model) {
    		model.addAttribute("studies", studyRepository.findAll());
    		return "home";
    }
}
