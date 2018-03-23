package com.rho.rhover.common.session;

public interface UserSessionService {

	UserSession getUserSession(String userName, String webSessionId);
}
