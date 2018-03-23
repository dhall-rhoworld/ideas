package com.rho.rhover.web.dto;

import java.util.ArrayList;
import java.util.List;

public class MergeFieldSetupInfo {
	
	private String datasetName1;
	
	private String datasetName2;
	
	private List<FieldDto> fields = new ArrayList<>();

	public MergeFieldSetupInfo() {
		
	}

	public String getDatasetName1() {
		return datasetName1;
	}

	public void setDatasetName1(String datasetName1) {
		this.datasetName1 = datasetName1;
	}

	public String getDatasetName2() {
		return datasetName2;
	}

	public void setDatasetName2(String datasetName2) {
		this.datasetName2 = datasetName2;
	}

	public List<FieldDto> getFields() {
		return fields;
	}

	public void setFields(List<FieldDto> fields) {
		this.fields = fields;
	}
}
