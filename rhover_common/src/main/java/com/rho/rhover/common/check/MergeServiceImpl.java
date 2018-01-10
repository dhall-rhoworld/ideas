package com.rho.rhover.common.check;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rho.rhover.common.study.CsvData;
import com.rho.rhover.common.study.CsvDataRepository;
import com.rho.rhover.common.study.Dataset;
import com.rho.rhover.common.study.FieldInstance;
import com.rho.rhover.common.study.MergeField;

@Service
public class MergeServiceImpl implements MergeService {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private CsvDataRepository csvDataRepository;

	@Override
	public String mergeToCsv(List<MergeField> mergeFields, List<FieldInstance> analysisFields) {
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
		// Values are serilized tuples of analysis data values as comma-separated strings.
		Map<String, String> mergeTuplesToAnalysisTuples = new HashMap<>();
		
		// Arbitrarily select a data frame
		Iterator<DataFrame> dataFrameIt = dataFrames.values().iterator();
		DataFrame dataFrame = dataFrameIt.next();
		
		// Get iterator that will return tuples of merge data and
		// analysis data as comma-separated strings
		RowIterator mergeTupleIterator = dataFrame.mergeTupleIterator();
		RowIterator analysisTupleIterator = dataFrame.analysisTupleIterator();
		
		// Populate map
		while (mergeTupleIterator.hasNext()) {
			mergeTuplesToAnalysisTuples.put(mergeTupleIterator.next(), analysisTupleIterator.next());
		}
		logger.debug("Found " + mergeTuplesToAnalysisTuples.size()
			+ " records in dataset " + dataFrame.dataSetName);
		
		// Fetch second data frame and build output.  Output will only contain records
		// where a given merge data tuple is found in both datasets.
		StringBuilder builder = new StringBuilder();
		dataFrame = dataFrameIt.next();
		mergeTupleIterator = dataFrame.mergeTupleIterator();
		analysisTupleIterator = dataFrame.analysisTupleIterator();
		int numRecs = 0;
		int numMergedRecs = 0;
		while (mergeTupleIterator.hasNext()) {
			numRecs++;
			String mergeTuple = mergeTupleIterator.next();
			
			// If merge data tuple from second dataset is also in first dataset,
			// create merged record
			if (mergeTuplesToAnalysisTuples.containsKey(mergeTuple)) {
				numMergedRecs++;
				String dataTuple = analysisTupleIterator.next();
				builder.append(mergeTuple);
				builder.append("," + mergeTuplesToAnalysisTuples.get(mergeTuple));
				builder.append("," + dataTuple + "\n");
			}
		}
		logger.debug("Found " + numRecs + " records in dataset " + dataFrame.dataSetName);
		logger.debug("Merged dataset contains " + numMergedRecs + " records");
		
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
		
		private RowIterator analysisTupleIterator() {
			return new RowIterator(extractAnalysisData());
		}
	}
	
	private static final class RowIterator implements Iterator<String> {
		
		private List<Iterator<String>> iterators = new ArrayList<>();
		
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
				builder.append(iterator.next());
			}
			return builder.toString();
		}
		
	}
}
