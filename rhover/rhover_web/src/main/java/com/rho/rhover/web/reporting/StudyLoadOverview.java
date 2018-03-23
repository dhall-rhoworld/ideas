package com.rho.rhover.web.reporting;

public class StudyLoadOverview extends StudyEventOverview {

	
	private Integer numNewDatasets;
	private Integer numModifiedDatasets;
	private Integer totalDatasets;
	
	
	public StudyLoadOverview() {
		
	}

	public Integer getNumNewDatasets() {
		return numNewDatasets;
	}

	public void setNumNewDatasets(Integer numNewDatasets) {
		this.numNewDatasets = numNewDatasets;
	}

	public Integer getNumModifiedDatasets() {
		return numModifiedDatasets;
	}

	public void setNumModifiedDatasets(Integer numModifiedDatasets) {
		this.numModifiedDatasets = numModifiedDatasets;
	}

	public Integer getTotalDatasets() {
		return totalDatasets;
	}

	public void setTotalDatasets(Integer totalDatasets) {
		this.totalDatasets = totalDatasets;
	}

	@Override
	public String getEventType() {
		return "Data Load";
	}
}
