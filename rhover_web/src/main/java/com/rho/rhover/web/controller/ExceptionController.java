package com.rho.rhover.web.controller;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@ExceptionHandler
	public String handleException(Exception e, Model model) {
		StringWriter stringWriter = new StringWriter();
		String errorMessage = e.getMessage();
		e.printStackTrace(new PrintWriter(stringWriter));
		logger.error(stringWriter.toString());
		model.addAttribute("message", errorMessage);
		return "error";
	}
	
}
