package com.rho.rhover.daemon;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.Transactional;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rho.rhover.common.check.Correlation;
import com.rho.rhover.common.check.CorrelationRepository;
import com.rho.rhover.common.check.CorrelationService;
import com.rho.rhover.common.study.CsvData;
import com.rho.rhover.common.study.CsvDataRepository;
import com.rho.rhover.common.study.DataLocation;
import com.rho.rhover.common.study.DataLocationRepository;
import com.rho.rhover.common.study.DataStream;
import com.rho.rhover.common.study.DataStreamRepository;
import com.rho.rhover.common.study.Dataset;
import com.rho.rhover.common.study.DatasetRepository;
import com.rho.rhover.common.study.DatasetVersion;
import com.rho.rhover.common.study.LoaderIssue;
import com.rho.rhover.common.study.LoaderIssue.IssueLevel;
import com.rho.rhover.common.study.LoaderIssueRepository;
import com.rho.rhover.common.study.DatasetVersionRepository;
import com.rho.rhover.common.study.Field;
import com.rho.rhover.common.study.FieldInstance;
import com.rho.rhover.common.study.FieldInstanceRepository;
import com.rho.rhover.common.study.FieldRepository;
import com.rho.rhover.common.study.FieldService;
import com.rho.rhover.common.study.Site;
import com.rho.rhover.common.study.SiteRepository;
import com.rho.rhover.common.study.Study;
import com.rho.rhover.common.study.StudyDbVersion;
import com.rho.rhover.common.study.StudyDbVersionRepository;
import com.rho.rhover.common.study.StudyRepository;
import com.rho.rhover.common.study.Subject;
import com.rho.rhover.common.study.SubjectRepository;

@Service
public class DataLoaderServiceImpl implements DataLoaderService {
	
	private static final double MIN_COEFFICIENT = 0.5;
	
	private static final double MAX_COEFFICIENT = 0.95;
	
	private static final double MIN_OBSERVATIONS = 10;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private StudyRepository studyRepository;
	
	@Autowired
	private DatasetRepository datasetRepository;
	
	@Autowired
	private StudyDbService studyDbService;
	
	@Autowired
	private DataLocationRepository dataLocationRepository;
	
	@Autowired
	private DatasetVersionRepository datasetVersionRepository;
	
	@Autowired
	private SiteRepository siteRepository;
	
	@Autowired
	private SubjectRepository subjectRepository;
	
	@Autowired
	private StudyDbVersionRepository studyDbVersionRepository;
	
	@Autowired
	private DataStreamRepository dataStreamRepository;
	
	@Autowired
	private FieldRepository fieldRepository;
	
	@Autowired
	private FieldInstanceRepository fieldInstanceRepository;
	
	@Autowired
	private CorrelationService correlationService;
	
	@Autowired
	private LoaderIssueRepository loaderIssueRepository;
	
	@Autowired
	private CsvDataRepository csvDataRepository;
	
	@Autowired
	private FieldService fieldService;

	@Override
	@Transactional
	public boolean updateStudy(Study study) {
		logger.info("Updating study: " + study.getStudyName());
		
		// Check for new and modified files
		Collection<File> modifiedFiles = studyDbService.getModifiedDataFiles(study);
		Collection<File> newFiles = studyDbService.getNewDataFiles(study);
		logger.info("Found " + modifiedFiles.size() + " modified data files in study " + study.getStudyName());
		logger.info("Found " + newFiles.size() + " new data files in study " + study.getStudyName());
		
		// Exit if there are no new or modified files
		if (modifiedFiles.size() == 0 && newFiles.size() == 0) {
			return false;
		}
		
		// Save new study DB version
		StudyDbVersion studyDbVersion = createAndSaveStudyDbVersion(study);
		
		// Create checklist to track which files have been processed
		Map<String, Boolean> fileChecklist = new HashMap<>();
		Set<File> allFiles = studyDbService.getDataFiles(study);
		for (File file : allFiles) {
			fileChecklist.put(file.getAbsolutePath().replace("\\", "/"), Boolean.FALSE);
		}
		
		// Process new files
		for (File file : newFiles) {
			fileChecklist.put(file.getAbsolutePath().replace("\\", "/"), Boolean.TRUE);
			if (study.getQueryFilePath() != null && file.getAbsolutePath().replace("\\", "/").equals(study.getQueryFilePath())) {
				continue;
			}
			Dataset dataset = createAndSaveNewDataset(study, file, studyDbVersion);
			updateDatasetAndStudy(dataset, study, file, studyDbVersion);
		}
		
		// Process modified files
		for (File file : modifiedFiles) {
			fileChecklist.put(file.getAbsolutePath().replace("\\", "/"), Boolean.TRUE);
			if (study.getQueryFilePath() != null && file.getAbsolutePath().replace("\\", "/").equals(study.getQueryFilePath())) {
				continue;
			}
			Dataset dataset = datasetRepository.findByFilePath(file.getAbsolutePath().replace("\\", "/"));
			updateDatasetAndStudy(dataset, study, file, studyDbVersion);
		}
		
		// Add unmodified files to study DB version
		for (String filePath : fileChecklist.keySet()) {
			if (study.getQueryFilePath() != null && filePath.equals(study.getQueryFilePath())) {
				continue;
			}
			if (fileChecklist.get(filePath).equals(Boolean.FALSE)) {
				Dataset dataset = datasetRepository.findByFilePath(filePath);
				DatasetVersion datasetVersion = datasetVersionRepository.findByDatasetAndIsCurrent(dataset, Boolean.TRUE);
				datasetVersion.addStudyDbVersion(studyDbVersion);
				datasetVersionRepository.save(datasetVersion);
				studyDbVersion.addDatasetVersion(datasetVersion);
			}
		}
		study.setIsInitialized(Boolean.TRUE);
		studyDbVersionRepository.save(studyDbVersion);
		return true;
	}

	private StudyDbVersion createAndSaveStudyDbVersion(Study study) {
		StudyDbVersion oldVersion = studyDbVersionRepository.findByStudyAndIsCurrent(study, Boolean.TRUE);
		if (oldVersion != null) {
			oldVersion.setIsCurrent(Boolean.FALSE);
			studyDbVersionRepository.save(oldVersion);
		}
		String studyDbVersionName = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		StudyDbVersion studyDbVersion = new StudyDbVersion(studyDbVersionName, study, Boolean.TRUE);
		logger.info("Saving new study DB version: " + studyDbVersionName);
		studyDbVersionRepository.save(studyDbVersion);
		return studyDbVersion;
	}

	private Dataset createAndSaveNewDataset(Study study, File file, StudyDbVersion studyDbVersion) {		
		DataLocation dataLocation = dataLocationRepository.findByFolderPath(file.getParentFile().getAbsolutePath().replace("\\", "/"));
		Dataset dataset = new Dataset(file.getName(), study, file.getAbsolutePath().replace("\\", "/"), dataLocation);
		logger.info("Saving new dataset " + file.getName());
		datasetRepository.save(dataset);
		return dataset;
	}
	
	
	// TODO: If there is a SAS read error, do not create dataset version
	private void updateDatasetAndStudy(Dataset dataset, Study study, File file, StudyDbVersion studyDbVersion) {
		
		// Save new dataset version
		DatasetVersion datasetVersion = createAndSaveNewDatasetVersion(file, dataset, studyDbVersion);
		
		try {
			DataFrame df = DataFrame.extractSasData(file);
			datasetVersion.setNumRecords(df.numRecords());
			datasetVersionRepository.save(datasetVersion);
			
			// Add data streams and fields to dataset version
			addDataStreams(datasetVersion, df, file);
			addFields(datasetVersion, df, file);
			
			// Add new sites and subjects to study
			addAnyNewSubjectsAndSites(study, df, file);
			studyRepository.save(study);
		}
		catch (SourceDataException e) {
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);
			e.getCause().printStackTrace(printWriter);
			String stackTrace = stringWriter.toString();
			logger.error(stackTrace);
			LoaderIssue issue = new LoaderIssue(e.getMessage(), stackTrace, IssueLevel.DATASET_VERSION);
			issue.setDatasetVersion(datasetVersion);
			loaderIssueRepository.save(issue);
		}
	}
	
	private DatasetVersion createAndSaveNewDatasetVersion(File file, Dataset dataset, StudyDbVersion studyDbVersion) {
		String datasetVersionName = studyDbService.generateDatasetVersionName(file);
		DatasetVersion oldVersion = datasetVersionRepository.findByDatasetAndIsCurrent(dataset, Boolean.TRUE);
		if (oldVersion != null) {
			oldVersion.setIsCurrent(Boolean.FALSE);
			datasetVersionRepository.save(oldVersion);
		}
		DatasetVersion datasetVersion = new DatasetVersion(datasetVersionName, Boolean.TRUE, dataset, 0);
		datasetVersion.addStudyDbVersion(studyDbVersion);
		datasetVersionRepository.save(datasetVersion);
		studyDbVersion.addDatasetVersion(datasetVersion);
		studyDbVersionRepository.save(studyDbVersion);
		return datasetVersion;
	}
	
	private void addDataStreams(DatasetVersion datasetVersion, DataFrame df, File file) {
		logger.debug("Adding data streams");
		
		// Make sure file contains a field indicating data stream
		List<String> fields = df.getColNames();
		Study study = datasetVersion.getDataset().getStudy();
		if (!fields.contains(study.getFormFieldName())) {
			String message = "File " + file.getName() + " missing data stream field name";
			throw new SourceDataException(message);
		}
	
		// Add new data streams
		Set<String> dataStreamNames = df.getUniqueValues(study.getFormFieldName());
		logger.debug("Found " + dataStreamNames.size() + " data streams");
		for (String dataStreamName : dataStreamNames) {
			DataStream dataStream = dataStreamRepository.findByStudyAndDataStreamName(study, dataStreamName);
			if (dataStream == null) {
				logger.info("Saving new data stream: " + dataStreamName);
				dataStream = new DataStream(dataStreamName, study);
				dataStreamRepository.save(dataStream);
			}
			datasetVersion.addDataStream(dataStream);
			dataStream.addDatasetVersion(datasetVersion);
			dataStreamRepository.save(dataStream);
		}
		
		datasetVersionRepository.save(datasetVersion);
	}
	
	private void addFields(DatasetVersion datasetVersion, DataFrame df, File file) {
		List<String> fields = df.getColNames();
		List<String> labels = df.getColLabels();
		List<Class> dataTypes = df.getDataTypes();
		Study study = datasetVersion.getDataset().getStudy();
		for (int i = 0; i < fields.size(); i++) {
			String fieldName = fields.get(i);
			String fieldLabel = labels.get(i);
			String colType = dataTypes.get(i).getSimpleName();
			Field field = fieldRepository.findByStudyAndFieldName(study, fieldName);
			if (field == null) {
				logger.info("Saving field " + fieldName);
				field = new Field(fieldName, fieldLabel, study, colType);
				fieldRepository.save(field);
			}
			else {
				String fieldType = field.getDataType();
				if (!fieldType.equals(colType) && !colType.equals("UnknownType")) {
					String conversionType = null;
					if (fieldType.equals("UnknownType") || fieldType.equals("Boolean")) {
						conversionType = colType;
					}
					if (fieldType.equals("Integer") || fieldType.equals("Double")) {
						if (colType.equals("String") || colType.equals("MixedType") || colType.equals("Date")) {
							conversionType = "MixedType";
						}
						if (fieldType.equals("Integer") && colType.equals("Double")) {
							conversionType = colType;
						}
					}
					if (fieldType.equals("String") || fieldType.equals("Date")) {
						conversionType = "MixedType";
					}
					if (conversionType != null) {
						logger.info("Converting field " + fieldName + " from type " + fieldType + " to " + conversionType);
						field.setDataType(conversionType);
						fieldRepository.save(field);
					}
				}
			}
			datasetVersion.addField(field);
			field.addDatasetVersion(datasetVersion);
			fieldRepository.save(field);
			CsvData csvData = csvDataRepository.findByFieldAndDataset(field, datasetVersion.getDataset());
			if (csvData == null) {
				csvData = new CsvData();
				csvData.setField(field);
				csvData.setDataset(datasetVersion.getDataset());
			}
			csvData.setData(df.getFieldAsCsv(fieldName));
			csvDataRepository.save(csvData);
			
			// Field instance
			FieldInstance fieldInstance = fieldInstanceRepository.findByFieldAndDataset(field, datasetVersion.getDataset());
			if (fieldInstance == null) {
				fieldInstance = new FieldInstance(field, datasetVersion.getDataset());
				fieldInstanceRepository.save(fieldInstance);
			}
		}
		datasetVersionRepository.save(datasetVersion);
	}

	private void addAnyNewSubjectsAndSites(Study study, DataFrame df, File file) {
		
		// Make sure data contains site and subject fields
		// TODO: Perform these checks early in process
		if (!df.getColNames().contains(study.getSiteFieldName())) {
			String message = "File " + file.getName() + " does not contain site field " + study.getSiteFieldName();
			throw new SourceDataException(message);
		}
		if (!df.getColNames().contains(study.getSubjectFieldName())) {
			String message = "File " + file.getName() + " does not contain subject field " + study.getSubjectFieldName();
			throw new SourceDataException(message);
		}
		
		// Create a map of site names to sites
		Map<String, Site> siteMap = new HashMap<>();
		Set<String> siteNames = df.getUniqueValues(study.getSiteFieldName());
		for (Object siteName : siteNames) {
			Site site = siteRepository.findByStudyAndSiteName(study, siteName.toString());
			if (site == null) {
				logger.info("Saving new site: " + siteName.toString());
				site = new Site(siteName.toString(), study);
				siteRepository.save(site);
			}
			siteMap.put(siteName.toString(), site);
		}
		
		// Create a map of subject names to site names
		Set<String> subjectNames = df.getUniqueValues(study.getSubjectFieldName());
		Map<String, String> siteNameMap = generateSubjectNameToSiteNameMapping(study, df);
		
		// Process subjects
		for (Object subjectName : subjectNames) {
			Subject subject = subjectRepository.findBySubjectName(subjectName.toString());
			if (subject == null) {
				//logger.info("Saving new subject: " + subjectName.toString());
				String siteName = siteNameMap.get(subjectName.toString());
				if (siteName == null) {
					logger.warn("Subject " + subjectName + " does not have an associated site in file " + file.getAbsolutePath() + ".  Not saving.");
					
					// TODO: Add user notification call
				}
				else {
					Site site = siteMap.get(siteName);
					subject = new Subject(subjectName.toString(), site);
					subjectRepository.save(subject);
				}
			}
		}
	}
	
	private Map<String, String> generateSubjectNameToSiteNameMapping(Study study, DataFrame df) {
		Map<String, String> map = new HashMap<>();
		List<String> subjectNames = df.getField(study.getSubjectFieldName());
		List<String> siteNames = df.getField(study.getSiteFieldName());
		int n = subjectNames.size();
		for (int i = 0; i < n; i++) {
			map.put(subjectNames.get(i).toString(), siteNames.get(i).toString());
		}
		return map;
	}
	
	@Override
	@Transactional
	public void calculateAndSaveCorrelations(Study study) {
		List<Dataset> datasets = datasetRepository.findByStudy(study);
		List<Field> commonFields = fieldService.findPotentiallyIdentiableFields(study);
		for (int i = 0; i < datasets.size(); i++) {
			DatasetVersion datasetVersion1 = datasetVersionRepository.findByDatasetAndIsCurrent(datasets.get(i), Boolean.TRUE);
			calculateAndSaveCorrelations(datasetVersion1, commonFields);
			for (int j = i + 1; j < datasets.size(); j++) {
				DatasetVersion datasetVersion2 = datasetVersionRepository.findByDatasetAndIsCurrent(datasets.get(j), Boolean.TRUE);
				calculateAndSaveCorrelations(study, datasetVersion1, datasetVersion2, commonFields);
			}
		}
	}
	
	private void calculateAndSaveCorrelations(DatasetVersion datasetVersion, List<Field> commonFields) {
		Dataset dataset = datasetVersion.getDataset();
		//logger.debug("Calculating correlation for dataset " + dataset.getDatasetName());
		List<Field> fields = new ArrayList<>();
		fields.addAll(datasetVersion.getFields());
		for (int i = 0; i < fields.size(); i++) {
			Field field1 = fields.get(i);
			if (field1.getDataType().equals("Double")) {
				if (commonFields.contains(field1)) {
					continue;
				}
				CsvData csvData1 = csvDataRepository.findByFieldAndDataset(field1, dataset);
				for (int j = i + 1; j < fields.size(); j++) {
					Field field2 = fields.get(j);
					if (field2.getDataType().equals("Double")) {
						if (commonFields.contains(field2)) {
							continue;
						}
						//logger.debug("Calculating correlation for " + field1.getDisplayName() + " and " + field2.getDisplayName());
						CsvData csvData2 = csvDataRepository.findByFieldAndDataset(field2, dataset);
						double coefficient = calculateCorrelation(csvData1, csvData2);
						//logger.debug("Coefficient: " + coefficient);
						double absCoefficient = Math.abs(coefficient);
						if (absCoefficient >= MIN_COEFFICIENT && absCoefficient <= MAX_COEFFICIENT) {
							logger.debug("Coefficient for " + dataset.getDatasetName() + " " + field1.getDisplayName() +
									" and " + dataset.getDatasetName() + " " + field2.getDisplayName() + ": " + coefficient);
							saveCorrelation(field1, dataset, field2, dataset, coefficient);
						}
					}
				}
			}
		}
	}
	
	private void calculateAndSaveCorrelations(Study study, DatasetVersion datasetVersion1, DatasetVersion datasetVersion2,
			List<Field> commonFields) {
		Dataset dataset1 = datasetVersion1.getDataset();
		Dataset dataset2 = datasetVersion2.getDataset();
		//logger.debug("Calculating correlation for datasets " + dataset1.getDatasetName() + " and " + dataset2.getDatasetName());
		Field subjectField = fieldRepository.findByStudyAndFieldName(study, study.getSubjectFieldName());
		CsvData subjects1 = csvDataRepository.findByFieldAndDataset(subjectField, dataset1);
		CsvData subjects2 = csvDataRepository.findByFieldAndDataset(subjectField, dataset2);
		for (Field field1 : datasetVersion1.getFields()) {
			if (field1.getDataType().equals("Double")) {
				if (commonFields.contains(field1)) {
					continue;
				}
				CsvData csvData1 = csvDataRepository.findByFieldAndDataset(field1, dataset1);
				for (Field field2 : datasetVersion2.getFields()) {
					if (field2.getDataType().equals("Double")) {
						if (commonFields.contains(field2)) {
							continue;
						}
						//logger.debug("Calculating correlation for " + field1.getDisplayName() + " and " + field2.getDisplayName());
						CsvData csvData2 = csvDataRepository.findByFieldAndDataset(field2, dataset2);
						double coefficient = calculateCorrelation(csvData1, subjects1, csvData2, subjects2);
						//logger.debug("Coefficient: " + coefficient);
						double absCoefficient = Math.abs(coefficient);
						if (absCoefficient >= MIN_COEFFICIENT && absCoefficient <= MAX_COEFFICIENT) {
							logger.debug("Coefficient for " + dataset1.getDatasetName() + " " + field1.getDisplayName() +
									" and " + dataset2.getDatasetName() + " " + field2.getDisplayName() + ": " + coefficient);
							saveCorrelation(field1, dataset1, field2, dataset2, coefficient);
						}
					}
				}
			}
		}
	}
	
	private double calculateCorrelation(CsvData csvData1, CsvData csvData2) {
		List<Double> raw1 = csvData1.extractDataAsDouble();
		List<Double> raw2 = csvData2.extractDataAsDouble();
		return calculateCorrelation(raw1, raw2);
	}
	
	private double calculateCorrelation(CsvData data1, CsvData subjects1, CsvData data2, CsvData subjects2) {
		List<Double> values1 = data1.extractDataAsDouble();
		List<String> subDat1 = subjects1.extractData();
		List<Double> values2 = data2.extractDataAsDouble();
		List<String> subDat2 = subjects2.extractData();
		List<Double> raw1 = new ArrayList<>();
		List<Double> raw2 = new ArrayList<>();
		Map<String, Double> index = new HashMap<>();
		for (int i = 0; i < values1.size(); i++) {
			Double value = values1.get(i);
			if (!Double.isNaN(value)) {
				String key = subDat1.get(i);
				index.put(key, value);
			}
		}
		for (int i = 0; i < values2.size(); i++) {
			String key = subDat2.get(i);
			if (index.containsKey(key)) {
				Double value = values2.get(i);
				if (!Double.isNaN(value)) {
					raw1.add(index.get(key));
					raw2.add(value);
				}
			}
		}
		return calculateCorrelation(raw1, raw2);
	}
	
	private double calculateCorrelation(List<Double> raw1, List<Double> raw2) {
		List<Double> processed1 = new ArrayList<>();
		List<Double> processed2 = new ArrayList<>();
		for (int i = 0; i < raw1.size(); i++) {
			Double val1 = raw1.get(i);
			Double val2 = raw2.get(i);
			if (Double.isNaN(val1) || Double.isNaN(val2)) {
				continue;
			}
			processed1.add(val1);
			processed2.add(val2);
		}
		if (processed1.size() < MIN_OBSERVATIONS) {
			return 0;
		}
		double[] data1 = new double[processed1.size()];
		double[] data2 = new double[processed2.size()];
		for (int i = 0; i < processed1.size(); i++) {
			data1[i] = processed1.get(i);
		}
		for (int i = 0; i < processed2.size(); i++) {
			data2[i] = processed2.get(i);
		}
		double coefficient = 0;
		try {
			coefficient = new PearsonsCorrelation().correlation(data1, data2);
		}
		catch (Exception e) {
			logger.warn("Exception encountered calculating correlation coefficient: " + e.getMessage());
		}
		if (Double.isNaN(coefficient)) {
			coefficient = 0.0;
		}
		return coefficient;
	}
	
	private void saveCorrelation(Field field1, Dataset dataset1, Field field2, Dataset dataset2, double coeff) {
		if (Double.isNaN(coeff)) {
			return;
		}
		FieldInstance fieldInstance1 = fieldInstanceRepository.findByFieldAndDataset(field1, dataset1);
		FieldInstance fieldInstance2 = fieldInstanceRepository.findByFieldAndDataset(field2, dataset2);
		Correlation correlation = correlationService.getCorrelationWithAnyFieldOrder(fieldInstance1, fieldInstance2);
		if (correlation == null) {
			correlation = new Correlation(fieldInstance1, fieldInstance2, coeff);
		}
		else {
			correlation.setCoefficient(coeff);
		}
		correlationService.save(correlation);
	}
}
