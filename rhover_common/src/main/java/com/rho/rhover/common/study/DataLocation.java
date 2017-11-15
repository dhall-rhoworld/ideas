package com.rho.rhover.common.study;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class DataLocation {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="data_location_id")
	private Long dataLocationId;
	
	@Column(name="folder_path")
	private String folderPath;
	
	@ManyToOne
	@JoinColumn(name="study_id")
	private Study study;
	
	public DataLocation() {
		
	}

	public Long getDataLocationId() {
		return dataLocationId;
	}

	public void setDataLocationId(Long dataLocationId) {
		this.dataLocationId = dataLocationId;
	}

	public String getFolderPath() {
		return folderPath;
	}

	public void setFolderPath(String folderPath) {
		this.folderPath = folderPath;
	}

	public Study getStudy() {
		return study;
	}

	public void setStudy(Study study) {
		this.study = study;
	}
	
}
