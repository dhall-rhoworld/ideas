package com.rho.rhover.common.anomaly;

import com.rho.rhover.common.study.Site;
import com.rho.rhover.common.study.Subject;

// TODO: Migrate to Spring CrudRepository
public interface AnomalyRepositoryOld {

	Iterable<AnomalyOld> getCurrentAnomalies(Long dataFieldId);
	
	Iterable<AnomalyOld> getCurrentAnomalies(Long dataFieldId, Site site);
	
	Iterable<AnomalyOld> getCurrentAnomalies(Long dataFieldId, Subject subject);
	
	int setIsAnIssue(Iterable<Long> anomalyIds, boolean isAnIssue);
	
	int setIsAnIssue(Long dataFieldId, String[] recruitIds, String[] events, boolean isAnIssue);
}
