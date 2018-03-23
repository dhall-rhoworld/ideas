package com.rho.rhover.web.dto;

import java.util.ArrayList;
import java.util.List;

public class CorrDatasetDto {
	
	private String datasetId;
	
	private String datasetName;
	
	private List<CorrFieldDto> fields = new ArrayList<>();

	public CorrDatasetDto() {
		
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

	public String getDatasetId() {
		return datasetId;
	}

	public void setDatasetId(String datasetId) {
		this.datasetId = datasetId;
	}

}
