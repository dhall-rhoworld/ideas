package com.rho.rhover.study;

public interface StudyDataRepository {

	String getAllDataFieldValues(Long dataFieldId);
	
	Double getLowerThreshold(Long dataFieldId);
	
	Double getUpperThreshold(Long dataFieldId);
}
