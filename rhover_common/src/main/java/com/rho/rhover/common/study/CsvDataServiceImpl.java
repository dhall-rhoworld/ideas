package com.rho.rhover.common.study;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rho.rhover.common.anomaly.UniAnomalyDto;
import com.rho.rhover.common.anomaly.UniAnomalyDtoRepository;
import com.rho.rhover.common.check.CheckRun;
import com.rho.rhover.common.study.CsvData;
import com.rho.rhover.common.study.CsvDataRepository;
import com.rho.rhover.common.study.Field;
import com.rho.rhover.common.study.FieldInstance;

@Service
public class CsvDataServiceImpl implements CsvDataService {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private CsvDataRepository csvDataRepository;
	
	@Autowired
	private UniAnomalyDtoRepository uniAnomalyDtoRepository;

	@Override
	public String getCsvData(List<FieldInstance> fieldInstances, boolean useFieldLabelsAsHeaders) {
		List<Iterator<String>> its = new ArrayList<>();
		for (FieldInstance fi : fieldInstances) {
			CsvData data = csvDataRepository.findByFieldAndDataset(fi.getField(), fi.getDataset());
			its.add(data.extractData().iterator());
		}
		
		// Add headers
		StringBuilder sb = new StringBuilder();
		int count = 0;
		for (FieldInstance fi : fieldInstances) {
			count++;
			if (count > 1) {
				sb.append(",");
			}
			if (useFieldLabelsAsHeaders) {
				sb.append(fi.getField().getFieldLabel().replaceAll("[,\\n\\r]", " "));
			}
			else {
				sb.append(fi.getField().getFieldName().replaceAll(",\\n\\r", " "));
			}
		}
		
		// Add data
		boolean stillHaveData = true;
		while (stillHaveData) {
			StringBuilder line = new StringBuilder();
			count = 0;
			for (Iterator<String> it : its) {
				if (!it.hasNext()) {
					stillHaveData = false;
					break;
				}
				count++;
				if (count > 1) {
					line.append(",");
				}
				line.append(it.next());
			}
			if (count == fieldInstances.size()) {
				sb.append(line.toString());
			}
		}
		
		return sb.toString();
	}
	
	@Override
	public String mergeToCsv(List<MergeField> mergeFields, List<FieldInstance> analysisFields, Boolean includeMergeFields,
			Boolean removeRecordsWithMissingValues) {
		logger.debug("Merging data to CSV");
		logger.debug("Found " + mergeFields.size() + " merge fields");
		logger.debug("Found " + analysisFields.size() + " anlaysis fields");
		
		// Create map from dataset names to data frames.  There should only be
		// 2 datasets represented in the method arguments.  Thus there should only
		// be 2 map entries.  Each data frame in the map will contain CSV data
		// "columns" including a set of columns for the merge variables and a set
		// of columns for the data variables.
		Map<String, DataFrame> dataFrames = new HashMap<>();
		
		// Add CSV data "columns" associated with merge variables to data frames
		for (MergeField mergeField : mergeFields) {
			logger.debug("Found merge field: "
					+ mergeField.getFieldInstance1().getField().getDisplayName());
			
			// Fetch/instantiate data frame for first field in mergeField
			FieldInstance fieldInstance = mergeField.getFieldInstance1();
			Dataset dataset = fieldInstance.getDataset();
			DataFrame dataFrame = dataFrames.get(dataset.getDatasetName());
			if (dataFrame == null) {
				dataFrame = new DataFrame();
				dataFrames.put(dataset.getDatasetName(), dataFrame);
				dataFrame.dataSetName = dataset.getDatasetName();
			}
			
			// Add merge data "column" to data frame
			CsvData csvData = csvDataRepository.findByFieldAndDataset(
					fieldInstance.getField(), dataset);
			dataFrame.mergeCols.add(csvData);
			
			// Fetch/instantiate data frame for second field in mergeField
			fieldInstance = mergeField.getFieldInstance2();
			dataset = fieldInstance.getDataset();
			dataFrame = dataFrames.get(dataset.getDatasetName());
			if (dataFrame == null) {
				dataFrame = new DataFrame();
				dataFrames.put(dataset.getDatasetName(), dataFrame);
				dataFrame.dataSetName = dataset.getDatasetName();
			}
			
			// Add merge data "column" to data frame
			CsvData csvData2 = csvDataRepository.findByFieldAndDataset(
					fieldInstance.getField(), dataset);
			dataFrame.mergeCols.add(csvData2);
		}
		
		// Add CSV data columns associated with the given analysis data fields to data frames
		for (FieldInstance analysisField : analysisFields) {
			logger.debug("Found analysis field: " + analysisField.getField().getDisplayName());
			CsvData csvData = csvDataRepository.findByFieldAndDataset(
					analysisField.getField(), analysisField.getDataset());
			DataFrame df = dataFrames.get(analysisField.getDataset().getDatasetName());
			df.analysisCols.add(csvData);
		}
		
		// Create map to help construct merged CSV strings of merged data.
		// Keys are serialized tuples of merge data values as comma-separated strings.
		// Values are lists serialized tuples of analysis data values as comma-separated strings.
		Map<String, List<String>> mergeTuplesToAnalysisTuples = new HashMap<>();
		
		// Arbitrarily select a data frame
		Iterator<DataFrame> dataFrameIt = dataFrames.values().iterator();
		DataFrame dataFrame = dataFrameIt.next();
		
		// Get iterator that will return tuples of merge data and
		// analysis data as comma-separated strings
		RowIterator mergeTupleIterator = dataFrame.mergeTupleIterator();
		RowIterator analysisTupleIterator = dataFrame.analysisTupleIterator(removeRecordsWithMissingValues);
		
		// Populate map
		while (mergeTupleIterator.hasNext()) {
			String key = mergeTupleIterator.next();
			List<String> analysisTuples = mergeTuplesToAnalysisTuples.get(key);
			if (analysisTuples == null) {
				analysisTuples = new ArrayList<>();
				mergeTuplesToAnalysisTuples.put(key, analysisTuples);
			}
			String tuple = analysisTupleIterator.next();
			if (tuple != null) {
				analysisTuples.add(tuple);
			}
		}
		logger.debug("Found " + mergeTuplesToAnalysisTuples.size()
			+ " records in dataset " + dataFrame.dataSetName);
		
		// Add header to output
		StringBuilder builder = new StringBuilder();
		if (includeMergeFields) {
			int count = 0;
			for (MergeField mergeField : mergeFields) {
				count++;
				if (count > 1) {
					builder.append(",");
				}
				builder.append(mergeField.getFieldInstance1().getField().getDisplayName());
			}
			builder.append(",");
		}
		int count = 0;
		for (FieldInstance fieldInstance : analysisFields) {
			count++;
			if (count > 1) {
				builder.append(",");
			}
			builder.append(fieldInstance.getField().getDisplayName());
		}
		builder.append("\n");
		
		// Fetch second data frame and build output.  Output will only contain records
		// where a given merge data tuple is found in both datasets.
		dataFrame = dataFrameIt.next();
		mergeTupleIterator = dataFrame.mergeTupleIterator();
		analysisTupleIterator = dataFrame.analysisTupleIterator(removeRecordsWithMissingValues);
		int numRecs = 0;
		int numMergedRecs = 0;
		while (mergeTupleIterator.hasNext()) {
			numRecs++;
			String mergeTuple = mergeTupleIterator.next();
			
			// If merge data tuple from second dataset is also in first dataset,
			// create merged record
			if (mergeTuplesToAnalysisTuples.containsKey(mergeTuple)) {
				String analysisTuple1 = analysisTupleIterator.next();
				if (analysisTuple1 != null) {
					List<String> analysisTuples2 = mergeTuplesToAnalysisTuples.get(mergeTuple);
					for (String analysisTuple2 : analysisTuples2) {
						numMergedRecs++;
						if (includeMergeFields.equals(Boolean.TRUE)) {
							builder.append(mergeTuple + ",");
						}
						builder.append(analysisTuple2);
						builder.append("," + analysisTuple1 + "\n");
					}
				}
			}
		}
		logger.debug("Found " + numRecs + " records in dataset " + dataFrame.dataSetName);
		logger.debug("Merged dataset contains " + numMergedRecs + " records");
		//logger.debug("\n" + builder.toString().substring(0, 100));
		
		return builder.toString();
	}
	

	private static final class DataFrame {
		private List<CsvData> mergeCols = new ArrayList<>();
		private List<CsvData> analysisCols = new ArrayList<>();
		private String dataSetName;
		
		private List<List<String>> extractMergeData() {
			List<List<String>> mergeData = new ArrayList<>();
			for (CsvData mergeCol : mergeCols) {
				mergeData.add(mergeCol.extractData());
			}
			return mergeData;
		}
		
		private List<List<String>> extractAnalysisData() {
			List<List<String>> data = new ArrayList<>();
			for (CsvData dataCol : analysisCols) {
				data.add(dataCol.extractData());
			}
			return data;
		}
		
		private RowIterator mergeTupleIterator() {
			return new RowIterator(extractMergeData());
		}
		
		private RowIterator analysisTupleIterator(boolean returnNullIfDataMissing) {
			RowIterator it = new RowIterator(extractAnalysisData());
			it.returnNullIfDataMissing = returnNullIfDataMissing;
			return it;
		}
	}
	
	private static final class RowIterator implements Iterator<String> {
		
		private List<Iterator<String>> iterators = new ArrayList<>();
		private boolean returnNullIfDataMissing = false;
		
		private RowIterator(List<List<String>> data) {
			for (List<String> dataCol : data) {
				iterators.add(dataCol.iterator());
			}
		}

		@Override
		public boolean hasNext() {
			boolean haveNext = true;
			for (Iterator<String> iterator : iterators) {
				haveNext = haveNext && iterator.hasNext();
			}
			return haveNext;
		}

		@Override
		public String next() {
			StringBuilder builder = new StringBuilder();
			int count = 0;
			for (Iterator<String> iterator : iterators) {
				count++;
				if (count > 1) {
					builder.append(",");
				}
				String value = iterator.next();
				if (isMissingData(value) && returnNullIfDataMissing) {
					return null;
				}
				builder.append(value);
			}
			return builder.toString();
		}
	}

	@Override
	public String getAsCsv(Dataset dataset, Boolean removeRecordsWithMissingValues, Field... fields) {
		
		// Fetch data from database
		List<List<String>> columns = new ArrayList<>();
		for (Field field : fields) {
			CsvData csvData = csvDataRepository.findByFieldAndDataset(field, dataset);
			columns.add(csvData.extractData());
		}
		
		// Add column headings to output
		StringBuilder builder = new StringBuilder();
		int count = 0;
		for (Field field : fields) {
			count++;
			if (count > 1) {
				builder.append(",");
			}
			builder.append(field.getDisplayName());
		}
		builder.append("\n");
		
		// Add data to output
		List<Iterator<String>> iterators = new ArrayList<>();
		for (List<String> column : columns) {
			iterators.add(column.iterator());
		}
		boolean haveData = true;
		while (haveData) {
			count = 0;
			StringBuilder record = new StringBuilder();
			boolean missingData = false;
			for (Iterator<String> iterator : iterators) {
				count++;
				if (count > 1) {
					record.append(",");
				}
				if (iterator.hasNext()) {
					String value = iterator.next();
					if (isMissingData(value)) {
						missingData = true;
					}
					record.append(value);
				}
				else {
					haveData = false;
					break;
				}
			}
			if (missingData && removeRecordsWithMissingValues) {
				continue;
			}
			if (haveData) {
				builder.append(record.toString() + "\n");
			}
		}
		//logger.debug(builder.toString().substring(builder.length() - 100));
		return builder.toString();
	}

	private static boolean isMissingData(String value) {
		return
			value == null
			|| value.trim().length() == 0
			|| value.equalsIgnoreCase("null");
	}

	@Override
	public String getCsvData(CheckRun checkRun) {
		
		// Assemble fields
		Study study = checkRun.getDatasetVersion().getDataset().getStudy();
		Set<Field> idFields = study.getUniqueIdentifierFields();
		List<Field> fields = new ArrayList<>();
		fields.addAll(idFields);
		Field subjectField = study.getSubjectField();
		Field siteField = study.getSiteField();
		if (!fields.contains(subjectField)) {
			fields.add(subjectField);
		}
		if (!fields.contains(siteField)) {
			fields.add(siteField);
		}
		fields.add(checkRun.getField());
		
		// Assemble data records
		List<StringTokenizer> tokenizers = new ArrayList<>();
		List<String> fieldNames = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		int count = 0;
		for (Field field : fields) {
			count++;
			if (count > 1) {
				builder.append(",");
			}
			builder.append(field.getDisplayName());
			CsvData csvData = csvDataRepository.findByFieldAndDataset(field, checkRun.getDatasetVersion().getDataset());
			tokenizers.add(new StringTokenizer(csvData.getData(), ","));
			fieldNames.add(field.getFieldName());
		}
		builder.append(",anomaly_id\n");
		boolean moreRecords = true;
		List<Record> records = new ArrayList<>();
		while (moreRecords) {
			boolean hasNull = false;
			Record record = new Record();
			for (int i = 0; i < fieldNames.size(); i++) {
				String fieldName = fieldNames.get(i);
				StringTokenizer tok = tokenizers.get(i);
				if (!tok.hasMoreTokens()) {
					moreRecords = false;
					break;
				}
				String token = tok.nextToken();
				if (token.trim().length() == 0 || token.equals("null")) {
					hasNull = true;
				}
				record.fieldValues.put(fieldName, token);
				if (fieldName.equals(checkRun.getField().getFieldName())) {
					record.dataValue = token;
				}
			}
			if (moreRecords) {
				if (!hasNull) {
					records.add(record);
				}
				if (record.dataValue == null) {
					throw new RuntimeException();
				}
			}
		}
		
		addAnomalyIds(records, checkRun, idFields);
		fieldNames.add("anomaly_id");
		
		Collections.sort(records);
		for (Record record : records) {
			builder.append(record.toCsv(fieldNames) + "\n");
		}
		//logger.debug(builder.toString());
		return builder.toString();
	}
	
	private void addAnomalyIds(List<Record> records, CheckRun checkRun, Set<Field> idFields) {
		List<UniAnomalyDto> dtos = uniAnomalyDtoRepository.findByCheckRunId(checkRun.getCheckRunId());
		Map<String, Long> anomalyIdIndex = new HashMap<>();
		for (UniAnomalyDto dto : dtos) {
			anomalyIdIndex.put(concatenateKeysAndValues(dto.getIdFieldNamesAndValues(), idFields), dto.getAnomalyId());
		}
		for (Record record : records) {
			String key = concatenateKeysAndValues(record.fieldValues, idFields);
			Long anomalyId = anomalyIdIndex.get(key);
			if (anomalyId == null) {
				anomalyId = 0L;
			}
			record.fieldValues.put("anomaly_id", anomalyId.toString());
		}
	}
	
	private String concatenateKeysAndValues(Map<String, String> map, Set<Field> idFields) {
		StringBuilder builder = new StringBuilder();
		int count = 0;
		for (Field field : idFields) {
			count++;
			if (count > 1) {
				builder.append(",");
			}
			String fieldName = field.getFieldName();
			builder.append(fieldName + "=" + map.get(fieldName));
		}
		return builder.toString();
	}
	
	private static final class Record implements Comparable<Record> {
		
		private Logger logger = LoggerFactory.getLogger(this.getClass());
		
		private Map<String, String> fieldValues = new HashMap<>();
		
		private String dataValue;
		
		private String toCsv(List<String> fieldNames) {
			StringBuilder builder = new StringBuilder();
			int count = 0;
			for (String fieldName : fieldNames) {
				count++;
				if (count > 1) {
					builder.append(",");
				}
				//logger.debug(fieldName + ": " + fieldValues.get(fieldName));
				builder.append(fieldValues.get(fieldName));
			}
			return builder.toString();
		}
	
		@Override
		public int compareTo(Record o) {
			return new Double(this.dataValue).compareTo(new Double(o.dataValue));
		}
		
	}
}



