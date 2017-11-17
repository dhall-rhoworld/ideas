package com.rho.rhover.common.study;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Type;

@Entity
public class StudyDbVersion {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="study_db_version_id")
	private Long studyDbVersionId;
	
	@Column(name="study_db_version_name")
	private String studyDbVersionName;
	
	@ManyToOne
	@JoinColumn(name="study_id")
	private Study study;
	
	@Column(name="is_current")
	@Type(type="org.hibernate.type.NumericBooleanType")
	private Boolean isCurrent;
	
	@ManyToMany
	@JoinTable(name="study_db_version_config", joinColumns=@JoinColumn(name="study_db_version_id"),
		inverseJoinColumns=@JoinColumn(name="dataset_version_id"))
	private Set<DatasetVersion> datasetVersions = new HashSet<>();
	
	public StudyDbVersion() {
		
	}

	public Long getStudyDbVersionId() {
		return studyDbVersionId;
	}

	public void setStudyDbVersionId(Long studyDbVersionId) {
		this.studyDbVersionId = studyDbVersionId;
	}

	public String getStudyDbVersionName() {
		return studyDbVersionName;
	}

	public void setStudyDbVersionName(String studyDbVersionName) {
		this.studyDbVersionName = studyDbVersionName;
	}

	public Study getStudy() {
		return study;
	}

	public void setStudy(Study study) {
		this.study = study;
	}

	public Boolean getIsCurrent() {
		return isCurrent;
	}

	public void setIsCurrent(Boolean isCurrent) {
		this.isCurrent = isCurrent;
	}

	public Set<DatasetVersion> getDatasetVersions() {
		return datasetVersions;
	}

	public void setDatasetVersions(Set<DatasetVersion> datasetVersions) {
		this.datasetVersions = datasetVersions;
	}
}
