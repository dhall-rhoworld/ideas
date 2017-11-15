package com.rho.rhover.common.study;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.rho.rhover.common.study.Study;

@Entity
public class Dataset {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="dataset_id")
	private Long datasetId;
	
	@Column(name="dataset_name")
	private String datasetName;
	
	@ManyToOne
	@JoinColumn(name="study_id")
	private Study study;
	
	public Dataset() {
		
	}

	public Long getDatasetId() {
		return datasetId;
	}

	public void setDatasetId(Long datasetId) {
		this.datasetId = datasetId;
	}

	public String getDatasetName() {
		return datasetName;
	}

	public void setDatasetName(String datasetName) {
		this.datasetName = datasetName;
	}

	public Study getStudy() {
		return study;
	}

	public void setStudy(Study study) {
		this.study = study;
	}
}
