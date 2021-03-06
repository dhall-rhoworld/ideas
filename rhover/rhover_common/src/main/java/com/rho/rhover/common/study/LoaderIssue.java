package com.rho.rhover.common.study;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class LoaderIssue {
	
	public enum IssueLevel {STUDY, STUDY_DATABASE, STUDY_DATABASE_VERSION, DATASET, DATASET_VERSION, SUBJECT};

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="loader_issue_id")
	private Long loaderIssueId;
	
	@Column(name="message")
	private String message;
	
	@Column(name="stack_trace")
	private String stackTrace;
	
	@Enumerated(EnumType.STRING)
	@Column(name="issue_level")
	private IssueLevel issueLevel;
	
	@ManyToOne
	@JoinColumn(name="study_db_version_id")
	private StudyDbVersion studyDbVersion;
	
	@ManyToOne
	@JoinColumn(name="dataset_version_id")
	private DatasetVersion datasetVersion;
	
	@ManyToOne
	@JoinColumn(name="study_id")
	private Study study;
	
	public LoaderIssue() {
		
	}

	public LoaderIssue(String message, String stackTrace, IssueLevel issueLevel) {
		super();
		this.message = message;
		this.stackTrace = stackTrace;
		this.issueLevel = issueLevel;
	}
	
	public LoaderIssue(String message, Throwable cause, IssueLevel issueLevel) {
		super();
		this.message = message;
		this.issueLevel = issueLevel;
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		cause.printStackTrace(printWriter);
		if (cause.getCause() != null) {
			printWriter.append("\n\nCaused by:");
			cause.getCause().printStackTrace(printWriter);
		}
		this.stackTrace = stringWriter.toString();
	}

	public Long getLoaderIssueId() {
		return loaderIssueId;
	}

	public void setLoaderIssueId(Long loaderIssueId) {
		this.loaderIssueId = loaderIssueId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getStackTrace() {
		return stackTrace;
	}

	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}

	public DatasetVersion getDatasetVersion() {
		return datasetVersion;
	}

	public IssueLevel getIssueLevel() {
		return issueLevel;
	}

	public void setIssueLevel(IssueLevel issueLevel) {
		this.issueLevel = issueLevel;
	}

	public void setDatasetVersion(DatasetVersion datasetVersion) {
		this.datasetVersion = datasetVersion;
	}

	public StudyDbVersion getStudyDbVersion() {
		return studyDbVersion;
	}

	public void setStudyDbVersion(StudyDbVersion studyDbVersion) {
		this.studyDbVersion = studyDbVersion;
	}

	public Study getStudy() {
		return study;
	}

	public void setStudy(Study study) {
		this.study = study;
	}
	
}
