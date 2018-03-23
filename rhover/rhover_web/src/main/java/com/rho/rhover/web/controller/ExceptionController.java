package com.rho.rhover.web.controller;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.rho.rhover.web.service.EmailService;

@ControllerAdvice
public class ExceptionController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private EmailService emailService;

	@ExceptionHandler(Exception.class)
	public String handleException(Exception e, Model model) {
		StringWriter stringWriter = new StringWriter();
		String errorMessage = "From the captain: " + e.getMessage();
		e.printStackTrace(new PrintWriter(stringWriter));
		logger.error(stringWriter.toString());
		model.addAttribute("message", errorMessage);
		//emailService.sendMessage("david_hall@rhoworld.com", "RhoVer Error", "Crap!");
		return "error";
	}
	
}
