package com.rho.rhover.web.dto;

public class FieldInstanceDto {
	
	private Long fieldInstanceId;
	
	private String fieldDisplayName;
	
	private String dataTypeDisplayName;
	
	private String dataSetName;

	public FieldInstanceDto() {
		
	}

	public FieldInstanceDto(Long fieldInstanceId, String fieldDisplayName, String dataTypeDisplayName,
			String dataSetName) {
		super();
		this.fieldInstanceId = fieldInstanceId;
		this.fieldDisplayName = fieldDisplayName;
		this.dataTypeDisplayName = dataTypeDisplayName;
		this.dataSetName = dataSetName;
	}

	public Long getFieldInstanceId() {
		return fieldInstanceId;
	}

	public void setFieldInstanceId(Long fieldInstanceId) {
		this.fieldInstanceId = fieldInstanceId;
	}

	public String getFieldDisplayName() {
		return fieldDisplayName;
	}

	public void setFieldDisplayName(String fieldDisplayName) {
		this.fieldDisplayName = fieldDisplayName;
	}

	public String getDataTypeDisplayName() {
		return dataTypeDisplayName;
	}

	public void setDataTypeDisplayName(String dataTypeDisplayName) {
		this.dataTypeDisplayName = dataTypeDisplayName;
	}

	public String getDataSetName() {
		return dataSetName;
	}

	public void setDataSetName(String dataSetName) {
		this.dataSetName = dataSetName;
	}

	public String getDisplayValue() {
		return fieldDisplayName + " [" + dataSetName + "]";
	}
}
