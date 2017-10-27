package com.rho.rhover.anomaly;

public interface AnomalyRepository {

	Iterable<Anomaly> getCurrentAnomalies(Long dataFieldId);
	
	int setIsAnIssue(Iterable<Long> anomalyIds, boolean isAnIssue);
	
	int setIsAnIssue(Long dataFieldId, String[] recruitIds, String[] events, boolean isAnIssue);
}
