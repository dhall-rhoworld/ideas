package com.rho.rhover.common.anomaly;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.rho.rhover.common.check.CheckRun;

@Entity
public class DataProperty {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="data_property_id")
	private Long dataPropertyId;
	
	@Column(name="data_property_name")
	private String dataPropertyName;
	
	@Column(name="data_property_value")
	private String dataPropertyValue;
	
	@ManyToOne
	@JoinColumn(name="check_run_id")
	private CheckRun checkRun;

	public DataProperty() {
		
	}

	public DataProperty(String dataPropertyName, String dataPropertyValue, CheckRun checkRun) {
		super();
		this.dataPropertyName = dataPropertyName;
		this.dataPropertyValue = dataPropertyValue;
		this.checkRun = checkRun;
	}

	public Long getDataPropertyId() {
		return dataPropertyId;
	}

	public void setDataPropertyId(Long dataPropertyId) {
		this.dataPropertyId = dataPropertyId;
	}

	public String getDataPropertyName() {
		return dataPropertyName;
	}

	public void setDataPropertyName(String dataPropertyName) {
		this.dataPropertyName = dataPropertyName;
	}

	public String getDataPropertyValue() {
		return dataPropertyValue;
	}

	public void setDataPropertyValue(String dataPropertyValue) {
		this.dataPropertyValue = dataPropertyValue;
	}

	public CheckRun getCheckRun() {
		return checkRun;
	}

	public void setCheckRun(CheckRun checkRun) {
		this.checkRun = checkRun;
	}

}
