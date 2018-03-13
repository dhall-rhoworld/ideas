package com.rho.rhover.common.study;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class DatasetModification {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="dataset_modification_id")
	private Long datasetModificationId;
	
	@ManyToOne
	@JoinColumn(name="study_db_version_id")
	private StudyDbVersion studyDbVersion;
	
	@ManyToOne
	@JoinColumn(name="dataset_id")
	private Dataset dataset;
	
	@Column(name="is_new")
	private Boolean isNew = Boolean.FALSE;
	
	@Column(name="is_modified")
	private Boolean isModified = Boolean.FALSE;

	public DatasetModification() {
		
	}

	public Long getDatasetModificationId() {
		return datasetModificationId;
	}

	public void setDatasetModificationId(Long datasetModificationId) {
		this.datasetModificationId = datasetModificationId;
	}

	public StudyDbVersion getStudyDbVersion() {
		return studyDbVersion;
	}

	public void setStudyDbVersion(StudyDbVersion studyDbVersion) {
		this.studyDbVersion = studyDbVersion;
	}

	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	public Boolean getIsNew() {
		return isNew;
	}

	public void setIsNew(Boolean isNew) {
		this.isNew = isNew;
	}

	public Boolean getIsModified() {
		return isModified;
	}

	public void setIsModified(Boolean isModified) {
		this.isModified = isModified;
	}

}
