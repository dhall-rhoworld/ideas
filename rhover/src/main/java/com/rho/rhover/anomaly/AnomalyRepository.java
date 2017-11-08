package com.rho.rhover.anomaly;

// TODO: Migrate to Spring CrudRepository
public interface AnomalyRepository {

	Iterable<Anomaly> getCurrentAnomalies(Long dataFieldId);
	
	Iterable<Anomaly> getCurrentAnomalies(Long dataFieldId, Long siteId);
	
	int setIsAnIssue(Iterable<Long> anomalyIds, boolean isAnIssue);
	
	int setIsAnIssue(Long dataFieldId, String[] recruitIds, String[] events, boolean isAnIssue);
}
