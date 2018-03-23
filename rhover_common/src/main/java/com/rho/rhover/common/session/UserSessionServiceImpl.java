package com.rho.rhover.common.session;

import java.sql.Timestamp;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserSessionServiceImpl implements UserSessionService {
	
	@Autowired
	private UserSessionRepository userSessionRepository;

	@Override
	public UserSession getUserSession(String userName, String webSessionId) {
		UserSession session = userSessionRepository.findByWebSessionId(webSessionId);
		if (session == null) {
			Timestamp sessionStarted = new Timestamp(new Date().getTime());
			session = new UserSession(userName, sessionStarted, webSessionId);
			userSessionRepository.save(session);
		}
		return session;
	}
}
