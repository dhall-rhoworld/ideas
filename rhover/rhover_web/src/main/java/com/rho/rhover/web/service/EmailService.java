package com.rho.rhover.web.service;

public interface EmailService {

	void sendMessage(String to, String subject, String text);
}
