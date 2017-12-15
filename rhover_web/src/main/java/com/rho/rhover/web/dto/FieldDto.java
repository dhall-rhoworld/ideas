package com.rho.rhover.web.dto;

import com.rho.rhover.common.study.Field;

public class FieldDto implements Comparable<FieldDto> {
	
	private static final int MAX_LABEL_LENGTH = 40;

	private Long fieldId;
	
	private String fieldName;
	
	private String fieldLabel = "";
	
	private String dataType;
	
	private Boolean isIdentifying;

	public FieldDto(Long fieldId, String fieldName, String fieldLabel, String dataType, Boolean isIdentifying) {
		super();
		this.fieldId = fieldId;
		this.fieldName = fieldName;
		this.fieldLabel = fieldLabel;
		this.dataType = dataType;
		this.isIdentifying = isIdentifying;
	}

	public FieldDto(Field field) {
		this(field.getFieldId(), field.getFieldName(), field.getTruncatedDisplayName(MAX_LABEL_LENGTH), field.getDisplayDataType(), field.getIsIdentifying());
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

	public String getFieldLabel() {
		return fieldLabel;
	}

	public void setFieldLabel(String fieldLabel) {
		this.fieldLabel = fieldLabel;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	
	public Boolean getIsIdentifying() {
		return isIdentifying;
	}

	public void setIsIdentifying(Boolean isIdentifying) {
		this.isIdentifying = isIdentifying;
	}

	@Override
	public int compareTo(FieldDto other) {
		int val = 0;
		if (this.isIdentifying && !other.isIdentifying) {
			val = -1;
		}
		if (!this.isIdentifying && other.isIdentifying) {
			val = 1;
		}
		if (val == 0) {
			val = this.fieldName.compareTo(other.fieldName);
		}
		return val;
	}
}
