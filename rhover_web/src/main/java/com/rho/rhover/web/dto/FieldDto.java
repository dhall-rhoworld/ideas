package com.rho.rhover.web.dto;

import java.util.ArrayList;
import java.util.List;

import com.rho.rhover.common.study.Field;

public class FieldDto {

	private Long fieldId;
	
	private String fieldName;
	
	private String dataType;

	public FieldDto(Long fieldId, String fieldName, String dataType) {
		super();
		this.fieldId = fieldId;
		this.fieldName = fieldName;
		this.dataType = dataType;
	}

	public FieldDto(Field field) {
		this(field.getFieldId(), field.getFieldName(), field.getDataType());
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
	
	public static List<FieldDto> toDtos(Iterable<Field> fields) {
		List<FieldDto> dtos = new ArrayList<>();
		for (Field field : fields) {
			dtos.add(new FieldDto(field.getFieldId(), field.getFieldName(), field.getDataType()));
		}
		return dtos;
	}
}
