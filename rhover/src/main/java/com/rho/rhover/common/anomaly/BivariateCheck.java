package com.rho.rhover.common.anomaly;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.rho.rhover.common.study.Dataset;

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
	@JoinColumn(name="dataset_id_1")
	private Dataset dataset1;
	
	@ManyToOne
	@JoinColumn(name="dataset_id_2")
	private Dataset dataset2;
	
	@Column(name="file_path")
	private String filePath;
	
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

	public Dataset getDataset1() {
		return dataset1;
	}

	public void setDataset1(Dataset dataset1) {
		this.dataset1 = dataset1;
	}
	
	public Dataset getDataset2() {
		return dataset2;
	}

	public void setDataset2(Dataset dataset2) {
		this.dataset2 = dataset2;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getDisplayName() {
		return this.dataField1 + " X " + this.dataField2;
	}
}
