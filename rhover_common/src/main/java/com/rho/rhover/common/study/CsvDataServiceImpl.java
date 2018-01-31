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
	public String getCsvData(List<FieldInstance> fieldInstances, boolean useFieldLabelsAsHeaders, boolean removeNulls) {
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
		sb.append("\n");
		
		// Add data
		boolean stillHaveData = true;
		while (stillHaveData) {
			StringBuilder line = new StringBuilder();
			count = 0;
			boolean dataOk = true;
			for (Iterator<String> it : its) {
				if (!it.hasNext()) {
					stillHaveData = false;
					break;
				}
				count++;
				if (count > 1) {
					line.append(",");
				}
				String value = it.next().replaceAll(",\\n\\r", " ");
				if (isNull(value) && removeNulls) {
					dataOk = false;
				}
				line.append(value);
			}
			if (count == fieldInstances.size() && dataOk) {
				sb.append(line.toString() + "\n");
			}
		}
		
		return sb.toString();
	}
	
	private boolean isNull(String value) {
		return value == null || value.trim().length() == 0 || value.equalsIgnoreCase("null");
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
		
		if (checkRun.getBivariateCheck() != null && !checkRun.getBivariateCheck().fieldsInSameDataset()) {
			throw new UnsupportedOperationException();
		}
		
		// Fetch data
		Dataset dataset = null;
		Iterator<String> values = null;
		Iterator<String> values2 = null;
		if (checkRun.getBivariateCheck() == null) {
			dataset = checkRun.getDatasetVersion().getDataset();
			values = csvDataRepository.findByFieldAndDataset(checkRun.getField(), dataset).extractData().iterator();
		}
		else {
			dataset = checkRun.getBivariateCheck().getxFieldInstance().getDataset();
			values = csvDataRepository.findByFieldAndDataset(checkRun.getBivariateCheck().getxFieldInstance().getField(), dataset).extractData().iterator();
			values2 = csvDataRepository.findByFieldAndDataset(checkRun.getBivariateCheck().getyFieldInstance().getField(), dataset).extractData().iterator();
		}
		Study study = dataset.getStudy();
		Iterator<String> subjects =
				csvDataRepository.findByFieldAndDataset(study.getSubjectField(), dataset).extractData().iterator();
		Iterator<String> sites =
				csvDataRepository.findByFieldAndDataset(study.getSiteField(), dataset).extractData().iterator();
		Iterator<String> phases =
				csvDataRepository.findByFieldAndDataset(study.getPhaseField(), dataset).extractData().iterator();
		Iterator<String> recordIds =
				csvDataRepository.findByFieldAndDataset(study.getRecordIdField(), dataset).extractData().iterator();
		
		// Package data into Record objects
		List<Record> records = new ArrayList<>();
		while (subjects.hasNext() && sites.hasNext() && phases.hasNext() && recordIds.hasNext() && values.hasNext()) {
			Record rec = new Record();
			rec.subjectName = subjects.next();
			rec.siteName = sites.next();
			rec.phaseName = phases.next();
			rec.recordId = recordIds.next();
			rec.dataValue = values.next();
			if (values2 != null) {
				rec.dataValue2 = values2.next();
			}
			if (!isNull(rec.dataValue)) {
				if (checkRun.getBivariateCheck() == null) {
					records.add(rec);
				}
				else if (!isNull(rec.dataValue2)) {
					records.add(rec);
				}
			}
		}
		
		// Add anomaly IDs to records
		addAnomalyIds(records, checkRun);
		
		// Sort data by value of checked field
		Collections.sort(records);
		
		// Add column names to output
		StringBuilder builder = new StringBuilder();
		builder.append(study.getSubjectField().getDisplayName()
				+ "," + study.getSiteField().getDisplayName()
				+ "," + study.getPhaseField().getDisplayName()
				+ "," + study.getRecordIdField().getDisplayName());
		if (checkRun.getBivariateCheck() == null) {
			builder.append("," + checkRun.getField().getDisplayName());
		}
		else {
			builder.append("," + checkRun.getBivariateCheck().getxFieldInstance().getField().getDisplayName() + ","
					+ checkRun.getBivariateCheck().getyFieldInstance().getField().getDisplayName());
		}
		builder.append(",anomaly_id\n");
		
		// Add data to output
		for (Record rec : records) {
			builder.append(rec.subjectName
					+ "," + rec.siteName
					+ "," + rec.phaseName
					+ "," + rec.recordId
					+ "," + rec.dataValue
					);
			if (rec.dataValue2 != null) {
				builder.append("," + rec.dataValue2);
			}
			builder.append("," + rec.anomalyId + "\n");
		}
		
		
		//logger.debug(builder.toString());
		return builder.toString();
	}
	
	private void addAnomalyIds(List<Record> records, CheckRun checkRun) {
		List<UniAnomalyDto> dtos = uniAnomalyDtoRepository.findByCheckRunId(checkRun.getCheckRunId());
		logger.debug("Num anomalies: " + dtos.size());
		Map<String, Long> anomalyIdIndex = new HashMap<>();
		StringBuilder sb = new StringBuilder();
		for (UniAnomalyDto dto : dtos) {
			String key = generateKey(dto.getSubjectName(), dto.getSiteName(), dto.getPhaseName(), dto.getRecordId());
//			sb.append("\n" + key);
			anomalyIdIndex.put(key, dto.getAnomalyId());
		}
//		logger.debug(sb.toString());
//		logger.debug("++++++++++++++++++++");
		sb = new StringBuilder();
		for (Record record : records) {
			String key = generateKey(record.subjectName, record.siteName, record.phaseName, record.recordId);
			sb.append("\n" + key);
			Long anomalyId = anomalyIdIndex.get(key);
			if (anomalyId == null) {
				anomalyId = 0L;
			}
			record.anomalyId = anomalyId;
		}
//		logger.debug(sb.toString());
	}
	
	private String generateKey(String subjectName, String siteName, String phaseName, String recordId) {
		return subjectName + "---" + siteName + "---" + phaseName + "---" + recordId;
	}
	
	private static final class Record implements Comparable<Record> {
		
		private Logger logger = LoggerFactory.getLogger(this.getClass());
		
		private String subjectName;
		
		private String siteName;
		
		private String phaseName;
		
		private String recordId;
		
		private String dataValue;
		
		private String dataValue2;
		
		private Long anomalyId = 0L;
	
		@Override
		public int compareTo(Record o) {
			return new Double(this.dataValue).compareTo(new Double(o.dataValue));
		}
		
	}
}



