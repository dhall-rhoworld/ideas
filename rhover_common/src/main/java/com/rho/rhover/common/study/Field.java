package com.rho.rhover.common.study;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

@Entity
public class Field {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="field_id")
	private Long fieldId;
	
	@Column(name="field_name")
	private String fieldName;
	
	@ManyToOne
	@JoinColumn(name="study_id")
	private Study study;
	
	@Column(name="data_type")
	private String dataType;
	
	@ManyToMany(mappedBy = "fields")
	private Set<DatasetVersion> datasetVersions = new HashSet<>();
	
	public Field() {
		
	}

	public Field(String fieldName, Study study, String dataType) {
		super();
		this.fieldName = fieldName;
		this.study = study;
		this.dataType = dataType;
	}

	public Long getFieldId() {
		return fieldId;
	}

	public void setFieldId(Long fieldId) {
		this.fieldId = fieldId;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public Study getStudy() {
		return study;
	}

	public void setStudy(Study study) {
		this.study = study;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public Set<DatasetVersion> getDatasetVersions() {
		return datasetVersions;
	}

	public void setDatasetVersions(Set<DatasetVersion> datasetVersions) {
		this.datasetVersions = datasetVersions;
	}
	
	public void addDatasetVersion(DatasetVersion datasetVersion) {
		datasetVersions.add(datasetVersion);
	}
}
