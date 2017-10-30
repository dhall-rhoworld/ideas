package com.rho.rhover.anomaly;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.rho.rhover.study.Dataset;

@Entity
public class BivariateCheck {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="bivariate_check_id")
	private Long bivariateCheckId;
	
	@Column(name="data_field_1")
	private String dataField1;
	
	@Column(name="data_field_2")
	private String dataField2;
	
	@ManyToOne
	@JoinColumn(name="dataset_id")
	private Dataset dataset;
	
	public BivariateCheck() {
		
	}

	public Long getBivariateCheckId() {
		return bivariateCheckId;
	}

	public void setBivariateCheckId(Long bivariateCheckId) {
		this.bivariateCheckId = bivariateCheckId;
	}

	public String getDataField1() {
		return dataField1;
	}

	public void setDataField1(String dataField1) {
		this.dataField1 = dataField1;
	}

	public String getDataField2() {
		return dataField2;
	}

	public void setDataField2(String dataField2) {
		this.dataField2 = dataField2;
	}

	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}
	
	public String getDisplayName() {
		return this.dataField1 + " X " + this.dataField2;
	}
}
