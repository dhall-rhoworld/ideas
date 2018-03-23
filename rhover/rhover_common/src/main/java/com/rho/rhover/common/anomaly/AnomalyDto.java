package com.rho.rhover.common.anomaly;

public class AnomalyDto {

	protected Long checkRunId;
	protected Long anomalyId;
	protected Long phaseId;
	protected String phaseName;
	protected Long subjectId;
	protected String subjectName;
	protected Long siteId;
	protected String siteName;
	protected String recordId;
	protected Boolean isAnIssue;
	protected Long queryCandidateId;

	public AnomalyDto() {
		super();
	}

	public Long getCheckRunId() {
		return checkRunId;
	}

	public Long getAnomalyId() {
		return anomalyId;
	}

	public Long getSubjectId() {
		return subjectId;
	}

	public Long getSiteId() {
		return siteId;
	}

	public Long getPhaseId() {
		return phaseId;
	}

	public String getRecordId() {
		return recordId;
	}

	public String getPhaseName() {
		return phaseName;
	}

	public String getSubjectName() {
		return subjectName;
	}

	public String getSiteName() {
		return siteName;
	}

	public void setCheckRunId(Long checkRunId) {
		this.checkRunId = checkRunId;
	}

	public void setAnomalyId(Long anomalyId) {
		this.anomalyId = anomalyId;
	}

	public void setPhaseId(Long phaseId) {
		this.phaseId = phaseId;
	}

	public void setPhaseName(String phaseName) {
		this.phaseName = phaseName;
	}

	public void setSubjectId(Long subjectId) {
		this.subjectId = subjectId;
	}

	public void setSubjectName(String subjectName) {
		this.subjectName = subjectName;
	}

	public void setSiteId(Long siteId) {
		this.siteId = siteId;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	public void setRecordId(String recordId) {
		this.recordId = recordId;
	}

	public Boolean getIsAnIssue() {
		return isAnIssue;
	}

	public void setIsAnIssue(Boolean isAnIssue) {
		this.isAnIssue = isAnIssue;
	}

	public Long getQueryCandidateId() {
		return queryCandidateId;
	}

	public void setQueryCandidateId(Long queryCandidateId) {
		this.queryCandidateId = queryCandidateId;
	}

}