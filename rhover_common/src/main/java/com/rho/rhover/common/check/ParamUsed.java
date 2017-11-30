package com.rho.rhover.common.check;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class ParamUsed {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="param_used_id")
	private Long paramUsedId;
	
	@Column(name="param_name")
	private String paramName;
	
	@Column(name="param_value")
	private String paramValue;
	
	@ManyToOne
	@JoinColumn(name="check_run_id")
	private CheckRun checkRun;

	public ParamUsed() {
		
	}

	public ParamUsed(String paramName, String paramValue, CheckRun checkRun) {
		super();
		this.paramName = paramName;
		this.paramValue = paramValue;
		this.checkRun = checkRun;
	}

	public Long getParamUsedId() {
		return paramUsedId;
	}

	public void setParamUsedId(Long paramUsedId) {
		this.paramUsedId = paramUsedId;
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

	public CheckRun getCheckRun() {
		return checkRun;
	}

	public void setCheckRun(CheckRun checkRun) {
		this.checkRun = checkRun;
	}
}
