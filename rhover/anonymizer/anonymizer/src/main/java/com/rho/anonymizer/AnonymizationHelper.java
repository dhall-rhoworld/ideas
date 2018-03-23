package com.rho.anonymizer;

import java.util.Date;

public interface AnonymizationHelper {

	String generateValue(String fieldName);
	
	boolean fieldMustBeGenerated(String fieldName);
	
	Date extractDate(String dateStr, String fieldName);
	
	boolean dropField(String fieldName);
	
	boolean isADate(String value);
	
	boolean generateRecordId();

}
