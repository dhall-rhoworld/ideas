package com.rho.rhover.web.dto;

import java.util.HashSet;
import java.util.Set;

public class CorrFieldDto {
	
	private String fieldInstanceId;
	
	private String fieldName;
	
	private String fieldLabel;
	
	private Set<String> correlatedFieldInstanceIds = new HashSet<>();

	public CorrFieldDto() {
		
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public Set<String> getCorrelatedFieldInstanceIds() {
		return correlatedFieldInstanceIds;
	}

	public void setCorrelatedFieldInstanceIds(Set<String> correlatedFieldInstanceIds) {
		this.correlatedFieldInstanceIds = correlatedFieldInstanceIds;
	}

	public String getFieldInstanceId() {
		return fieldInstanceId;
	}

	public void setFieldInstanceId(String fieldInstanceId) {
		this.fieldInstanceId = fieldInstanceId;
	}

	public String getFieldLabel() {
		return fieldLabel;
	}

	public void setFieldLabel(String fieldLabel) {
		this.fieldLabel = fieldLabel;
	}

}
