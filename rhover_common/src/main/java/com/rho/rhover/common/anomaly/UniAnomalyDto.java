package com.rho.rhover.common.anomaly;

import java.util.HashMap;
import java.util.Map;

/**
 * Data transfer object for univariate anomaly data
 * @author dhall
 *
 */
public class UniAnomalyDto extends AnomalyDto implements Comparable<UniAnomalyDto> {
	
	private Long fieldId;
	
	private String fieldName;
	
	private String anomalousValue;
	
	public UniAnomalyDto() {
		
	}


	public UniAnomalyDto(Long checkRunId, Long anomalyId, Long fieldId, String fieldName, String anomalousValue,
			Long phaseId, String phaseName, Long subjectId, String subjectName, Long siteId, String siteName,
			String recordId, Boolean isAnIssue, Long queryCandidateId) {
		super();
		this.checkRunId = checkRunId;
		this.anomalyId = anomalyId;
		this.fieldId = fieldId;
		this.fieldName = fieldName;
		this.anomalousValue = anomalousValue;
		this.phaseId = phaseId;
		this.phaseName = phaseName;
		this.subjectId = subjectId;
		this.subjectName = subjectName;
		this.siteId = siteId;
		this.siteName = siteName;
		this.recordId = recordId;
		this.isAnIssue = isAnIssue;
		this.queryCandidateId = queryCandidateId;
	}


	public Long getFieldId() {
		return fieldId;
	}

	public String getFieldName() {
		return fieldName;
	}

	public String getAnomalousValue() {
		return anomalousValue;
	}

	public void setFieldId(Long fieldId) {
		this.fieldId = fieldId;
	}


	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}


	public void setAnomalousValue(String anomalousValue) {
		this.anomalousValue = anomalousValue;
	}


	@Override
	public int compareTo(UniAnomalyDto o) {
		return new Double(this.anomalousValue).compareTo(new Double(o.anomalousValue));
	}

}
