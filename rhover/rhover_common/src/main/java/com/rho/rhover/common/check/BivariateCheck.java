package com.rho.rhover.common.check;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;

import com.rho.rhover.common.study.FieldInstance;
import com.rho.rhover.common.study.Study;

@Entity
public class BivariateCheck {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="bivariate_check_id")
	private Long bivariateCheckId;
	
	@ManyToOne
	@JoinColumn(name="x_field_instance_id")
	private FieldInstance xFieldInstance;
	
	@ManyToOne
	@JoinColumn(name="y_field_instance_id")
	private FieldInstance yFieldInstance;
	
	@ManyToOne
	@JoinColumn(name="check_id")
	private Check check;
	
	@ManyToOne
	@JoinColumn(name="study_id")
	private Study study;
	
	@OneToMany(mappedBy="bivariateCheck", fetch=FetchType.EAGER)
	@MapKey(name="paramName")
	private Map<String, CheckParam> checkParams = new HashMap<>();

	public BivariateCheck() {
		
	}

	public BivariateCheck(FieldInstance xFieldInstance, FieldInstance yFieldInstance, Check check, Study study) {
		super();
		this.xFieldInstance = xFieldInstance;
		this.yFieldInstance = yFieldInstance;
		this.check = check;
		this.study = study;
	}

	public Long getBivariateCheckId() {
		return bivariateCheckId;
	}

	public void setBivariateCheckId(Long bivariateCheckId) {
		this.bivariateCheckId = bivariateCheckId;
	}

	// TODO: Rename so that the X is capitalized
	public FieldInstance getxFieldInstance() {
		return xFieldInstance;
	}

	public void setxFieldInstance(FieldInstance xFieldInstance) {
		this.xFieldInstance = xFieldInstance;
	}

	public FieldInstance getyFieldInstance() {
		return yFieldInstance;
	}

	public void setyFieldInstance(FieldInstance yFieldInstance) {
		this.yFieldInstance = yFieldInstance;
	}

	public Check getCheck() {
		return check;
	}

	public void setCheck(Check check) {
		this.check = check;
	}

	public Study getStudy() {
		return study;
	}

	public void setStudy(Study study) {
		this.study = study;
	}
	public Map<String, CheckParam> getCheckParams() {
		return checkParams;
	}

	public void setCheckParams(Map<String, CheckParam> checkParams) {
		this.checkParams = checkParams;
	}
	
	public boolean fieldsInSameDataset() {
		return xFieldInstance.getDataset().equals(yFieldInstance.getDataset());
	}
}
