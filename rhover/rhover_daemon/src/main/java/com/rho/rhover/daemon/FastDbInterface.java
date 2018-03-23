package com.rho.rhover.daemon;

import java.util.Map;

import javax.sql.DataSource;

import org.springframework.stereotype.Component;

@Component
public class FastDbInterface {

	private DataSource dataSource;
	
	public Map<String, Long> buildSiteNameToIdMap(Long studyId) {
		return null;
	}
	
	public Map<String, Long> buildSubjectNameToIdMap(Long studyId) {
		return null;
	}
	
	public Map<String, Long> buildPhaseNameToIdMap(Long studyId) {
		return null;
	}
	
	public Map<String, Long> buildRecordIdToObservationIdMap(Long datasetId) {
		return null;
	}
}
