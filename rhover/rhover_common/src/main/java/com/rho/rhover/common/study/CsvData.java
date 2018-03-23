package com.rho.rhover.common.study;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

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
	
	@ManyToOne
	@JoinColumn(name="dataset_id")
	private Dataset dataset;
	
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

	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}
	
	public List<Double> extractDataAsDouble() {
		List<Double> list = new ArrayList<>();
		StringTokenizer tok = new StringTokenizer(data, ",");
		while (tok.hasMoreTokens()) {
			String value = tok.nextToken();
			if (value.trim().length() == 0 || value.equals("null")) {
				list.add(Double.NaN);
			}
			else {
				list.add(new Double(value));
			}
		}
		return list;
	}
	
	public List<String> extractData() {
		List<String> list = new ArrayList<>();
		StringTokenizer tok = new StringTokenizer(data, ",");
		while (tok.hasMoreTokens()) {
			list.add(tok.nextToken());
		}
		return list;
	}
}
