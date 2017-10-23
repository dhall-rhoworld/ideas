package com.rho.rhover.dataset;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class DataField {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="data_field_id")
	private Long dataFieldId;
	
	@Column(name="data_field_name")
	private String dataFieldName;
	
	@ManyToOne
	@JoinColumn(name="dataset_id")
	private Dataset dataset;
	
	public DataField() {
		
	}

	public Long getDataFieldId() {
		return dataFieldId;
	}

	public void setDataFieldId(Long dataFieldId) {
		this.dataFieldId = dataFieldId;
	}

	public String getDataFieldName() {
		return dataFieldName;
	}

	public void setDataFieldName(String dataFieldName) {
		this.dataFieldName = dataFieldName;
	}

	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}
	
}
