package com.rho.anonymizer;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class ProseAnonymizationHelper implements AnonymizationHelper {
	
	private int lastSubjectId = 0;
	private int lastSiteId = 0;
	
	private static final Set<String> GENERATED_FIELDS = new HashSet<String>(Arrays.asList(
			"RecruitID",
			"Site"
			));
	
	private static final Set<String> DROP_FIELDS = new HashSet<String>(Arrays.asList(
			"StudyID",
			"version",
			"sstatus",
			"aPI"
			));

	public String generateValue(String fieldName) {
		String value = null;
		if (fieldName.equals("RecruitID")) {
			lastSubjectId++;
			value = String.valueOf(lastSubjectId);
		}
		else if (fieldName.equals("Site")) {
			lastSiteId++;
			value = String.valueOf(lastSiteId);
		}
		return value;
	}

	public boolean fieldMustBeGenerated(String fieldName) {
		return GENERATED_FIELDS.contains(fieldName);
	}

	public String dateFormatString() {
		// TODO Auto-generated method stub
		return null;
	}

	public Date extractDate(String dateStr, String fieldName) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean dropField(String fieldName) {
		return DROP_FIELDS.contains(fieldName);
	}

	public boolean isADate(String value) {
		return value.matches("[0-9]{2} [A-Z]{3} [0-9]{4}");
	}

	public boolean generateRecordId() {
		
		return true;
	}
}
