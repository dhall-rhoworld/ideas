package com.rho.rhover.common.anomaly;

import java.util.HashMap;
import java.util.Map;

/**
 * Data transfer object for univariate anomaly data
 * @author dhall
 *
 */
public class UniAnomalyDto implements Comparable<UniAnomalyDto> {
	
	private Long checkRunId;
	
	private Long anomalyId;
	
	private Long fieldId;
	
	private String fieldName;
	
	private String anomalousValue;
	
	private Long phaseId;
	
	private String phaseName;
	
	private Long subjectId;
	
	private String subjectName;
	
	private Long siteId;
	
	private String siteName;
	
	private String recordId;

	public UniAnomalyDto() {
		
	}


	public UniAnomalyDto(Long checkRunId, Long anomalyId, Long fieldId, String fieldName, String anomalousValue,
			Long phaseId, String phaseName, Long subjectId, String subjectName, Long siteId, String siteName,
			String recordId) {
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
	}


	public Long getCheckRunId() {
		return checkRunId;
	}

	public Long getAnomalyId() {
		return anomalyId;
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


	@Override
	public int compareTo(UniAnomalyDto o) {
		return new Double(this.anomalousValue).compareTo(new Double(o.anomalousValue));
	}

}
