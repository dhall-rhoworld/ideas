package com.rho.rhover.anomaly;

import com.rho.rhover.study.Site;
import com.rho.rhover.study.Subject;

// TODO: Migrate to Spring CrudRepository
public interface AnomalyRepository {

	Iterable<Anomaly> getCurrentAnomalies(Long dataFieldId);
	
	Iterable<Anomaly> getCurrentAnomalies(Long dataFieldId, Site site);
	
	Iterable<Anomaly> getCurrentAnomalies(Long dataFieldId, Subject subject);
	
	int setIsAnIssue(Iterable<Long> anomalyIds, boolean isAnIssue);
	
	int setIsAnIssue(Long dataFieldId, String[] recruitIds, String[] events, boolean isAnIssue);
}
