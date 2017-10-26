package com.rho.rhover.study;

public interface StudyDataRepository {

	String getAllDataFieldValues(Long dataFieldId);
	
	Double getLowerThreshold(Long dataFieldId);
	
	Double getUpperThreshold(Long dataFieldId);
	
	String getDataFieldName(Long dataFieldId);
	
	String getDatasetName(Long dataFieldId);
	
	String getStudyName(Long dataFieldId);
	
	Long getDatasetId(Long dataFieldId);
	
	Long getStudyId(Long dataFieldId);
}
