package com.rho.rhover.common.check;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

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
	@JoinColumn(name="check_id")
	private Check check;
	
	public CheckParam() {
		
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
}
