package com.rho.rhover.common.check;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.rho.rhover.common.session.UserSession;

@Entity
public class CheckParamChange {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="check_param_change_id")
	private Long checkParamChangeId;
	
	@ManyToOne
	@JoinColumn(name="old_check_param_id")
	private CheckParam oldCheckParam;
	
	@ManyToOne
	@JoinColumn(name="new_check_param_id")
	private CheckParam newCheckParam;
	
	@ManyToOne
	@JoinColumn(name="user_session_id")
	private UserSession userSession;
	
	public CheckParamChange() {
		
	}

	public CheckParamChange(CheckParam oldCheckParam, CheckParam newCheckParam, UserSession userSession) {
		super();
		this.oldCheckParam = oldCheckParam;
		this.newCheckParam = newCheckParam;
		this.userSession = userSession;
	}

	public Long getCheckParamChangeId() {
		return checkParamChangeId;
	}

	public void setCheckParamChangeId(Long checkParamChangeId) {
		this.checkParamChangeId = checkParamChangeId;
	}

	public CheckParam getOldCheckParam() {
		return oldCheckParam;
	}

	public void setOldCheckParam(CheckParam oldCheckParam) {
		this.oldCheckParam = oldCheckParam;
	}

	public CheckParam getNewCheckParam() {
		return newCheckParam;
	}

	public void setNewCheckParam(CheckParam newCheckParam) {
		this.newCheckParam = newCheckParam;
	}

	public UserSession getUserSession() {
		return userSession;
	}

	public void setUserSession(UserSession userSession) {
		this.userSession = userSession;
	}

}
