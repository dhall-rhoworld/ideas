package com.rho.rhover.anomaly;

public class Anomaly {

	private final String recruitId;
	private final String event;
	private final String fieldValue;
	private final String versionFirstSeenIn;
	
	public Anomaly(String recruitId, String event, String fieldValue, String versionFirstSeenIn) {
		super();
		this.recruitId = recruitId;
		this.event = event;
		this.fieldValue = fieldValue;
		this.versionFirstSeenIn = versionFirstSeenIn;
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
