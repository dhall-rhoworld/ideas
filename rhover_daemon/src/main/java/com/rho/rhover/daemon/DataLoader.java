package com.rho.rhover.daemon;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import com.rho.rhover.common.study.Study;
import com.rho.rhover.common.util.IOUtils;

public class DataLoader {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private Study study;
	
	private File workingDir;
	
	private File tempDir;
	
	private JdbcTemplate jdbcTemplate;
	
	private StudyDbService studyDbService;
	
	private Long studyDbVersionId;
	
	private Map<String, Long> primaryKeyIndex = new HashMap<>();
	
	private Map<String, Long> siteIndex = new HashMap<>();
	
	private Map<String, Long> subjectIndex = new HashMap<>();
	
	private Map<String, Long> phaseIndex = new HashMap<>();
	
	private Map<String, Long> dataStreamIndex = new HashMap<>();
	
	private Map<String, Long> fieldIndex = new HashMap<>();
	
	private BufferedWriter observationWriter = null;
	
	private BufferedWriter datumWriter = null;
	
	private BufferedWriter datumVersionWriter = null;

	public DataLoader(Study study, File workingDir, DataSource dataSource, StudyDbService studyDbService) {
		this.study = study;
		this.workingDir = workingDir;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.studyDbService = studyDbService;
	}

	public boolean loadData() {
		logger.info("Loading study " + study.getStudyName());
		
		createTempDir();
		clearStagingTables();
		
		// Check for new and modified files
		Collection<File> modifiedFiles = studyDbService.getModifiedDataFiles(study);
		Collection<File> newFiles = studyDbService.getNewDataFiles(study);
		logger.info("Found " + modifiedFiles.size() + " modified data files in study " + study.getStudyName());
		logger.info("Found " + newFiles.size() + " new data files in study " + study.getStudyName());
		
		// Exit if there are no new or modified files
		if (modifiedFiles.size() == 0 && newFiles.size() == 0) {
			return true;
		}
		
		// Create checklist to track which files have been processed
		Map<String, Boolean> fileChecklist = new HashMap<>();
		Set<File> allFiles = studyDbService.getDataFiles(study);
		for (File file : allFiles) {
			fileChecklist.put(file.getAbsolutePath().replaceAll("\\\\", "/"), Boolean.FALSE);
		}
		
		initializeFileWriters();

		// New study db version
		writeStudyDbVersionDataToStaging();
		
		// Create lookup indices
		populateIndex(siteIndex, "site", "site_id", "site_name");
		populateIndex(phaseIndex, "phase", "phase_id", "phase_name");
		populateIndex(fieldIndex, "field", "field_id", "field_name");
		populateIndex(dataStreamIndex, "data_stream", "data_stream_id", "data_stream_name");
		populateSubjectIndex();
		
		// Process new files
		for (File file : newFiles) {
			fileChecklist.put(file.getAbsolutePath().replaceAll("\\\\", "/"), Boolean.TRUE);
			if (study.getQueryFilePath() != null && file.getAbsolutePath().equals(study.getQueryFilePath())) {
				continue;
			}
			Long datasetId = writeDatasetToStaging(file);
			writeDatasetModificationToFile(datasetId, true);
			processFile(file, datasetId);
		}
		
		
		closeFileWriters();
		
		logger.info("Loading staging files");
		loadStagingFiles();
		
		logger.info("Copying data to main tables");
		copyDataToMainTables();
		
		return true;
	}
	
	private void initializeFileWriters() {
		try {
			this.observationWriter = new BufferedWriter(new FileWriter(
					new File(this.tempDir, "observation.csv")));
			this.datumWriter = new BufferedWriter(new FileWriter(
					new File(this.tempDir, "datum.csv")));
			this.datumVersionWriter = new BufferedWriter(new FileWriter(
					new File(this.tempDir, "datum_version.csv")));
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void closeFileWriters() {
		IOUtils.close(this.observationWriter);
		IOUtils.close(this.datumWriter);
		IOUtils.close(this.datumVersionWriter);
	}

	private void createTempDir() {
		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String dirName = "loader_files_" + format.format(date);
		File newDir = new File(workingDir, dirName);
		boolean successful = newDir.mkdir();
		if (!successful) {
			throw new RuntimeException("Unable to create temporary directory");
		}
		this.tempDir = newDir;
	}
	
	private void clearStagingTables() {
		clearTables("stg_study_db_version", "stg_dataset", "stg_dataset_version", "stg_site",
				"stg_subject", "stg_phase", "stg_data_stream", "stg_dataset_version_stream",
				"stg_observation", "stg_field", "stg_field_instance", "stg_dataset_version_field",
				"stg_study_db_version_config", "stg_dataset_modification", "stg_datum",
				"stg_datum_version");
	}
	
	private void clearTables(String ...tableNames) {
		for (String table : tableNames) {
			String sql = "delete from " + table;
			this.jdbcTemplate.update(sql);
		}
	}
	
	private Long getNextPkValue(String table, String column) {
		if (!this.primaryKeyIndex.containsKey(table)) {
			String sql = "select max(" + column + ") from " + table;
			Long max = this.jdbcTemplate.queryForObject(sql, Long.class);
			if (max == null) {
				max = 0L;
			}
			this.primaryKeyIndex.put(table, max);
		}
		Long currentMax = this.primaryKeyIndex.get(table);
		Long newMax = currentMax + 1;
		this.primaryKeyIndex.put(table, newMax);
		return newMax;
	}
	
	private void writeStudyDbVersionDataToStaging() {
		this.studyDbVersionId = getNextPkValue("study_db_version", "study_db_version_id");
		String studyDbVersionName = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		String sql = "insert into stg_study_db_version values (?, ?, ?, ?)";
		this.jdbcTemplate.update(sql, studyDbVersionId, studyDbVersionName, study.getStudyId(), 1);
	}
	
	private Long writeDatasetToStaging(File file) {
		Long datasetId = getNextPkValue("dataset", "dataset_id");
		String datasetName = file.getName();
		String filePath = file.getAbsolutePath().replaceAll("\\\\", "/");
		String sql = "insert into stg_dataset values (?, ?, ?, ?, ?)";
		this.jdbcTemplate.update(sql, datasetId, datasetName, filePath, 0, this.study.getStudyId());
		return datasetId;
	}
	
	private void writeDatasetModificationToFile(Long datasetId, boolean isNew) {
		Long datasetModificationId = getNextPkValue("dataset_modification", "dataset_modification_id");
		short newDataset = 0;
		short modifiedDataset = 1;
		if (isNew) {
			newDataset = 1;
			modifiedDataset = 0;
		}
		String sql = "insert into stg_dataset_modification values(?, ?, ?, ?, ?)";
		this.jdbcTemplate.update(sql, datasetModificationId, this.studyDbVersionId, datasetId,
				newDataset, modifiedDataset);
	}
	
	private void processFile(File file, Long datasetId) {
		logger.info("Processing file: " + file.getName());
		
		// Extract data from file
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
		
		// Dataset version
		Long datasetVersionId = writeNewDatasetVersionToStaging(file, df, datasetId);
		writeStudyDbVersionConfigToStaging(datasetVersionId);
		
		// Process records
		Map<String, Long> observationIndex = generateObservationIndex(datasetId);
		Map<String, Long> fieldInstanceIndex = generateFieldInstanceIndex(datasetId);
		Map<String, Long> datumIndex = generateDatumIndex(datasetId);
		Map<String, Long> datumVersionIndex = generateDatumVersionIndex(datasetId);
		Map<String, String> datumVersionValueIndex = generateDatumVersionValueIndex(datasetId);
		Set<Long> datasetVersionFieldsAdded = new HashSet<>();
		Iterator<Map<String, String>> iterator = df.iterator();
		while (iterator.hasNext()) {
			Map<String, String> record = iterator.next();
			
			String recordId = record.get(study.getRecordIdFieldName());
			String siteName = record.get(study.getSiteFieldName());
			String subjectName = record.get(study.getSubjectFieldName());
			String phaseName = record.get(study.getPhaseFieldName());
			
			if (anyNulls(recordId, siteName, subjectName, phaseName)) {
				continue;
			}
			
			// Site
			Long siteId = this.siteIndex.get(siteName);
			if (siteId == null) {
				siteId = writeNewSiteToStaging(siteName);
				this.siteIndex.put(siteName, siteId);
			}
			record.remove(study.getSiteFieldName());
			
			// Subject
			Long subjectId = this.subjectIndex.get(subjectName);
			if (subjectId == null) {
				subjectId = writeNewSubjectToStaging(subjectName, siteId);
				this.subjectIndex.put(subjectName, subjectId);
			}
			record.remove(study.getSubjectFieldName());
			
			// Phase
			Long phaseId = this.phaseIndex.get(phaseName);
			if (phaseId == null) {
				phaseId = writePhaseToStaging(phaseName);
				this.phaseIndex.put(phaseName, phaseId);
			}
			record.remove(study.getPhaseFieldName());
			
			// Data stream
			String streamName = record.get(study.getFormFieldName());
			Long streamId = this.dataStreamIndex.get(streamName);
			if (streamId == null) {
				streamId = writeDataStreamToStaging(streamName);
				this.dataStreamIndex.put(streamName, streamId);
				writeDatasetVersionStreamToStaging(datasetVersionId, streamId);
			}
			record.remove(study.getFormFieldName());
			
			// Observation
			Long observationId = observationIndex.get(recordId);
			if (observationId == null) {
				observationId = writeObservationToFile(recordId, datasetId, datasetVersionId,
						siteId, subjectId, phaseId);
				observationIndex.put(recordId, observationId);
			}
			record.remove(study.getRecordIdFieldName());
			
			// Load data fields
			for (String fieldName : record.keySet()) {
				
				// Field
				Long fieldId = this.fieldIndex.get(fieldName);
				if (fieldId == null) {
					fieldId = writeFieldToStaging(fieldName, df.getFieldLabel(fieldName),
							df.getDataType(fieldName).getName());
					this.fieldIndex.put(fieldName, fieldId);
				}
				
				// Field instance
				Long fieldInstanceId = fieldInstanceIndex.get(fieldName);
				if (fieldInstanceId == null) {
					fieldInstanceId = writeFieldInstanceToStaging(fieldId, datasetId, datasetVersionId);
					fieldInstanceIndex.put(fieldName, fieldInstanceId);
				}
				
				// Dataset version field join table
				if (!datasetVersionFieldsAdded.contains(fieldId)) {
					writeDatasetVersionFieldToStaging(datasetVersionId, fieldId);
					datasetVersionFieldsAdded.add(fieldId);
				}
				
				// Datum
				String datumKey = generateDatumNaturalKey(observationId, fieldId);
				Long datumId = datumIndex.get(datumKey);
				if (datumId == null) {
					datumId = writeDatumToFile(observationId, fieldId, datasetVersionId);
					datumIndex.put(datumKey, datumId);
				}
				
				// Datum version
				String value = record.get(fieldName);
				if (!(value == null || value.length() == 0 || value.equalsIgnoreCase("null"))) {
					Long datumVersionId = datumVersionIndex.get(datumKey);
					if (datumVersionId == null) {
						datumVersionId = writeDatumVersionToFile(datumId, value, datasetVersionId);
						datumVersionIndex.put(datumKey, datumVersionId);
					}
				}
			}
		}
	}
	
	private boolean anyNulls(String ...strings) {
		boolean nulls = false;
		for (String str : strings) {
			if (isNull(str)) {
				nulls = true;
				break;
			}
		}
		return nulls;
	}
	
	private boolean isNull(String str) {
		return str == null || str.length() == 0 || str.equalsIgnoreCase("null");
	}
	
	private Long writeNewDatasetVersionToStaging(File file, DataFrame df, Long datasetId) {
		Long datasetVersionId = getNextPkValue("dataset_version", "dataset_version_id");
		String datasetVersionName = studyDbService.generateDatasetVersionName(file);
		String sql = "insert into stg_dataset_version values(?, ?, ?, ?, ?)";
		this.jdbcTemplate.update(sql, datasetVersionId, datasetVersionName, 1, df.numRecords(), datasetId);
		return datasetVersionId;
	}
	
	private void writeStudyDbVersionConfigToStaging(Long datasetVersionId) {
		String sql = "insert into stg_study_db_version_config values(?, ?)";
		this.jdbcTemplate.update(sql, this.studyDbVersionId, datasetVersionId);
	}
	
	private void populateIndex(Map<String, Long> index, String tableName, String idField,
			String nameField) {
		String sql =
				"select " + idField + ", " + nameField + " " +
				"from " + tableName + " " +
				"where study_id = " + this.study.getStudyId();
		this.jdbcTemplate.query(sql, new RowCallbackHandler() {
			public void processRow(ResultSet rs) throws SQLException {
				index.put(rs.getString(2), rs.getLong(1));
			}
		});
	}
	
	private void populateSubjectIndex() {
		String sql =
				"select s.subject_id, s.subject_name\r\n" + 
				"from subject s\r\n" + 
				"join site si on si.site_id = s.site_id\r\n" + 
				"where si.study_id = " + this.study.getStudyId();
		this.jdbcTemplate.query(sql, new RowCallbackHandler() {
			public void processRow(ResultSet rs) throws SQLException {
				subjectIndex.put(rs.getString(2), rs.getLong(1));
			}
		});
	}
	
	private Map<String, Long> generateObservationIndex(Long datasetId) {
		Map<String, Long> index = new HashMap<>();
		String sql =
				"select o.observation_id, o.record_id " +
				"from observation o " +
				"where o.dataset_id = " + datasetId;
		this.jdbcTemplate.query(sql, new RowCallbackHandler() {
			public void processRow(ResultSet rs) throws SQLException {
				index.put(rs.getString(2), rs.getLong(1));
			}
		});
		return index;
	}
	
	private Map<String, Long> generateFieldInstanceIndex(Long datasetId) {
		Map<String, Long> index = new HashMap<>();
		String sql =
				"select f.field_name, fi.field_instance_id " +
				"from field f " +
				"join field_instance fi on fi.field_id = f.field_id " +
				"where fi.dataset_id = " + datasetId;
		this.jdbcTemplate.query(sql, new RowCallbackHandler() {
			public void processRow(ResultSet rs) throws SQLException {
				index.put(rs.getString(1), rs.getLong(2));
			}
		});
		return index;
	}
	
	private Long writeNewSiteToStaging(String siteName) {
		Long siteId = getNextPkValue("site", "site_id");
		String sql = "insert into stg_site values(?, ?, ?)";
		this.jdbcTemplate.update(sql, siteId, siteName, this.study.getStudyId());
		return siteId;
	}
	
	private Long writeNewSubjectToStaging(String subjectName, Long siteId) {
		Long subjectId = getNextPkValue("subject", "subject_id");
		String sql = "insert into stg_subject values(?, ?, ?)";
		this.jdbcTemplate.update(sql, subjectId, subjectName, siteId);
		return subjectId;
	}
	
	private Long writePhaseToStaging(String phaseName) {
		Long phaseId = getNextPkValue("phase", "phase_id");
		String sql = "insert into stg_phase values(?, ?, ?)";
		this.jdbcTemplate.update(sql, phaseId, phaseName, this.study.getStudyId());
		return phaseId;
	}
	
	private Long writeDataStreamToStaging(String streamName) {
		Long dataStreamId = getNextPkValue("data_stream", "data_stream_id");
		String sql = "insert into stg_data_stream values(?, ?, ?)";
		this.jdbcTemplate.update(sql, dataStreamId, streamName, this.study.getStudyId());
		return dataStreamId;
	}
	
	private void writeDatasetVersionStreamToStaging(Long datasetVersionId, Long dataStreamId) {
		String sql = "insert into stg_dataset_version_stream values(?, ?)";
		this.jdbcTemplate.update(sql, datasetVersionId, dataStreamId);
	}
	
	private Long writeObservationToFile(String recordId, Long datasetId, Long datasetVersionId,
			Long siteId, Long subjectId, Long phaseId) {
		Long observationId = getNextPkValue("observation", "observation_id");
		String record = toCsv(observationId, datasetId, datasetVersionId, siteId, subjectId, phaseId,
				recordId.replaceAll(",", " "));
		try {
			this.observationWriter.write(record + "\n");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return observationId;
	}
	
	private Long writeFieldToStaging(String fieldName, String fieldLabel, String dataType) {
		Long fieldId = getNextPkValue("field", "field_id");
		String sql = "insert into stg_field values(?, ?, ?, ?, ?, ?)";
		this.jdbcTemplate.update(sql, fieldId, fieldName, fieldLabel, study.getStudyId(), dataType, 0);
		return fieldId;
	}
	
	private Long writeFieldInstanceToStaging(Long fieldId, Long datasetId, Long datasetVersionId) {
		Long fieldInstanceId = getNextPkValue("field_instance", "field_instance_id");
		String sql = "insert into stg_field_instance values(?, ?, ?, ?)";
		this.jdbcTemplate.update(sql, fieldInstanceId, fieldId, datasetId, datasetVersionId);
		return fieldInstanceId;
	}
	
	private void writeDatasetVersionFieldToStaging(Long datasetVersionId, Long fieldId) {
		String sql = "insert into stg_dataset_version_field values(?, ?)";
		this.jdbcTemplate.update(sql, datasetVersionId, fieldId);
	}
	
	private Map<String, Long> generateDatumIndex(Long datasetId) {
		Map<String, Long> index = new HashMap<>();
		String sql =
				"select d.observation_id, d.field_id, d.datum_id " +
				"from datum d " +
				"join observation o on o.observation_id = d.observation_id " +
				"where o.dataset_id = " + datasetId;
		this.jdbcTemplate.query(sql, new RowCallbackHandler() {
			public void processRow(ResultSet rs) throws SQLException {
				String key = generateDatumNaturalKey(rs.getLong(1), rs.getLong(2));
				index.put(key, rs.getLong(3));
			}
		});
		return index;
	}
	
	private String generateDatumNaturalKey(Long observationId, Long fieldId) {
		return observationId + "---" + fieldId;
	}
	
	private Long writeDatumToFile(Long observationId, Long fieldId, Long datasetVersionId) {
		Long datumId = getNextPkValue("datum", "datum_id");
		String record = toCsv(datumId, fieldId, datasetVersionId, observationId);
		try {
			this.datumWriter.write(record + "\n");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return datumId;
	}
	
	private Long writeDatumVersionToFile(Long datumId, String value, Long datasetVersionId) {
		Long datumVersionId = getNextPkValue("datum_version", "datum_version_id");
		String record = toCsv(datumVersionId, value.replaceAll(",", " "), datasetVersionId,
				datasetVersionId, 1, datumId);
		try {
			this.datumVersionWriter.write(record + "\n");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return datumVersionId;
	}
	
	private Map<String, Long> generateDatumVersionIndex(Long datasetId) {
		Map<String, Long> index = new HashMap<>();
		String sql =
				"select dv.datum_version_id, d.observation_id, d.field_id " +
				"from datum_version dv " +
				"join datum d on d.datum_id = dv.datum_id " +
				"join observation o on o.observation_id = d.observation_id " +
				"where o.dataset_id = " + datasetId + " " +
				"and dv.is_current = 1";
		this.jdbcTemplate.query(sql, new RowCallbackHandler() {
			public void processRow(ResultSet rs) throws SQLException {
				String key = generateDatumNaturalKey(rs.getLong(2), rs.getLong(3));
				index.put(key, rs.getLong(1));
			}
		});
		return index;
	}
	
	private Map<String, String> generateDatumVersionValueIndex(Long datasetId) {
		Map<String, String> index = new HashMap<>();
		String sql =
				"select dv.value, o.observation_id, d.field_id " +
				"from datum_version dv " +
				"join datum d on d.datum_id = dv.datum_id " +
				"join observation o on o.observation_id = d.observation_id " +
				"where o.dataset_id = " + datasetId + " " +
				"and dv.is_current = 1";
		this.jdbcTemplate.query(sql, new RowCallbackHandler() {
			public void processRow(ResultSet rs) throws SQLException {
				String key = generateDatumNaturalKey(rs.getLong(2), rs.getLong(3));
				index.put(key, rs.getString(1));
			}
		});
		return index;
	}
	
	private String toCsv(Object ...values) {
		StringBuilder builder = new StringBuilder();
		int count = 0;
		for (Object value : values) {
			count++;
			if (count > 1) {
				builder.append(",");
			}
			builder.append(value.toString());
		}
		return builder.toString();
	}
	
	private void loadStagingFiles() {
		loadStagingFile(new File(tempDir, "observation.csv"), "stg_observation");
		loadStagingFile(new File(tempDir, "datum.csv"), "stg_datum");
		loadStagingFile(new File(tempDir, "datum_version.csv"), "stg_datum_version");
	}
	
	private void loadStagingFile(File file, String tableName) {
		String sql =
				"LOAD DATA LOCAL INFILE '" + file.getAbsolutePath().replaceAll("\\\\", "/") + "' " +
				"INTO TABLE " + tableName + " " +
				"FIELDS TERMINATED BY ',' " +
				"LINES TERMINATED BY '\n'";
		this.jdbcTemplate.execute(sql);
	}
	
	private void copyDataToMainTables() {
		try {
			Connection connection = this.jdbcTemplate.getDataSource().getConnection();
			boolean initialAutoCommitState = setConnectionForOptimizedLoading(connection);
			copyDataToMainTable(connection, "study_db_version");
			copyDataToMainTable(connection, "dataset");
			copyDataToMainTable(connection, "dataset_version");
			copyDataToMainTable(connection, "site");
			copyDataToMainTable(connection, "subject");
			copyDataToMainTable(connection, "phase");
			copyDataToMainTable(connection, "data_stream");
			copyDataToMainTable(connection, "dataset_version_stream");
			copyDataToMainTable(connection, "field");
			copyDataToMainTable(connection, "field_instance");
			copyDataToMainTable(connection, "observation");
			copyDataToMainTable(connection, "dataset_version_field");
			copyDataToMainTable(connection, "study_db_version_config");
			copyDataToMainTable(connection, "dataset_modification");
			copyDataToMainTable(connection, "datum");
			copyDataToMainTable(connection, "datum_version");
			connection.commit();
			returnConnectionToNormalOperation(connection, initialAutoCommitState);
		}
		catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	private boolean setConnectionForOptimizedLoading(Connection connection) throws SQLException {
		boolean initialAutocommitValue = connection.getAutoCommit();
		connection.setAutoCommit(false);
		Statement statement = null;
		try {
			statement = connection.createStatement();
			statement.executeUpdate("SET unique_checks=0");
			statement.executeUpdate("SET foreign_key_checks=0");
		}
		finally {
			if (statement != null) {
				statement.close();
			}
		}
		return initialAutocommitValue;
	}
	
	private void returnConnectionToNormalOperation(Connection connection,
			boolean initialAutoCommitState) throws SQLException {
		connection.setAutoCommit(initialAutoCommitState);
		Statement statement = null;
		try {
			statement = connection.createStatement();
			statement.executeUpdate("SET unique_checks=1");
			statement.executeUpdate("SET foreign_key_checks=1");
		}
		finally {
			if (statement != null) {
				statement.close();
			}
		}
	}
	
	private void copyDataToMainTable(Connection connection, String targetTable) throws SQLException {
		String sourceTable = "stg_" + targetTable;
		String sql = "select column_name from information_schema.columns where table_name = '" + sourceTable + "'";
		List<String> columns = this.jdbcTemplate.queryForList(sql, String.class);
		StringBuilder builder = new StringBuilder();
		int count = 0;
		for (String column : columns) {
			count++;
			if (count > 1) {
				builder.append(", ");
			}
			builder.append(column);
		}
		String colString = builder.toString();
		sql = "insert into " + targetTable + "(" + colString + ") select " + colString + " from " + sourceTable;
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			stmt.execute(sql);
		}
		finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}
}
