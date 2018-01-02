package com.rho.rhover.web.dto;

public class JqueryUiAutocompleteDto {
	
	private String label;
	
	private String value;

	public JqueryUiAutocompleteDto() {
		
	}

	public JqueryUiAutocompleteDto(String label, String value) {
		super();
		this.label = label;
		this.value = value;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
