package com.rho.rhover.common.session;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.rho.rhover.common.study.DataLocation;
import com.rho.rhover.common.study.Study;

@Entity
public class Event {
	
	public enum EventType {ADD_STUDY, ADD_DATA_LOCATION};
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="evemt_id")
	private Long eventId;
	
	@Enumerated(EnumType.STRING)
	@Column(name="event_type")
	private EventType eventType;
	
	@ManyToOne
	@JoinColumn(name="user_session_id")
	private UserSession userSession;
	
	@ManyToOne
	@JoinColumn(name="study_id")
	private Study study;
	
	@ManyToOne
	@JoinColumn(name="data_location_id")
	private DataLocation dataLocation;
	
	@Column(name="created_on")
	private Timestamp createdOn;

	public Event() {
		
	}
	
	public static Event newAddStudyEvent(UserSession userSession, Study study) {
		Event event = new Event();
		event.setUserSession(userSession);
		event.setEventType(EventType.ADD_STUDY);
		event.setStudy(study);
		return event;
	}
	
	public static Event newAddDataLocationEvent(UserSession userSession, DataLocation dataLocation) {
		Event event = new Event();
		event.setUserSession(userSession);
		event.setEventType(EventType.ADD_DATA_LOCATION);
		event.setDataLocation(dataLocation);
		return event;
	}

	public Long getEventId() {
		return eventId;
	}

	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}

	public EventType getEventType() {
		return eventType;
	}

	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}

	public UserSession getUserSession() {
		return userSession;
	}

	public void setUserSession(UserSession userSession) {
		this.userSession = userSession;
	}

	public Study getStudy() {
		return study;
	}

	public void setStudy(Study study) {
		this.study = study;
	}

	public DataLocation getDataLocation() {
		return dataLocation;
	}

	public void setDataLocation(DataLocation dataLocation) {
		this.dataLocation = dataLocation;
	}

	public Timestamp getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Timestamp createdOn) {
		this.createdOn = createdOn;
	}

}
