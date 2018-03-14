package com.rho.rhover.web.reporting;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class StudyLoadOverview {

	private Timestamp loadStarted;
	private Timestamp loadStopped;
	private String studyName;
	private Integer numNewDatasets;
	private Integer numModifiedDatasets;
	private Integer totalDatasets;
	private Long studyDbVersionId;
	
	public StudyLoadOverview() {
		
	}

	public String getStudyName() {
		return studyName;
	}

	public void setStudyName(String studyName) {
		this.studyName = studyName;
	}

	public Integer getNumNewDatasets() {
		return numNewDatasets;
	}

	public void setNumNewDatasets(Integer numNewDatasets) {
		this.numNewDatasets = numNewDatasets;
	}

	public Timestamp getLoadStarted() {
		return loadStarted;
	}

	public void setLoadStarted(Timestamp loadStarted) {
		this.loadStarted = loadStarted;
	}

	public Timestamp getLoadStopped() {
		return loadStopped;
	}

	public void setLoadStopped(Timestamp loadStopped) {
		this.loadStopped = loadStopped;
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

	public Long getStudyDbVersionId() {
		return studyDbVersionId;
	}

	public void setStudyDbVersionId(Long studyDbVersionId) {
		this.studyDbVersionId = studyDbVersionId;
	}

	public String getFormattedLoadStarted() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(loadStarted);
	}
	
	public String getFormattedElapsedLoadTime() {
		StringBuilder builder = new StringBuilder();
		long elapsedSec = (loadStopped.getTime() - loadStarted.getTime()) / 1000;
		long seconds = elapsedSec % 60;
		long minutes = (long)(Math.floor(elapsedSec / 60.0));
		if (minutes > 0) {
			builder.append(minutes + " min. ");
		}
		builder.append(seconds + " sec.");
		return builder.toString();
	}
}
