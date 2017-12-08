package com.rho.rhover.web.dto;

import java.util.HashSet;
import java.util.Set;

public class CorrFieldDto {
	
	private String fieldName;
	
	private Set<String> correlatedFields = new HashSet<>();

	public CorrFieldDto() {
		
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public Set<String> getCorrelatedFields() {
		return correlatedFields;
	}

	public void setCorrelatedFields(Set<String> correlatedFields) {
		this.correlatedFields = correlatedFields;
	}

}
