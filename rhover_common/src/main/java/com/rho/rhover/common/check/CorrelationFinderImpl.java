package com.rho.rhover.common.check;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rho.rhover.common.study.CsvData;
import com.rho.rhover.common.study.CsvDataRepository;
import com.rho.rhover.common.study.Dataset;
import com.rho.rhover.common.study.DatasetRepository;
import com.rho.rhover.common.study.DatasetVersion;
import com.rho.rhover.common.study.DatasetVersionRepository;
import com.rho.rhover.common.study.Field;
import com.rho.rhover.common.study.FieldInstance;
import com.rho.rhover.common.study.FieldInstanceRepository;
import com.rho.rhover.common.study.FieldRepository;
import com.rho.rhover.common.study.Study;

// TODO: Consolidate correlation code in DataLoaderServiceImpl with this

@Service
public class CorrelationFinderImpl implements CorrelationFinder {
	
	private static final int MIN_RECORDS = 10;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private FieldRepository fieldRepository;
	
	@Autowired
	private DatasetRepository datasetRepository;
	
	@Autowired
	private DatasetVersionRepository datasetVersionRepository;
	
	@Autowired
	private FieldInstanceRepository fieldInstanceRepository;
	
	@Autowired
	private CsvDataRepository csvDataRepository;

	@Override
	public List<Correlation> findAllCorrelatedFields(FieldInstance fieldInstance, double minCorrelationCoeff) {
		
		// Extract subjects and data values associated with given field instance
		Study study = fieldInstance.getField().getStudy();
		Field subjectField = study.getSubjectField();
		List<String> subjects1 = csvDataRepository.findByFieldAndDataset(subjectField, fieldInstance.getDataset()).extractData();
		List<Double> data1 = csvDataRepository.findByFieldAndDataset(fieldInstance.getField(), fieldInstance.getDataset()).extractDataAsDouble();
		
		// Iterate through all datasets
		List<Dataset> datasets = datasetRepository.findByStudy(study);
		List<Correlation> correlations = new ArrayList<Correlation>();
		for (Dataset dataset : datasets) {
			//logger.debug("Processing dataset: " + dataset.getDatasetName());
			
			// Extract subject data
			CsvData subjects2Data = csvDataRepository.findByFieldAndDataset(subjectField, dataset);
			if (subjects2Data == null) {
				logger.warn("No subject field in dataset: " + dataset.getDatasetName());
				continue;
			}
			List<String> subjects2 = subjects2Data.extractData();
			
			// Iterate through all fields in dataset
			DatasetVersion datasetVersion = datasetVersionRepository.findByDatasetAndIsCurrent(dataset, Boolean.TRUE);
			Set<Field> fields = datasetVersion.getFields();
			for (Field field : fields) {
				if (field.equals(fieldInstance.getField())) {
					continue;
				}
				if (field.getDataType().equals("Double") || field.getDataType().equals("Integer")) {
					//logger.debug("Processing field: " + field.getDisplayName());
					try {
						List<Double> data2 = csvDataRepository.findByFieldAndDataset(field, dataset).extractDataAsDouble();
						double coeff = computeCorrelation(subjects1, data1, subjects2, data2);
						if (Math.abs(coeff) >= minCorrelationCoeff) {
							FieldInstance fieldInstance2 = fieldInstanceRepository.findByFieldAndDataset(field, dataset);
							correlations.add(new Correlation(study, fieldInstance, fieldInstance2, coeff));
//							logger.debug(fieldInstance.getField().getFieldName() + " and " + field.getFieldName()
//								+ " [" + dataset.getDatasetName() + "] are correlated");
						}
					}
					catch (NumberFormatException e) {
						logger.warn("Error converting data to numeric for dataset " + dataset.getDatasetName() + " field " + field.getDisplayName());
					}
				}
			}
		}
		
		return correlations;
	}

	private double computeCorrelation(List<String> subjects1, List<Double> data1, List<String> subjects2,
			List<Double> data2) {
		
		// Merge data by subject
		List<Double> merged1 = new ArrayList<>();
		List<Double> merged2 = new ArrayList<>();
		Map<String, Double> index = new HashMap<>();
		for (int i = 0; i < data1.size(); i++) {
			Double value = data1.get(i);
			if (!Double.isNaN(value)) {
				String key = subjects1.get(i);
				index.put(key, value);
			}
		}
		for (int i = 0; i < data2.size(); i++) {
			String key = subjects2.get(i);
			if (index.containsKey(key)) {
				Double value = data2.get(i);
				if (!Double.isNaN(value)) {
					merged1.add(index.get(key));
					merged2.add(value);
				}
			}
		}
		if (merged1.size() < MIN_RECORDS) {
			return 0.0;
		}
		
		// Repackage data for API call
		double[] array1 = new double[merged1.size()];
		double[] array2 = new double[merged2.size()];
		for (int i = 0; i < merged1.size(); i++) {
			array1[i] = merged1.get(i);
		}
		for (int i = 0; i < merged2.size(); i++) {
			array2[i] = merged2.get(i);
		}
		
		// Calculate coefficient
		double coefficient = 0;
		try {
			coefficient = new PearsonsCorrelation().correlation(array1, array2);
		}
		catch (Exception e) {
			logger.warn("Exception encountered calculating correlation coefficient: " + e.getMessage());
		}
		if (Double.isNaN(coefficient)) {
			coefficient = 0.0;
		}
		
		return coefficient;
	}

}
