package com.rho.rhover.web.admin;

import java.text.SimpleDateFormat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name="logging_event")
public class LoggingEvent {
	
	@Transient
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="event_id")
	private Long eventId;
	
	@Column(name="timestmp")
	private Long timestamp;
	
	@Column(name="formatted_message")
	private String formattedMessage;
	
	@Column(name="level_string")
	private String levelString;
	
	@Column(name="caller_filename")
	private String callerFileName;
	
	@Column(name="caller_class")
	private String callerClass;
	
	@Column(name="caller_method")
	private String callerMethod;
	
	@Column(name="caller_line")
	private String callerLine;

	public LoggingEvent() {
		
	}

	public Long getEventId() {
		return eventId;
	}

	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public String getFormattedMessage() {
		return formattedMessage;
	}

	public void setFormattedMessage(String formattedMessage) {
		this.formattedMessage = formattedMessage;
	}

	public String getLevelString() {
		return levelString;
	}

	public void setLevelString(String levelString) {
		this.levelString = levelString;
	}

	public String getCallerFileName() {
		return callerFileName;
	}

	public void setCallerFileName(String callerFileName) {
		this.callerFileName = callerFileName;
	}

	public String getCallerClass() {
		return callerClass;
	}

	public void setCallerClass(String callerClass) {
		this.callerClass = callerClass;
	}

	public String getCallerMethod() {
		return callerMethod;
	}

	public void setCallerMethod(String callerMethod) {
		this.callerMethod = callerMethod;
	}

	public String getCallerLine() {
		return callerLine;
	}

	public void setCallerLine(String callerLine) {
		this.callerLine = callerLine;
	}

	public String getFormattedDateTime() {
		return dateFormat.format(timestamp);
	}
}
