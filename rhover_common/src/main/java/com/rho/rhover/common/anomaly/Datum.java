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
public class Datum {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="datum_id")
	private Long datumId;
	
	@ManyToOne
	@JoinColumn(name="field_id")
	private Field field;
	
	@ManyToOne
	@JoinColumn(name="observation_id")
	private Observation observation;

	public Datum() {
		
	}

	public Datum(Field field, Observation observation) {
		super();
		this.field = field;
		this.observation = observation;
	}

	public Long getDatumId() {
		return datumId;
	}

	public void setDatumId(Long datumId) {
		this.datumId = datumId;
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
