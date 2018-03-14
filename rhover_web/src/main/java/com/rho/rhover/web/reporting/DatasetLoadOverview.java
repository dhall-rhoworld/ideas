package com.rho.rhover.web.reporting;

public class DatasetLoadOverview {

	private String datasetName;
	
	private Long datasetVersionId;
	
	private Integer numNewFields;
	
	private Integer numNewRecords;
	
	private Integer numModifiedDataValues;
	
	public DatasetLoadOverview() {
		
	}

	public String getDatasetName() {
		return datasetName;
	}

	public void setDatasetName(String datasetName) {
		this.datasetName = datasetName;
	}

	public Long getDatasetVersionId() {
		return datasetVersionId;
	}

	public void setDatasetVersionId(Long datasetVersionId) {
		this.datasetVersionId = datasetVersionId;
	}

	public Integer getNumNewFields() {
		return numNewFields;
	}

	public void setNumNewFields(Integer numNewFields) {
		this.numNewFields = numNewFields;
	}

	public Integer getNumNewRecords() {
		return numNewRecords;
	}

	public void setNumNewRecords(Integer numNewRecords) {
		this.numNewRecords = numNewRecords;
	}

	public Integer getNumModifiedDataValues() {
		return numModifiedDataValues;
	}

	public void setNumModifiedDataValues(Integer numModifiedDataValues) {
		this.numModifiedDataValues = numModifiedDataValues;
	}

}
