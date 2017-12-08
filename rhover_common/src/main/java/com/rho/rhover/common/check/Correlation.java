package com.rho.rhover.common.check;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.rho.rhover.common.study.FieldInstance;
import com.rho.rhover.common.study.Study;

@Entity
public class Correlation {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="correlation_id")
	private Long correlationId;
	
	@ManyToOne
	@JoinColumn(name="field_instance_id_1")
	private FieldInstance fieldInstance1;
	
	@ManyToOne
	@JoinColumn(name="field_instance_id_2")
	private FieldInstance fieldInstance2;
	
	@ManyToOne
	@JoinColumn(name="study_id")
	private Study study;
	
	@Column(name="coefficient")
	private Double coefficient;
	
	public Correlation() {
		
	}

	public Correlation(Study study, FieldInstance fieldInstance1, FieldInstance fieldInstance2, Double correlation) {
		super();
		this.study = study;
		this.fieldInstance1 = fieldInstance1;
		this.fieldInstance2 = fieldInstance2;
		this.coefficient = correlation;
	}

	public Long getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(Long correlationId) {
		this.correlationId = correlationId;
	}

	public FieldInstance getFieldInstance1() {
		return fieldInstance1;
	}

	public void setFieldInstance1(FieldInstance fieldInstance1) {
		this.fieldInstance1 = fieldInstance1;
	}

	public FieldInstance getFieldInstance2() {
		return fieldInstance2;
	}

	public void setFieldInstance2(FieldInstance fieldInstance2) {
		this.fieldInstance2 = fieldInstance2;
	}

	public Double getCoefficient() {
		return coefficient;
	}

	public void setCoefficient(Double coefficient) {
		this.coefficient = coefficient;
	}

	public Study getStudy() {
		return study;
	}

	public void setStudy(Study study) {
		this.study = study;
	}

}
