package com.rho.rhover.web.reporting;

public class FailedLoadOverview extends StudyEventOverview {
	
	private String errorMessage;

	public FailedLoadOverview() {
		
	}

	@Override
	public String getEventType() {
		return "Failed Load";
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
