package com.rho.rhover.common.study;

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
	@JoinColumn(name="dataset_version_id")
	private DatasetVersion datasetVersion;
	
	public LoaderIssue() {
		
	}

	public LoaderIssue(String message, String stackTrace, IssueLevel issueLevel) {
		super();
		this.message = message;
		this.stackTrace = stackTrace;
		this.issueLevel = issueLevel;
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
}
