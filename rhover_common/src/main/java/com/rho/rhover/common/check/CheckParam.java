package com.rho.rhover.common.check;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Type;

import com.rho.rhover.common.session.UserSession;
import com.rho.rhover.common.study.Dataset;
import com.rho.rhover.common.study.Field;
import com.rho.rhover.common.study.Study;

@Entity
public class CheckParam {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="check_param_id")
	private Long checkParamId;
	
	@Column(name="param_name")
	private String paramName;
	
	@Column(name="param_value")
	private String paramValue;
	
	// TODO: Consider making this an enum
	@Column(name="param_scope")
	private String paramScope;
	
	@ManyToOne
	@JoinColumn(name="study_id")
	private Study study;
	
	@ManyToOne
	@JoinColumn(name="dataset_id")
	private Dataset dataset;
	
	@ManyToOne
	@JoinColumn(name="field_id")
	private Field field;
	
	@ManyToOne
	@JoinColumn(name="bivariate_check_id")
	private BivariateCheck bivariateCheck;
	
	@ManyToOne
	@JoinColumn(name="check_id")
	private Check check;
	
	@ManyToOne
	@JoinColumn(name="user_session_id")
	private UserSession userSession;
	
	@Column(name="is_current")
	@Type(type="org.hibernate.type.NumericBooleanType")
	private Boolean isCurrent = Boolean.TRUE;
	
	public CheckParam() {
		
	}

	public CheckParam(String paramName, String paramScope, Check check, UserSession userSession) {
		super();
		this.paramName = paramName;
		this.paramScope = paramScope;
		this.check = check;
		this.userSession = userSession;
	}

	public CheckParam(String paramName, String paramValue, String paramScope, Check check, UserSession userSession) {
		super();
		this.paramName = paramName;
		this.paramValue = paramValue;
		this.paramScope = paramScope;
		this.check = check;
		this.userSession = userSession;
	}
	
	public Long getCheckParamId() {
		return checkParamId;
	}

	public void setCheckParamId(Long checkParamId) {
		this.checkParamId = checkParamId;
	}

	public String getParamName() {
		return paramName;
	}

	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	public String getParamValue() {
		return paramValue;
	}

	public void setParamValue(String paramValue) {
		this.paramValue = paramValue;
	}

	public String getParamScope() {
		return paramScope;
	}

	public void setParamScope(String paramScope) {
		this.paramScope = paramScope;
	}

	public Study getStudy() {
		return study;
	}

	public void setStudy(Study study) {
		this.study = study;
	}

	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	public Field getField() {
		return field;
	}

	public void setField(Field field) {
		this.field = field;
	}

	public Check getCheck() {
		return check;
	}

	public void setCheck(Check check) {
		this.check = check;
	}

	public BivariateCheck getBivariateCheck() {
		return bivariateCheck;
	}

	public void setBivariateCheck(BivariateCheck bivariateCheck) {
		this.bivariateCheck = bivariateCheck;
	}

	public UserSession getUserSession() {
		return userSession;
	}

	public void setUserSession(UserSession userSession) {
		this.userSession = userSession;
	}

	public Boolean getIsCurrent() {
		return isCurrent;
	}

	public void setIsCurrent(Boolean isCurrent) {
		this.isCurrent = isCurrent;
	}
	
}
