package com.rho.rhover.web.dto;

import java.util.ArrayList;
import java.util.List;

public class CorrDatasetDto {
	
	private String datasetName;
	
	private List<CorrFieldDto> fields = new ArrayList<>();

	public CorrDatasetDto() {
		// TODO Auto-generated constructor stub
	}

	public String getDatasetName() {
		return datasetName;
	}

	public void setDatasetName(String datasetName) {
		this.datasetName = datasetName;
	}

	public List<CorrFieldDto> getFields() {
		return fields;
	}

	public void setFields(List<CorrFieldDto> fields) {
		this.fields = fields;
	}

}
