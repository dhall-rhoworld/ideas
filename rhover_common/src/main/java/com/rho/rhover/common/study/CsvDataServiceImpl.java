package com.rho.rhover.common.study;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Service;

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
	private FieldInstanceRepository fieldInstanceRepository;
	
	@Autowired
	private DataSource dataSource;

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
		FieldInstance fieldInstance = fieldInstanceRepository.findByFieldAndDataset(checkRun.getField(), checkRun.getDatasetVersion().getDataset());
		return getCurrentDataAndIdFieldsAndAnomalyIdsAsCsv(fieldInstance, HeaderOption.FIELD_DISPLAY_NAMES);
	}

	@Override
	public String getCurrentDataAndIdFieldsAsCsv(FieldInstance fieldInstance, HeaderOption headerOption) {
		
		// Add header
		StringBuilder results = new StringBuilder();
		addHeader(results, fieldInstance, headerOption, false);
		
		// Add data
		String sql =
				"select si.site_name, p.phase_name, s.subject_name, o.record_id, cast(dv.value as decimal) value\n" + 
				"from datum_version dv\n" + 
				"join datum d on d.datum_id = dv.datum_id\n" + 
				"join observation o on o.observation_id = d.observation_id\n" + 
				"join subject s on s.subject_id = o.subject_id\n" + 
				"join site si on si.site_id = o.site_id\n" + 
				"join phase p on p.phase_id = o.phase_id\n" + 
				"where o.dataset_id = " + fieldInstance.getDataset().getDatasetId() + "\n" +
				"and d.field_id = " + fieldInstance.getField().getFieldId() + "\n" +  
				"and dv.is_current = 1\n" +
				"order by value";
		logger.info(sql);
		JdbcTemplate template = new JdbcTemplate(dataSource);
		template.query(sql, new RowCallbackHandler() {
			public void processRow(ResultSet rs) throws SQLException {
				results.append(
						rs.getString(1) + "," +
						rs.getString(2) + "," +
						rs.getString(3) + "," +
						rs.getString(4) + "," +
						rs.getString(5) + "\n");
			}
		});
		
		return results.toString();
	}
	
	@Override
	public String getCurrentDataAndIdFieldsAndAnomalyIdsAsCsv(FieldInstance fieldInstance, HeaderOption headerOption) {
		
		// Add header
		StringBuilder results = new StringBuilder();
		addHeader(results, fieldInstance, headerOption, true);
		
		// Add data
		String sql =
				"select si.site_name, p.phase_name, s.subject_name, o.record_id, cast(dv.value as decimal) value,\n" + 
				"a.anomaly_id, a.is_an_issue, qc.query_candidate_id\n" + 
				"from datum_version dv\n" + 
				"join datum d on d.datum_id = dv.datum_id\n" + 
				"join observation o on o.observation_id = d.observation_id\n" + 
				"join subject s on s.subject_id = o.subject_id\n" + 
				"join site si on si.site_id = o.site_id\n" + 
				"join phase p on p.phase_id = o.phase_id\n" + 
				"left join anomaly_datum_version adv on adv.datum_version_id = dv.datum_version_id\n" + 
				"left join anomaly a on a.anomaly_id = adv.anomaly_id\n" + 
				"left join query_candidate qc on qc.anomaly_id = a.anomaly_id\n" + 
				"where o.dataset_id = " + fieldInstance.getDataset().getDatasetId() + "\n" +
				"and d.field_id = " + fieldInstance.getField().getFieldId() + "\n" +  
				"and dv.is_current = 1\n" + 
				"order by value";
		logger.info(sql);
		JdbcTemplate template = new JdbcTemplate(dataSource);
		template.query(sql, new RowCallbackHandler() {
			public void processRow(ResultSet rs) throws SQLException {
				results.append(
						rs.getString(1) + "," +
						rs.getString(2) + "," +
						rs.getString(3) + "," +
						rs.getString(4) + "," +
						rs.getString(5) + "," +
						rs.getLong(6) + "," +
						rs.getInt(7) + "," +
						rs.getLong(8) + "\n");
			}
		});
		
		return results.toString();
	}
	
	private void addHeader(StringBuilder builder, FieldInstance fieldInstance, HeaderOption headerOption, boolean includeAnomalyFields) {
		if (!headerOption.equals(HeaderOption.NO_HEADER)) {
			Study study = fieldInstance.getField().getStudy();
			builder.append(
				study.getSiteFieldName() + "," +
				study.getPhaseFieldName() + "," +
				study.getSubjectFieldName() + "," +
				study.getRecordIdFieldName() + ",");
			if (headerOption.equals(HeaderOption.FIELD_NAMES)) {
				builder.append(fieldInstance.getField().getFieldName());
			}
			else if (headerOption.equals(HeaderOption.FIELD_LABELS)) {
				builder.append(fieldInstance.getField().getFieldLabel());
			}
			else if (headerOption.equals(HeaderOption.FIELD_DISPLAY_NAMES)) {
				builder.append(fieldInstance.getField().getDisplayName());
			}
			if (includeAnomalyFields) {
				builder.append(",anomaly_id,is_an_issue,query_candidate_id");
			}
			builder.append("\n");
		}
	}
}



