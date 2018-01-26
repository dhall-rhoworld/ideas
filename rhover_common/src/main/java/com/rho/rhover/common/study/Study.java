package com.rho.rhover.common.study;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Type;

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
	
	@Column(name="phase_field_name")
	private String phaseFieldName;
	
	@Column(name="record_id_field_name")
	private String recordIdFieldName;
	
	@Column(name="query_file_path")
	private String queryFilePath;
	
	@Column(name="is_initialized")
	@Type(type="org.hibernate.type.NumericBooleanType")
	private Boolean isInitialized = Boolean.FALSE;
	
	@ManyToOne
	@JoinColumn(name="form_field_id")
	private Field formField;
	
	@ManyToOne
	@JoinColumn(name="site_field_id")
	private Field siteField;
	
	@ManyToOne
	@JoinColumn(name="subject_field_id")
	private Field subjectField;
	
	@ManyToOne
	@JoinColumn(name="phase_field_id")
	private Field phaseField;
	
	@ManyToOne
	@JoinColumn(name="record_id_field_id")
	private Field recordIdField;
	
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

	public String getQueryFilePath() {
		return queryFilePath;
	}

	public void setQueryFilePath(String queryFilePath) {
		this.queryFilePath = queryFilePath;
	}

	public Boolean getIsInitialized() {
		return isInitialized;
	}

	public void setIsInitialized(Boolean isInitialized) {
		this.isInitialized = isInitialized;
	}

	public String getRecordIdFieldName() {
		return recordIdFieldName;
	}

	public void setRecordIdFieldName(String recordIdFieldName) {
		this.recordIdFieldName = recordIdFieldName;
	}

	public Field getFormField() {
		return formField;
	}

	public void setFormField(Field formField) {
		this.formField = formField;
	}

	public Field getSiteField() {
		return siteField;
	}

	public void setSiteField(Field siteField) {
		this.siteField = siteField;
	}

	public Field getSubjectField() {
		return subjectField;
	}

	public void setSubjectField(Field subjectField) {
		this.subjectField = subjectField;
	}

	public Field getRecordIdField() {
		return recordIdField;
	}

	public void setRecordIdField(Field recordIdField) {
		this.recordIdField = recordIdField;
	}

	public String getPhaseFieldName() {
		return phaseFieldName;
	}

	public void setPhaseFieldName(String phaseFieldName) {
		this.phaseFieldName = phaseFieldName;
	}

	public Field getPhaseField() {
		return phaseField;
	}

	public void setPhaseField(Field phaseField) {
		this.phaseField = phaseField;
	}
	
	public boolean isFieldIdentifying(Field field) {
		return
				field.equals(this.formField)
				|| field.equals(this.phaseField)
				|| field.equals(this.recordIdField)
				|| field.equals(this.siteField)
				|| field.equals(this.subjectField);
	}
	
	public boolean isFieldIdentifying(String fieldName) {
		return
				fieldName.equals(this.formFieldName)
				|| fieldName.equals(this.phaseFieldName)
				|| fieldName.equals(this.recordIdFieldName)
				|| fieldName.equals(this.siteFieldName)
				|| fieldName.equals(this.subjectFieldName);
	}
	
	// TODO: Figure out how to get rid of this
	public Set<Field> getUniqueIdentifierFields() {
		Set<Field> fields = new HashSet<>();
		fields.addAll(Arrays.asList(subjectField, phaseField, recordIdField, siteField));
		return fields;
	}
}
