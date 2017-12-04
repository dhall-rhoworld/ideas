package com.rho.rhover.common.anomaly;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.rho.rhover.common.study.Field;

@Entity
public class IdFieldValue {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id_field_value_id")
	private Long idFieldValueId;
	
	@Column(name="value")
	private String value;
	
	@ManyToOne
	@JoinColumn(name="field_id")
	private Field field;
	
	@ManyToOne
	@JoinColumn(name="observation_id")
	private Observation observation;
	
	public IdFieldValue() {
		
	}
	
	public IdFieldValue(String value, Field field) {
		super();
		this.value = value;
		this.field = field;
	}
	
	public IdFieldValue(String value, Field field, Observation observation) {
		super();
		this.value = value;
		this.field = field;
		this.observation = observation;
	}

	public Long getIdFieldValueId() {
		return idFieldValueId;
	}

	public void setIdFieldValueId(Long idFieldValueId) {
		this.idFieldValueId = idFieldValueId;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Field getField() {
		return field;
	}

	public void setField(Field field) {
		this.field = field;
	}

	public Observation getObservation() {
		return observation;
	}

	public void setObservation(Observation observation) {
		this.observation = observation;
	}
}
