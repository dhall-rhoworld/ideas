package com.rho.rhover.study;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

/**
 * Represents a research study.
 * @author dhall
 *
 */
@Entity
public class Study {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="study_id")
	private Long studyId;
	
	@Column(name="study_name")
	private String studyName;
	
	@OneToMany(mappedBy="study", cascade=CascadeType.ALL)
	private Set<StudyFolder> studyFolders;
	
	public Study() {
		
	}

	public Long getStudyId() {
		return studyId;
	}

	public void setStudyId(Long studyId) {
		this.studyId = studyId;
	}

	public String getStudyName() {
		return studyName;
	}

	public void setStudyName(String studyName) {
		this.studyName = studyName;
	}

	public Set<StudyFolder> getStudyFolders() {
		return studyFolders;
	}

	public void setStudyFolders(Set<StudyFolder> studyFolders) {
		this.studyFolders = studyFolders;
	}
	
}
