package com.rho.rhover.web.dto;

import java.util.ArrayList;
import java.util.List;

import com.rho.rhover.common.study.Field;

public class FieldDto implements Comparable<FieldDto> {

	private Long fieldId;
	
	private String fieldName;
	
	private String dataType;
	
	private Boolean isIdentifying;

	public FieldDto(Long fieldId, String fieldName, String dataType, Boolean isIdentifying) {
		super();
		this.fieldId = fieldId;
		this.fieldName = fieldName;
		this.dataType = dataType;
		this.isIdentifying = isIdentifying;
	}

	public FieldDto(Field field) {
		this(field.getFieldId(), field.getFieldName(), field.getDataType(), field.getIsIdentifying());
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
