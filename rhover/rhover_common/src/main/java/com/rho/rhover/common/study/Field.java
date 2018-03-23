package com.rho.rhover.common.study;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.Where;

import com.rho.rhover.common.check.CheckParam;

@Entity
public class Field {
	
	private static final int DISPLAY_LENGTH = 50;
	private static final int DISPLAY_LENGTH_SHORT = 40;
	
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
	//@GeneratedValue(strategy=GenerationType.IDENTITY)
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
	
	@ManyToMany(mappedBy = "fields")
	private Set<DatasetVersion> datasetVersions = new HashSet<>();
	
	@OneToMany(mappedBy="field", cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	@MapKey(name="paramName")
	@Where(clause="is_current = 1")
	private Map<String, CheckParam> checkParams = new HashMap<>();
	
	@Column(name="is_skipped")
	@Type(type="org.hibernate.type.NumericBooleanType")
	private Boolean isSkipped = Boolean.FALSE;
	
	public Field() {
		
	}

	public Field(String fieldName, String fieldLabel, Study study, String dataType) {
		super();
		this.fieldName = fieldName;
		this.fieldLabel = fieldLabel;
		this.study = study;
		this.dataType = dataType;
	}
	
	public Field(Long fieldId, String fieldName, String fieldLabel, Study study, String dataType) {
		super();
		this.fieldId = fieldId;
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

	public String getDisplayDataType() {
		return DISPLAY_VALUES.get(this.dataType);
	}
	
	public boolean getIsNumeric() {
		return dataType.equals("Integer") || dataType.equals("Double");
	}

	public Boolean getIsSkipped() {
		return isSkipped;
	}

	public void setIsSkipped(Boolean isChecked) {
		this.isSkipped = isChecked;
	}
	
	public CheckParam getCheckParam(String paramName) {
		return checkParams.get(paramName);
	}
	
	public Map<String, CheckParam> getCheckParams() {
		return checkParams;
	}

	public void setCheckParams(Map<String, CheckParam> checkParams) {
		this.checkParams = checkParams;
	}
	
	public String getDisplayName() {
		String displayName = fieldLabel;
		if (displayName == null || displayName.trim().length() == 0) {
			displayName = fieldName;
		}
		return displayName;
	}
	
	public String getTruncatedDisplayName(int length) {
		String name = fieldName;
		if (fieldLabel != null && fieldLabel.trim().length() > 0) {
			name = getTruncatedFieldLabel(length);
		}
		return name;
	}
	
	public String getVeryTruncatedDisplayName() {
		return getTruncatedDisplayName(DISPLAY_LENGTH_SHORT);	
	}
	
	public String getTruncatedDisplayName() {
		return getTruncatedDisplayName(DISPLAY_LENGTH);
	}

	public String getTruncatedFieldLabel() {
		return getTruncatedFieldLabel(DISPLAY_LENGTH);
	}
	
	public String getTruncatedFieldLabel(int length) {
		if (fieldLabel.length() < length) {
			return fieldLabel;
		}
		int segLen = length / 2;
		int p = fieldLabel.substring(0, segLen).lastIndexOf(" ");
		if (p < 0) {
			p = segLen;
		}
		int q = fieldLabel.length() - segLen;
		for (int i = q; i < fieldLabel.length(); i++) {
			if (fieldLabel.charAt(i) == ' ') {
				q = i;
				break;
			}
		}
		return fieldLabel.substring(0, p) + " ... " + fieldLabel.substring(q);
	}
	
	public DatasetVersion getCurrentDatasetVersion(Dataset dataset) {
		DatasetVersion current = null;
		for (DatasetVersion datasetVersion : datasetVersions) {
			if (datasetVersion.getIsCurrent() && datasetVersion.getDataset().equals(dataset)) {
				current = datasetVersion;
				break;
			}
		}
		return current;
	}
}
