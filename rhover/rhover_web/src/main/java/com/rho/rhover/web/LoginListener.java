package com.rho.rhover.web;

import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;

import com.rho.rhover.common.session.UserSession;
import com.rho.rhover.common.session.UserSessionRepository;

@Component
public class LoginListener implements ApplicationListener<AuthenticationSuccessEvent> {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private UserSessionRepository userSessionRepository;

	@Override
	public void onApplicationEvent(AuthenticationSuccessEvent event) {
		String userName = event.getAuthentication().getName();
		String sessionId = RequestContextHolder.getRequestAttributes().getSessionId();
		Timestamp sessionStarted = new Timestamp(event.getTimestamp());
		UserSession userSession = new UserSession(userName, sessionStarted, sessionId);
		userSessionRepository.save(userSession);
		logger.info("New user session: " + userSession.getUserName());
	}
}
