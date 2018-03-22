package com.rho.rhover.common.session;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class UserSession {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="user_session_id")
	private Long userSessionId;
	
	@Column(name="user_name")
	private String userName;
	
	@Column(name="session_started")
	private Timestamp sessionStarted;
	
	@Column(name="web_session_id")
	private String webSessionId;

	public UserSession() {
		
	}

	public UserSession(String userName, Timestamp sessionStarted, String webSessionId) {
		super();
		this.userName = userName;
		this.sessionStarted = sessionStarted;
		this.webSessionId = webSessionId;
	}

	public Long getUserSessionId() {
		return userSessionId;
	}

	public void setUserSessionId(Long userSessionId) {
		this.userSessionId = userSessionId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Timestamp getSessionStarted() {
		return sessionStarted;
	}

	public void setSessionStarted(Timestamp sessionStarted) {
		this.sessionStarted = sessionStarted;
	}

}
