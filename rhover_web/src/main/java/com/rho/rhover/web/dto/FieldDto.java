package com.rho.rhover.web.dto;

import com.rho.rhover.common.study.Field;
import com.rho.rhover.common.study.FieldInstance;

public class FieldDto implements Comparable<FieldDto> {
	
	private static final int MAX_LABEL_LENGTH = 40;

	private Long fieldInstanceId;
	
	private String fieldName;
	
	private String fieldLabel = "";
	
	private String dataType;
	
	private Boolean isIdentifying;
	
	private Long fieldId;
	
	public FieldDto() {
		
	}

	public FieldDto(Long fieldInstanceId, String fieldName, String fieldLabel, String dataType, Boolean isIdentifying) {
		super();
		this.fieldInstanceId = fieldInstanceId;
		this.fieldName = fieldName;
		this.fieldLabel = fieldLabel;
		this.dataType = dataType;
		this.isIdentifying = isIdentifying;
	}

	public FieldDto(FieldInstance fieldInstance) {
		this.fieldInstanceId = fieldInstance.getFieldInstanceId();
		Field field = fieldInstance.getField();
		this.fieldName = field.getFieldName();
		this.fieldLabel = field.getTruncatedDisplayName(MAX_LABEL_LENGTH);
		this.dataType = field.getDisplayDataType();
		this.isIdentifying = field.getIsIdentifying();
	}

	public Long getFieldInstanceId() {
		return fieldInstanceId;
	}

	public void setFieldInstanceId(Long fieldInstanceId) {
		this.fieldInstanceId = fieldInstanceId;
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
	
	public Long getFieldId() {
		return fieldId;
	}

	public void setFieldId(Long fieldId) {
		this.fieldId = fieldId;
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

	public String getDisplayName() {
		String name = fieldLabel;
		if (name == null || name.length() == 0) {
			name = fieldName;
		}
		return name;
	}
	
}
