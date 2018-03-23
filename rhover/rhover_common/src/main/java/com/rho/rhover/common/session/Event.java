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

import com.rho.rhover.common.check.CheckParam;
import com.rho.rhover.common.check.CheckParamChange;
import com.rho.rhover.common.study.DataLocation;
import com.rho.rhover.common.study.Dataset;
import com.rho.rhover.common.study.Field;
import com.rho.rhover.common.study.FieldInstance;
import com.rho.rhover.common.study.Study;
import com.rho.rhover.common.study.StudyDbVersion;

@Entity
public class Event {
	
	public enum EventType {ADD_STUDY, ADD_DATA_LOCATION, LOAD_STUDY, NEW_CHECK_PARAM, MODIFIED_CHECK_PARAM,
		ADD_DATASET_CHECK, REMOVE_DATASET_CHECK, DEACTIVATE_CHECK_PARAM, ADD_FIELD_SKIP, REMOVE_FIELD_SKIP};
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="event_id")
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
	
	@ManyToOne
	@JoinColumn(name="study_db_version_id")
	private StudyDbVersion studyDbVersion;
	
	@ManyToOne
	@JoinColumn(name="check_param_id")
	private CheckParam checkParam;
	
	@ManyToOne
	@JoinColumn(name="check_param_change_id")
	private CheckParamChange checkParamChange;
	
	@ManyToOne
	@JoinColumn(name="dataset_id")
	private Dataset dataset;
	
	@ManyToOne
	@JoinColumn(name="field_id")
	private Field field;
	
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
	
	public static Event newNewCheckParamEvent(UserSession userSession, CheckParam checkParam) {
		Event event = new Event();
		event.setUserSession(userSession);
		event.setEventType(EventType.NEW_CHECK_PARAM);
		event.setCheckParam(checkParam);
		return event;
	}
	
	public static Event newModifiedCheckParamEvent(UserSession userSession, CheckParamChange checkParamChange) {
		Event event = new Event();
		event.setUserSession(userSession);
		event.setEventType(EventType.MODIFIED_CHECK_PARAM);
		event.setCheckParamChange(checkParamChange);
		return event;
	}
	
	public static Event newAddDatasetCheckEvent(UserSession userSession, Dataset dataset) {
		Event event = new Event();
		event.setUserSession(userSession);
		event.setEventType(EventType.ADD_DATASET_CHECK);
		event.setDataset(dataset);
		return event;
	}
	
	public static Event newRemoveDatasetCheckEvent(UserSession userSession, Dataset dataset) {
		Event event = new Event();
		event.setUserSession(userSession);
		event.setEventType(EventType.REMOVE_DATASET_CHECK);
		event.setDataset(dataset);
		return event;
	}
	
	public static Event newDeactivateCheckParamEvent(UserSession userSession, CheckParam checkParam) {
		Event event = new Event();
		event.setUserSession(userSession);
		event.setEventType(EventType.DEACTIVATE_CHECK_PARAM);
		event.setCheckParam(checkParam);
		return event;
	}
	
	public static Event newAddSkipEvent(UserSession userSession, Field field) {
		Event event = new Event();
		event.setUserSession(userSession);
		event.setEventType(EventType.ADD_FIELD_SKIP);
		event.setField(field);
		return event;
	}
	
	public static Event newRemoveSkipEvent(UserSession userSession, Field field) {
		Event event = new Event();
		event.setUserSession(userSession);
		event.setEventType(EventType.REMOVE_FIELD_SKIP);
		event.setField(field);
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

	public StudyDbVersion getStudyDbVersion() {
		return studyDbVersion;
	}

	public void setStudyDbVersion(StudyDbVersion studyDbVersion) {
		this.studyDbVersion = studyDbVersion;
	}

	public CheckParam getCheckParam() {
		return checkParam;
	}

	public void setCheckParam(CheckParam checkParam) {
		this.checkParam = checkParam;
	}

	public CheckParamChange getCheckParamChange() {
		return checkParamChange;
	}

	public void setCheckParamChange(CheckParamChange checkParamChange) {
		this.checkParamChange = checkParamChange;
	}

	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	public Field getField() {
		return field;
	}

	public void setField(Field field) {
		this.field = field;
	}

}
