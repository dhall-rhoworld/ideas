package com.rho.rhover.common.check;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.rho.rhover.common.study.FieldInstance;

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

	public BivariateCheck() {
		
	}

	public BivariateCheck(FieldInstance xFieldInstance, FieldInstance yFieldInstance, Check check) {
		super();
		this.xFieldInstance = xFieldInstance;
		this.yFieldInstance = yFieldInstance;
		this.check = check;
	}

	public Long getBivariateCheckId() {
		return bivariateCheckId;
	}

	public void setBivariateCheckId(Long bivariateCheckId) {
		this.bivariateCheckId = bivariateCheckId;
	}

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
}
