package com.rho.rhover.anomaly;

public class Anomaly {

	private final Long anomalyId;
	private final String recruitId;
	private final String event;
	private final String fieldValue;
	private final String versionFirstSeenIn;
	
	// TODO: Consider creating Site entity and make this a many-to-one
	private final String siteName;
	
	public Anomaly(Long anomalyId, String recruitId, String event, String fieldValue, String versionFirstSeenIn, String siteName) {
		super();
		this.anomalyId = anomalyId;
		this.recruitId = recruitId;
		this.event = event;
		this.fieldValue = fieldValue;
		this.versionFirstSeenIn = versionFirstSeenIn;
		this.siteName = siteName;
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

	public String getSiteName() {
		return siteName;
	}

}
