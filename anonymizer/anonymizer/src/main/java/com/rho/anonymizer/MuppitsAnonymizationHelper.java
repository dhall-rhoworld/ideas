package com.rho.anonymizer;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class MuppitsAnonymizationHelper implements AnonymizationHelper {
	
	private int lastSubjectId = 0;
	private int lastSiteId = 0;
	
	private static final Set<String> GENERATED_FIELDS = new HashSet<String>(Arrays.asList(
			"SUBJECT",
			"SITEID"
			));
	
	private static final Set<String> DROP_FIELDS = new HashSet<String>(Arrays.asList(
			"ID",
			"SEQNO",
			"STUDYSITENUMBER",
			"COMPLDT",
			"COMPLDT_RAW",
			"COMPLDT_INT",
			"COMPLDT_YYYY",
			"COMPLDT_MM",
			"COMPLDT_DD",
			"STFINIT",
			"VSDAT",
			"VSDAT_RAW",
			"VSDAT_INT",
			"VSDAT_YYYY",
			"VSDAT_MM",
			"VSDAT_DD",
			"VSTIM",
			"PROJECTID",
			"PROJECT",
			"STUDYID",
			"ENVIRONMENTNAME",
			"SUBJECTID",
			"SITE",
			"SITENUMBER",
			"SITEGROUP",
			"RECORDDATE",
			"MINCREATED",
			"MAXUPDATED",
			"SAVETS",
			"STUDYSITEID"
			));

	public String generateValue(String fieldName) {
		String value = null;
		if (fieldName.equals("SUBJECT")) {
			lastSubjectId++;
			value = String.valueOf(lastSubjectId);
		}
		else if (fieldName.equals("SITEID")) {
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

	
}
