package com.rho.rhover.common.study;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Type;

import com.rho.rhover.common.check.CheckParam;

@Entity
public class Field {
	
	private static final Map<String, String> DISPLAY_VALUES = new HashMap<>();
	
	static {
		DISPLAY_VALUES.put("MixedType", "MIXED");
		DISPLAY_VALUES.put("String", "CHARACTER");
		DISPLAY_VALUES.put("Double", "CONTINUOUS");
		DISPLAY_VALUES.put("Date", "DATE");
		DISPLAY_VALUES.put("Boolean", "YES/NO");
		DISPLAY_VALUES.put("Integer", "INTEGER");
		DISPLAY_VALUES.put("UnknownType", "UNKNOWN");
	}
	

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="field_id")
	private Long fieldId;
	
	@Column(name="field_name")
	private String fieldName;
	
	@Column(name="field_label")
	private String fieldLabel;
	
	@ManyToOne
	@JoinColumn(name="study_id")
	private Study study;
	
	@Column(name="data_type")
	private String dataType;
	
	@Column(name="is_identifying")
	@Type(type="org.hibernate.type.NumericBooleanType")
	private Boolean isIdentifying = Boolean.FALSE;
	
	@ManyToMany(mappedBy = "fields")
	private Set<DatasetVersion> datasetVersions = new HashSet<>();
	
	@OneToMany(mappedBy="field", cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	private Set<CheckParam> checkParams = new HashSet<>();
	
	public Field() {
		
	}

	public Field(String fieldName, String fieldLabel, Study study, String dataType) {
		super();
		this.fieldName = fieldName;
		this.fieldLabel = fieldLabel;
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

	public String getFieldLabel() {
		return fieldLabel;
	}

	public void setFieldLabel(String fieldLabel) {
		this.fieldLabel = fieldLabel;
	}

	public Boolean getIsIdentifying() {
		return isIdentifying;
	}

	public void setIsIdentifying(Boolean isIdentifying) {
		this.isIdentifying = isIdentifying;
	}
	
	public String getDisplayDataType() {
		return DISPLAY_VALUES.get(this.dataType);
	}
	
	public boolean getIsNumeric() {
		return dataType.equals("Integer") || dataType.equals("Double");
	}

	public Set<CheckParam> getCheckParams() {
		return checkParams;
	}

	public void setCheckParams(Set<CheckParam> checkParams) {
		this.checkParams = checkParams;
	}
}
