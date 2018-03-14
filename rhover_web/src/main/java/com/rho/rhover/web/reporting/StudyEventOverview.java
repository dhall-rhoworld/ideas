package com.rho.rhover.web.reporting;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * Summary information on a system event at the study level, like loading data or running
 * data checks.
 * 
 * @author dhall
 *
 */
public abstract class StudyEventOverview {
	
	private Timestamp eventStarted;
	private Timestamp eventStopped;
	private String studyName;
	private Long studyDbVersionId;
	private Integer numIssues = 0;

	public StudyEventOverview() {
		
	}

	public String getStudyName() {
		return studyName;
	}

	public void setStudyName(String studyName) {
		this.studyName = studyName;
	}
	
	public Timestamp getEventStarted() {
		return eventStarted;
	}

	public void setEventStarted(Timestamp eventStarted) {
		this.eventStarted = eventStarted;
	}

	public Timestamp getEventStopped() {
		return eventStopped;
	}

	public void setEventStopped(Timestamp eventStopped) {
		this.eventStopped = eventStopped;
	}

	public Long getStudyDbVersionId() {
		return studyDbVersionId;
	}

	public void setStudyDbVersionId(Long studyDbVersionId) {
		this.studyDbVersionId = studyDbVersionId;
	}

	public Integer getNumIssues() {
		return numIssues;
	}

	public void setNumIssues(Integer numIssues) {
		this.numIssues = numIssues;
	}

	public String getFormattedEventStarted() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(eventStarted);
	}
	
	public String getFormattedElapsedTime() {
		StringBuilder builder = new StringBuilder();
		long elapsedSec = (eventStopped.getTime() - eventStarted.getTime()) / 1000;
		long seconds = elapsedSec % 60;
		long minutes = (long)(Math.floor(elapsedSec / 60.0));
		if (minutes > 0) {
			builder.append(minutes + " min. ");
		}
		builder.append(seconds + " sec.");
		return builder.toString();
	}
	
	public abstract String getEventType();
}
