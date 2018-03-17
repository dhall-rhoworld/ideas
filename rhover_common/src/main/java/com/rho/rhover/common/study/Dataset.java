package com.rho.rhover.common.study;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Type;

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
	
	@Column(name="file_path")
	private String filePath;
	
	@Column(name="is_checked")
	@Type(type="org.hibernate.type.NumericBooleanType")
	private Boolean isChecked = Boolean.FALSE;
	
	@Column(name="is_critical")
	@Type(type="org.hibernate.type.NumericBooleanType")
	private Boolean isCritical = Boolean.FALSE;
	
	public Dataset() {
		
	}

	public Dataset(String datasetName, Study study, String filePath) {
		super();
		this.datasetName = datasetName;
		this.study = study;
		this.setFilePath(filePath);
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

	public String getFilePath() {
		return filePath.replaceAll("\\\\", "/");
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath.replaceAll("\\\\", "/");
	}

	public Boolean getIsChecked() {
		return isChecked;
	}

	public void setIsChecked(Boolean isChecked) {
		this.isChecked = isChecked;
	}

	public Boolean getIsCritical() {
		return isCritical;
	}

	public void setIsCritical(Boolean isCritical) {
		this.isCritical = isCritical;
	}
	
}
