package com.rho.rhover.common.check;

import java.util.List;

import com.rho.rhover.common.study.Dataset;
import com.rho.rhover.common.study.Field;
import com.rho.rhover.common.study.FieldInstance;
import com.rho.rhover.common.study.MergeField;

public interface CsvDataService {

	String mergeToCsv(List<MergeField> mergeFields, List<FieldInstance> dataFields, Boolean includeMergeFields,
			Boolean removeRecordsWithMissingValues);
	
	String getAsCsv(Dataset dataset, Boolean removeRecordsWithMissingValues, Field ...fields);
}
