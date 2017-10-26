package com.rho.rhover.study;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class DataField {
	
	private static final NumberFormat FORMAT = new DecimalFormat("###,###.##");

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="data_field_id")
	private Long dataFieldId;
	
	@Column(name="data_field_name")
	private String dataFieldName;
	
	@ManyToOne
	@JoinColumn(name="dataset_id")
	private Dataset dataset;
	
	@Column(name="lower_threshold")
	private Double lowerThreshold;
	
	@Column(name="upper_threshold")
	private Double upperThreshold;
	
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

	public Double getLowerThreshold() {
		return lowerThreshold;
	}

	public void setLowerThreshold(Double lowerThreshold) {
		this.lowerThreshold = lowerThreshold;
	}

	public Double getUpperThreshold() {
		return upperThreshold;
	}

	public void setUpperThreshold(Double upperThreshold) {
		this.upperThreshold = upperThreshold;
	}
	
	public String getLowerThresholdFormatted() {
		return FORMAT.format(this.lowerThreshold);
	}
	
	public String getUpperThresholdFormatted() {
		return FORMAT.format(this.upperThreshold);
	}
}
