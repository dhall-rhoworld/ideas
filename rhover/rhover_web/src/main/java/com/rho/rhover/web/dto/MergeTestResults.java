package com.rho.rhover.web.dto;

public class MergeTestResults {
	
	private String datasetName1;
	
	private String datasetName2;
	
	private Integer numRecords1;
	
	private Integer numRecords2;
	
	private Integer numMergedRecords;
	
	private String note;

	public MergeTestResults() {
		
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

	public Integer getNumRecords1() {
		return numRecords1;
	}

	public void setNumRecords1(Integer numRecords1) {
		this.numRecords1 = numRecords1;
	}

	public Integer getNumRecords2() {
		return numRecords2;
	}

	public void setNumRecords2(Integer numRecords2) {
		this.numRecords2 = numRecords2;
	}

	public Integer getNumMergedRecords() {
		return numMergedRecords;
	}

	public void setNumMergedRecords(Integer numMergedRecords) {
		this.numMergedRecords = numMergedRecords;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

}
