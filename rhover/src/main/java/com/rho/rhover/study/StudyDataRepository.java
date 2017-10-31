package com.rho.rhover.study;

public interface StudyDataRepository {

	String getUnivariateData(Long dataFieldId);
	
	String getBivariateData(Long bivariateCheckId);
	
}
