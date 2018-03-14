package com.rho.rhover.daemon;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;
import javax.transaction.Transactional;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.rho.rhover.common.anomaly.Datum;
import com.rho.rhover.common.anomaly.DatumChange;
import com.rho.rhover.common.anomaly.DatumChangeRepository;
import com.rho.rhover.common.anomaly.DatumRepository;
import com.rho.rhover.common.anomaly.DatumVersion;
import com.rho.rhover.common.anomaly.DatumVersionRepository;
import com.rho.rhover.common.anomaly.Observation;
import com.rho.rhover.common.anomaly.ObservationRepository;
import com.rho.rhover.common.check.Correlation;
import com.rho.rhover.common.check.CorrelationService;
import com.rho.rhover.common.study.CsvData;
import com.rho.rhover.common.study.CsvDataRepository;
import com.rho.rhover.common.study.DataLocation;
import com.rho.rhover.common.study.DataLocationService;
import com.rho.rhover.common.study.DataStream;
import com.rho.rhover.common.study.DataStreamRepository;
import com.rho.rhover.common.study.Dataset;
import com.rho.rhover.common.study.DatasetModification;
import com.rho.rhover.common.study.DatasetModificationRepository;
import com.rho.rhover.common.study.DatasetRepository;
import com.rho.rhover.common.study.DatasetVersion;
import com.rho.rhover.common.study.LoaderIssue;
import com.rho.rhover.common.study.LoaderIssue.IssueLevel;
import com.rho.rhover.common.study.LoaderIssueRepository;
import com.rho.rhover.common.study.Phase;
import com.rho.rhover.common.study.PhaseRepository;
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
	private PhaseRepository phaseRepository;
	
	@Autowired
	private FieldService fieldService;
	
	@Autowired
	private DataLocationService dataLocationService;
	
	@Autowired
	private ObservationRepository observationRepository;
	
	@Autowired
	private DatumRepository datumRepository;
	
	@Autowired
	private DatumVersionRepository datumVersionRepository;
	
	@Autowired
	private DatasetModificationRepository datasetModificationRepository;
	
	@Autowired
	private DatumChangeRepository datumChangeRepository;
	
	@Override
	@Transactional
	public void updateStudy(Study study) {
		logger.info("Updating study: " + study.getStudyName());
		
		// Check for new and modified files
		Collection<File> modifiedFiles = studyDbService.getModifiedDataFiles(study);
		Collection<File> newFiles = studyDbService.getNewDataFiles(study);
		logger.info("Found " + modifiedFiles.size() + " modified data files in study " + study.getStudyName());
		logger.info("Found " + newFiles.size() + " new data files in study " + study.getStudyName());
		
		// Exit if there are no new or modified files
		if (modifiedFiles.size() == 0 && newFiles.size() == 0) {
			return;
		}
		
		// Save new study DB version
		StudyDbVersion studyDbVersion = createAndSaveStudyDbVersion(study);
		
		// Create checklist to track which files have been processed
		Map<String, Boolean> fileChecklist = new HashMap<>();
		Set<File> allFiles = studyDbService.getDataFiles(study);
		for (File file : allFiles) {
			fileChecklist.put(file.getAbsolutePath().replaceAll("\\\\", "/"), Boolean.FALSE);
		}
		
		// Process new files
		for (File file : newFiles) {
			fileChecklist.put(file.getAbsolutePath(), Boolean.TRUE);
			if (study.getQueryFilePath() != null && file.getAbsolutePath().equals(study.getQueryFilePath())) {
				continue;
			}
			Dataset dataset = createAndSaveNewDataset(study, file, studyDbVersion);
			updateDatasetAndStudy(dataset, study, file, studyDbVersion);
		}
		
		// Process modified files
		for (File file : modifiedFiles) {
			fileChecklist.put(file.getAbsolutePath(), Boolean.TRUE);
			if (study.getQueryFilePath() != null && file.getAbsolutePath().equals(study.getQueryFilePath())) {
				continue;
			}
			Dataset dataset = datasetRepository.findByFilePath(file.getAbsolutePath().replaceAll("\\\\", "/"));
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
		Date date = new Date();
		studyDbVersion.setLoadStopped(new Timestamp(date.getTime()));
		studyDbVersionRepository.save(studyDbVersion);
	}

	private StudyDbVersion createAndSaveStudyDbVersion(Study study) {
		StudyDbVersion oldVersion = studyDbVersionRepository.findByStudyAndIsCurrent(study, Boolean.TRUE);
		if (oldVersion != null) {
			oldVersion.setIsCurrent(Boolean.FALSE);
			studyDbVersionRepository.save(oldVersion);
		}
		String studyDbVersionName = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		StudyDbVersion studyDbVersion = new StudyDbVersion(studyDbVersionName, study, Boolean.TRUE);
		Date date = new Date();
		studyDbVersion.setLoadStarted(new Timestamp(date.getTime()));
		logger.info("Saving new study DB version: " + studyDbVersionName);
		studyDbVersionRepository.save(studyDbVersion);
		return studyDbVersion;
	}

	private Dataset createAndSaveNewDataset(Study study, File file, StudyDbVersion studyDbVersion) {		
		DataLocation dataLocation = dataLocationService.findByDirectory(file.getParentFile());
		Dataset dataset = new Dataset(file.getName(), study, file.getAbsolutePath(), dataLocation);
		logger.info("Saving new dataset " + dataset.getDatasetName() + ": " + dataset.getFilePath());
		datasetRepository.save(dataset);
		return dataset;
	}
	
	
	// TODO: If there is a SAS read error, do not create dataset version
	private void updateDatasetAndStudy(Dataset dataset, Study study, File file, StudyDbVersion studyDbVersion) {
		
		// Save new dataset version
		DatasetVersion datasetVersion = createAndSaveNewDatasetVersion(file, dataset, studyDbVersion);
		
		try {
			DataFrame df = null;
			if (file.getName().endsWith(".sas7bdat")) {
				df = DataFrame.extractSasData(file);
			}
			else if (file.getName().endsWith(".csv")) {
				df = DataFrame.extractCsvData(file);
			}
			else {
				throw new RuntimeException("Unsupported file type: " + file.getName());
			}
			datasetVersion.setNumRecords(df.numRecords());
			datasetVersionRepository.save(datasetVersion);
			
			// Add any new data stream and phases
			addDataStreams(datasetVersion, df, file);
			addPhases(df, study, file, datasetVersion);
			
			// Add any new fields
			boolean missingAnIdField = addFields(datasetVersion, df, file);
			
			// Add new sites and subjects to study
			addAnyNewSubjectsAndSites(study, df, file);
			studyRepository.save(study);
			
			if (!missingAnIdField) {
				
				// Find splitter and splittee fields, if any
				boolean multipleRecs = hasMultipleRecsPerEncounter(df, study);
				if (multipleRecs && !datasetVersion.getHasMultipleRecsPerEncounter()) {
					datasetVersion.setHasMultipleRecsPerEncounter(multipleRecs);
					datasetVersionRepository.save(datasetVersion);
					findPotentialSplittersAndSplittees(datasetVersion, df);
				}
				
				// Save data
				saveData(df, datasetVersion);
			}
		}
		catch (SourceDataException e) {
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);
			e.printStackTrace(printWriter);
			if (e.getCause() != null) {
				printWriter.append("\n\nCaused by:");
				e.getCause().printStackTrace(printWriter);
			}
			String stackTrace = stringWriter.toString();
			logger.error(stackTrace);
			LoaderIssue issue = new LoaderIssue(e.getMessage(), stackTrace, IssueLevel.DATASET_VERSION);
			issue.setDatasetVersion(datasetVersion);
			loaderIssueRepository.save(issue);
		}
	}
	
	private void saveData(DataFrame df, DatasetVersion datasetVersion) {
		Study study = datasetVersion.getDataset().getStudy();
		Dataset dataset = datasetVersion.getDataset();
		for (String fieldName : df.getColNames()) {
			if (fieldName.equals(study.getSubjectFieldName())
					|| fieldName.equals(study.getSiteFieldName())
					|| fieldName.equals(study.getPhaseFieldName())
					|| fieldName.equals(study.getRecordIdFieldName())
					|| fieldName.equals(study.getFormFieldName())) {
				continue;
			}
			Iterator<String> subjectNames = df.getField(study.getSubjectFieldName()).iterator();
			Iterator<String> siteNames = df.getField(study.getSiteFieldName()).iterator();
			Iterator<String> phaseNames = df.getField(study.getPhaseFieldName()).iterator();
			Iterator<String> recordIds = df.getField(study.getRecordIdFieldName()).iterator();
			Iterator<String> data = df.getField(fieldName).iterator();
			Field field = fieldRepository.findByStudyAndFieldName(study, fieldName);
			while (subjectNames.hasNext() && siteNames.hasNext() && phaseNames.hasNext() && recordIds.hasNext() && data.hasNext()) {
				
				// Fetch or create new observation
				Subject subject = subjectRepository.findBySubjectNameAndStudy(subjectNames.next(), study);
				Site site = siteRepository.findByStudyAndSiteName(study, siteNames.next());
				Phase phase = phaseRepository.findByPhaseNameAndStudy(phaseNames.next(), study);
				String recordId = recordIds.next();
				Observation observation = observationRepository
						.findByDatasetAndSubjectAndPhaseAndSiteAndRecordId(
								dataset, subject, phase, site, recordId);
				if (observation == null) {
					observation = new Observation(dataset, subject, site, phase, recordId);
					observation.setFirstDatasetVersion(datasetVersion);
					observationRepository.save(observation);
				}
			
				// Fetch or create new datum
				String value = data.next();
				if (value != null) {
					Datum datum = datumRepository.findByObservationAndField(observation, field);
					if (datum == null) {
						datum = new Datum(field, observation);
						datum.setFirstDatasetVersion(datasetVersion);
						datumRepository.save(datum);
					}
					
					// Fetch or create new datum version
					DatumVersion datumVersion = datumVersionRepository.findByDatumAndIsCurrent(datum, Boolean.TRUE);
					if (datumVersion == null) {
						datumVersion = new DatumVersion(value, Boolean.TRUE, datum);
						datumVersion.setFirstDatasetVersion(datasetVersion);
					}
					else if (!(datumVersion.getValue().equals(value))) {
						DatumChange datumChange = new DatumChange();
						datumChange.setDatasetVersion(datasetVersion);
						datumChange.setOldDatumVersion(datumVersion);
						datumVersion.setIsCurrent(Boolean.FALSE);
						datumVersionRepository.save(datumVersion);
						datumVersion = new DatumVersion(value, Boolean.TRUE, datum);
						datumVersion.setFirstDatasetVersion(datasetVersion);
						datumVersionRepository.save(datumVersion);
						datumChange.setNewDatumVersion(datumVersion);
						datumChangeRepository.save(datumChange);
					}
					datumVersion.getDatasetVersions().add(datasetVersion);
					datumVersionRepository.save(datumVersion);
				}
			}
		}
	}
	
	private void findPotentialSplittersAndSplittees(DatasetVersion datasetVersion, DataFrame df) {
		logger.debug("Looking for splitters and splittees in " + datasetVersion.getDataset().getDatasetName());
		
		// Create clusters of records by subject ID and phase
		// Keys: SUBJECTID---PHASE
		// Values: List of record indices in data frame
		Map<String, List<Integer>> clusters = new HashMap<>();
		Study study = datasetVersion.getDataset().getStudy();
		String subjectFieldName = study.getSubjectFieldName();
		String phaseFieldName = study.getPhaseFieldName();
		List<String> subjects = df.getField(subjectFieldName);
		List<String> phases = df.getField(phaseFieldName);
		Iterator<String> subjectIt = subjects.iterator();
		Iterator<String> phaseIt = phases.iterator();
		int rowNum = 0;
		while (subjectIt.hasNext() && phaseIt.hasNext()) {
			String key = subjectIt.next() + "---" + phaseIt.next();
			List<Integer> indices = clusters.get(key);
			if (indices == null) {
				indices = new ArrayList<>();
				clusters.put(key, indices);
			}
			indices.add(rowNum);
			rowNum++;
		}
		
		// Find potential splitters and splitees by identifying columns where different
		// members of a cluster have different values
		int colNum = -1;
		for (String colName : df.getColNames()) {
			colNum++;
			logger.debug("Col: " + colName + ", dataType: " + df.getDataTypes().get(colNum));
			Class dataType = df.getDataTypes().get(colNum);
			if (study.isFieldIdentifying(colName)
					|| !(dataType.equals(String.class) || dataType.equals(Double.class))) {
				continue;
			}
			List<String> values = df.getField(colName);
			int numClustersWithDiffValues = 0;
			for (String key : clusters.keySet()) {
				Set<String> clusterValues = new HashSet<>();
				List<Integer> indices = clusters.get(key);
				for (Integer index : indices) {
					clusterValues.add(values.get(index));
				}
				if (clusterValues.size() > 1 && indices.size() > 1) {
					numClustersWithDiffValues++;
				}
//				logger.debug("dataset: " + datasetVersion.getDataset().getDatasetName()
//						+ ", field: " + colName
//						+ ", cluster: " + key + ", num records: " + indices.size()
//						+ ", num diff values: " + clusterValues.size());
			}
			int threshold = (int)(clusters.size() * 0.25);
			logger.debug("Num clusters with diff values: " + numClustersWithDiffValues);
			logger.debug("Num records: " + df.numRecords());
			logger.debug("Num clusters: " + clusters.size());
			logger.debug("Threshold: " + threshold);
			if (numClustersWithDiffValues >= threshold) {
				Field field = fieldRepository.findByStudyAndFieldName(study, colName);
				FieldInstance fi = fieldInstanceRepository.findByFieldAndDataset(
						field, datasetVersion.getDataset());
				if (dataType.equals(String.class) && df.getUniqueValues(colName).size() <= 20) {
					logger.debug("Potential splitter");
					fi.setIsPotentialSplitter(Boolean.TRUE);
				}
				else if (dataType.equals(Double.class)) {
					logger.debug("Potential splittee");
					fi.setIsPotentialSplittee(Boolean.TRUE);
				}
				fieldInstanceRepository.save(fi);
			}
		}
		
	}

	private Boolean hasMultipleRecsPerEncounter(DataFrame df, Study study) {
		boolean hasMultiples = false;
		Set<String> keys = new HashSet<>();
		Iterator<String> subjectIterator = df.getField(study.getSubjectFieldName()).iterator();
		Iterator<String> phaseIterator = df.getField(study.getPhaseFieldName()).iterator();
		while (subjectIterator.hasNext() && phaseIterator.hasNext()) {
			String key = subjectIterator.next() + "---" + phaseIterator.next();
			if (keys.contains(key)) {
				hasMultiples = true;
				break;
			}
			keys.add(key);
		}
		return hasMultiples;
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
		DatasetModification datasetMod = new DatasetModification();
		datasetMod.setDataset(dataset);
		datasetMod.setStudyDbVersion(studyDbVersion);
		if (oldVersion == null) {
			datasetMod.setIsNew(Boolean.TRUE);
			datasetMod.setIsModified(Boolean.FALSE);
		}
		else {
			datasetMod.setIsNew(Boolean.FALSE);
			datasetMod.setIsModified(Boolean.TRUE);
		}
		datasetModificationRepository.save(datasetMod);
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
			logger.warn("File " + file.getName() + " missing data stream field name");
			return;
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
	
	private void addPhases(DataFrame df, Study study, File file, DatasetVersion datasetVersion) {
		
		// Make sure file contains a field indicating phase
		List<String> fields = df.getColNames();
		if (!fields.contains(study.getPhaseFieldName())) {
			throw new SourceDataException("File " + file.getName() + " missing data stream field name");
		}
		
		// Extract phase names and save any new phases
		List<String> phaseData = df.getField(study.getPhaseFieldName());
		for (String phaseName : phaseData) {
			Phase phase = phaseRepository.findByPhaseNameAndStudy(phaseName, study);
			if (phase == null) {
				logger.debug("Saving new phase: " + phaseName);
				phase = new Phase(phaseName, study);
				phaseRepository.save(phase);
			}
			datasetVersion.getPhases().add(phase);
		}
		datasetVersionRepository.save(datasetVersion);
	}
	
	private boolean addFields(DatasetVersion datasetVersion, DataFrame df, File file) {
		List<String> fields = df.getColNames();
		List<String> labels = df.getColLabels();
		List<Class> dataTypes = df.getDataTypes();
		Study study = datasetVersion.getDataset().getStudy();
		boolean hasSubjectField = false;
		boolean hasPhaseField = false;
		boolean hasRecordIdField = false;
		boolean hasSiteField = false;
		for (int i = 0; i < fields.size(); i++) {
			String fieldName = fields.get(i);
			String fieldLabel = labels.get(i);
			String colType = dataTypes.get(i).getSimpleName();
			Field field = fieldRepository.findByStudyAndFieldName(study, fieldName);
			if (field == null) {
				logger.debug("Saving field " + fieldName);
				field = new Field(fieldName, fieldLabel, study, colType);
				fieldRepository.save(field);
				if (fieldName.equals(study.getFormFieldName())) {
					study.setFormField(field);
					studyRepository.save(study);
				}
				else if (fieldName.equals(study.getSiteFieldName())) {
					study.setSiteField(field);
					studyRepository.save(study);
				}
				else if (fieldName.equals(study.getSubjectFieldName())) {
					study.setSubjectField(field);
					studyRepository.save(study);
				}
				else if (fieldName.equals(study.getPhaseFieldName())) {
					study.setPhaseField(field);
					studyRepository.save(study);
				}
				else if (fieldName.equals(study.getRecordIdFieldName())) {
					study.setRecordIdField(field);
					studyRepository.save(study);
				}
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
			if (fieldName.equals(study.getSubjectFieldName())) {
				hasSubjectField = true;
			}
			if (fieldName.equals(study.getPhaseFieldName())) {
				hasPhaseField = true;
			}
			if (fieldName.equals(study.getRecordIdFieldName())) {
				hasRecordIdField = true;
			}
			if (fieldName.equals(study.getSiteFieldName())) {
				hasSiteField = true;
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
				fieldInstance.setFirstDatasetVersion(datasetVersion);
				fieldInstanceRepository.save(fieldInstance);
			}
		}
		if (hasSubjectField && hasPhaseField && hasRecordIdField && hasSiteField) {
			datasetVersion.setIsMissingAnIdField(Boolean.FALSE);
		}
		else {
			datasetVersion.setIsMissingAnIdField(Boolean.TRUE);
		}
		datasetVersionRepository.save(datasetVersion);
		
		return datasetVersion.getIsMissingAnIdField();
	}

	private void addAnyNewSubjectsAndSites(Study study, DataFrame df, File file) {
		
		boolean hasSubjectField = df.getColNames().contains(study.getSubjectFieldName());
		boolean hasSiteField = df.getColNames().contains(study.getSiteFieldName());
				
		// Create a map of site names to sites while saving any new sites
		Map<String, Site> siteMap = new HashMap<>();
		if (hasSiteField) {
			Set<String> siteNames = df.getUniqueValues(study.getSiteFieldName());
			for (String siteName : siteNames) {
				Site site = siteRepository.findByStudyAndSiteName(study, siteName);
				if (site == null) {
					logger.info("Saving new site: " + siteName);
					site = new Site(siteName, study);
					siteRepository.save(site);
				}
				siteMap.put(siteName, site);
			}
		}
		
		if (hasSubjectField) {
		
			// Create a map of subject names to site names
			Set<String> subjectNames = df.getUniqueValues(study.getSubjectFieldName());
			Map<String, String> subjectNameToSiteName = new HashMap<>();
			if (hasSiteField) {
				subjectNameToSiteName = generateSubjectNameToSiteNameMapping(study, df);
			}
			
			// Process subjects
			for (String subjectName : subjectNames) {
				Subject subject = subjectRepository.findBySubjectNameAndStudy(subjectName, study);
				
				// Case: new subject
				if (subject == null) {
					//logger.info("Saving new subject: " + subjectName.toString());
					subject = new Subject();
					subject.setSubjectName(subjectName.toString());
					if (hasSiteField) {
						String siteName = subjectNameToSiteName.get(subjectName);
						if (siteName != null) {
							Site site = siteMap.get(siteName);
							subject.setSite(site);
						}
					}
					subjectRepository.save(subject);
				}
				
				// Case: add site to subject
				else if (subject.getSite() == null && hasSiteField) {
					String siteName = subjectNameToSiteName.get(subjectName);
					if (siteName != null) {
						Site site = siteMap.get(siteName);
						subject.setSite(site);
						subjectRepository.save(subject);
					}
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
							saveCorrelation(dataset.getStudy(), field1, dataset, field2, dataset, coefficient);
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
		Field subjectField = study.getSubjectField();
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
							saveCorrelation(study, field1, dataset1, field2, dataset2, coefficient);
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
	
	private void saveCorrelation(Study study, Field field1, Dataset dataset1, Field field2, Dataset dataset2, double coeff) {
		if (Double.isNaN(coeff)) {
			return;
		}
		FieldInstance fieldInstance1 = fieldInstanceRepository.findByFieldAndDataset(field1, dataset1);
		FieldInstance fieldInstance2 = fieldInstanceRepository.findByFieldAndDataset(field2, dataset2);
		Correlation correlation = correlationService.getCorrelationWithAnyFieldOrder(fieldInstance1, fieldInstance2);
		if (correlation == null) {
			correlation = new Correlation(study, fieldInstance1, fieldInstance2, coeff);
		}
		else {
			correlation.setCoefficient(coeff);
		}
		correlationService.save(correlation);
	}
}
