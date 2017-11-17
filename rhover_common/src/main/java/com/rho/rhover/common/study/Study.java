package com.rho.rhover.common.study;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
	
	// TODO: Consider whether we need this collection.  Other many-to-one relationships
	// only have the child to parent relationship.
	@OneToMany(mappedBy="study", cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	private Set<DataLocation> dataLocations = new HashSet<>();
	
	@Column(name="form_field_name")
	private String formFieldName;
	
	@Column(name="site_field_name")
	private String siteFieldName;
	
	@Column(name="subject_field_name")
	private String subjectFieldName;
	
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

	public Set<DataLocation> getDataLocations() {
		return dataLocations;
	}

	public void setDataLocations(Set<DataLocation> dataLocations) {
		this.dataLocations = dataLocations;
	}

	public String getFormFieldName() {
		return formFieldName;
	}

	public void setFormFieldName(String formFieldName) {
		this.formFieldName = formFieldName;
	}

	public String getSiteFieldName() {
		return siteFieldName;
	}

	public void setSiteFieldName(String siteFieldName) {
		this.siteFieldName = siteFieldName;
	}

	public String getSubjectFieldName() {
		return subjectFieldName;
	}

	public void setSubjectFieldName(String subjectFieldName) {
		this.subjectFieldName = subjectFieldName;
	}
	
}
