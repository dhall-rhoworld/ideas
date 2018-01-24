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
	
	private Map<String, String> idFieldNamesAndValues = new HashMap<>();
	
	private Long subjectId;
	
	private Long siteId;

	public UniAnomalyDto() {
		
	}

	public UniAnomalyDto(Long checkRunId, Long anomalyId, Long fieldId, String fieldName, String anomalousValue,
			Long subjectId, Long siteId) {
		super();
		this.checkRunId = checkRunId;
		this.anomalyId = anomalyId;
		this.fieldId = fieldId;
		this.fieldName = fieldName;
		this.anomalousValue = anomalousValue;
		this.subjectId = subjectId;
		this.siteId = siteId;
	}

	public void addIdFieldNameAndValue(String name, String value) {
		idFieldNamesAndValues.put(name, value);
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

	public Map<String, String> getIdFieldNamesAndValues() {
		return idFieldNamesAndValues;
	}

	public Long getSubjectId() {
		return subjectId;
	}

	public Long getSiteId() {
		return siteId;
	}

	@Override
	public int compareTo(UniAnomalyDto o) {
		return new Double(this.anomalousValue).compareTo(new Double(o.anomalousValue));
	}
	
}
