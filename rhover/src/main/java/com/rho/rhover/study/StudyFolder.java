package com.rho.rhover.study;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Concapsulates a Windows folder path that contains study database files.
 * @author dhall
 *
 */
@Entity
public class StudyFolder {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="study_folder_id")
	private Long studyFolderId;
	
	@Column(name="folder_path")
	private String folderPath;
	
	@ManyToOne
	@JoinColumn(name="study_id")
	private Study study;
	
	public StudyFolder() {
		
	}

	public Long getStudyFolderId() {
		return studyFolderId;
	}

	public void setStudyFolderId(Long studyFolderId) {
		this.studyFolderId = studyFolderId;
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
