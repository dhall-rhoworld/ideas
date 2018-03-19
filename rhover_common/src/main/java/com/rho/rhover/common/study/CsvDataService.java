package com.rho.rhover.common.study;

import java.util.List;

import com.rho.rhover.common.check.CheckRun;
import com.rho.rhover.common.study.FieldInstance;

// TODO: Clean this up.  Methods were consolidated from several earlier interfaces with same name but
// in different packages.
public interface CsvDataService {
	
	public enum HeaderOption {FIELD_NAMES, FIELD_LABELS, FIELD_DISPLAY_NAMES, NO_HEADER};

	String getCsvData(CheckRun checkRun);
	
	/**
	 * Return a serialized rectangular dataset composed of the given fields.
	 * @param fieldInstances Fields to put in CSV output.
	 * @param useFieldLabelsAsHeaders If TRUE, headers in the output string will be
	 * field labels.  Otherwise, field names will be used.
	 * @param removeNulls If TRUE, records containing any null values will not be included
	 * @return A comma-separated encoding of data records.
	 */
	String getCsvData(List<FieldInstance> fieldInstances, boolean useFieldLabelsAsHeaders, boolean removeNulls);
	
	String mergeToCsv(List<MergeField> mergeFields, List<FieldInstance> dataFields, Boolean includeMergeFields,
			Boolean removeRecordsWithMissingValues);
	
	String getAsCsv(Dataset dataset, Boolean removeRecordsWithMissingValues, Field ...fields);
	
	String getCurrentDataAndIdFieldsAsCsv(FieldInstance fieldInstance, HeaderOption headerOption);
	
	String getCurrentDataAndIdFieldsAndAnomalyIdsAsCsv(FieldInstance fieldInstance, HeaderOption headerOption);
}
