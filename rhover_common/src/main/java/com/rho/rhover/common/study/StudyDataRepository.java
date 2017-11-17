package com.rho.rhover.common.study;

// TODO: Do away with this.  Data will be stored in the database.
public interface StudyDataRepository {

	String getUnivariateData(Long dataFieldId);
	
	String getBivariateData(Long bivariateCheckId);
	
	// TODO: This really belongs somewhere else
	void markAnomaliesAsViewed(Long dataFieldId);
}
