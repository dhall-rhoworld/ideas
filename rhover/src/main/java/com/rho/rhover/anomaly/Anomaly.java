package com.rho.rhover.anomaly;

public class Anomaly {

	private final Long anomalyId;
	private final String recruitId;
	private final String event;
	private final String fieldValue;
	private final String versionFirstSeenIn;
	
	public Anomaly(Long anomalyId, String recruitId, String event, String fieldValue, String versionFirstSeenIn) {
		super();
		this.anomalyId = anomalyId;
		this.recruitId = recruitId;
		this.event = event;
		this.fieldValue = fieldValue;
		this.versionFirstSeenIn = versionFirstSeenIn;
	}
	
	public Long getAnomalyId() {
		return anomalyId;
	}

	public String getRecruitId() {
		return recruitId;
	}

	public String getEvent() {
		return event;
	}

	public String getFieldValue() {
		return fieldValue;
	}

	public String getVersionFirstSeenIn() {
		return versionFirstSeenIn;
	}

}
