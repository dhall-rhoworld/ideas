package com.rho.rhover.common.study;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class CsvData {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="csv_data_id")
	private Long csvDataId;
	
	@Column(name="data")
	private String data;
	
	@ManyToOne
	@JoinColumn(name="field_id")
	private Field field;
	
	public CsvData() {
		
	}

	public Long getCsvDataId() {
		return csvDataId;
	}

	public void setCsvDataId(Long fieldId) {
		this.csvDataId = fieldId;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public Field getField() {
		return field;
	}

	public void setField(Field field) {
		this.field = field;
	}
	
}
