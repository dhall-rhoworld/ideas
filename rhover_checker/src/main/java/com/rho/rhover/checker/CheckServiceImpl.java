package com.rho.rhover.checker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.transaction.Transactional;

import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import com.rho.rhover.common.anomaly.Anomaly;
import com.rho.rhover.common.anomaly.AnomalyRepository;
import com.rho.rhover.common.anomaly.DataIntegrityException;
import com.rho.rhover.common.anomaly.DataProperty;
import com.rho.rhover.common.anomaly.DataPropertyRepository;
import com.rho.rhover.common.anomaly.Datum;
import com.rho.rhover.common.anomaly.DatumRepository;
import com.rho.rhover.common.anomaly.DatumVersion;
import com.rho.rhover.common.anomaly.DatumVersionRepository;
import com.rho.rhover.common.anomaly.IdFieldValue;
import com.rho.rhover.common.anomaly.IdFieldValueRepository;
import com.rho.rhover.common.anomaly.Observation;
import com.rho.rhover.common.anomaly.ObservationRepository;
import com.rho.rhover.common.check.Check;
import com.rho.rhover.common.check.CheckParamRepository;
import com.rho.rhover.common.check.CheckParamService;
import com.rho.rhover.common.check.CheckRun;
import com.rho.rhover.common.check.CheckRunRepository;
import com.rho.rhover.common.check.ParamUsed;
import com.rho.rhover.common.check.ParamUsedRepository;
import com.rho.rhover.common.study.CsvData;
import com.rho.rhover.common.study.CsvDataRepository;
import com.rho.rhover.common.study.Dataset;
import com.rho.rhover.common.study.DatasetVersion;
import com.rho.rhover.common.study.DatasetVersionRepository;
import com.rho.rhover.common.study.Field;
import com.rho.rhover.common.study.FieldRepository;
import com.rho.rhover.common.study.Subject;
import com.rho.rhover.common.study.SubjectRepository;

@Service
public class CheckServiceImpl implements CheckService {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private DatasetVersionRepository datasetVersionRepository;
	
	@Autowired
	private FieldRepository fieldRepository;
	
	@Autowired
	private CsvDataRepository csvDataRepository;
	
	@Autowired
	private CheckParamService checkParamService;
	
	@Autowired
	private CheckRunRepository checkRunRepository;
	
	@Autowired
	private ParamUsedRepository paramUsedRepository;
	
	@Autowired
	private ObservationRepository observationRepository;
	
	@Autowired
	private IdFieldValueRepository idFieldValueRepository;
	
	@Autowired
	private SubjectRepository subjectRepository;
	
	@Autowired
	private DatumRepository datumRepository;
	
	@Autowired
	private DatumVersionRepository datumVersionRepository;
	
	@Autowired
	private AnomalyRepository anomalyRepository;
	
	@Autowired
	private DataPropertyRepository dataPropertyRepository;
	
	@Value("${working.dir}")
	private String workingDirPath;
	
	@Value("${r.exec.path}")
	private String rExecutablePath;
	
	@Value("${univariate.outlier.script.path}")
	private String univariateOutlierScriptPath;

	// TODO: Only run checks if a minumum number of records are present
	@Override
	@Transactional
	public void runUnivariateCheck(Check check, Dataset dataset) {
		String dataTypesToCheck = checkParamService.getCheckParam(check, "data_types", dataset).getParamValue();
		File workingDir = new File(workingDirPath);
		if (!workingDir.isDirectory()) {
			throw new ConfigurationException("Working directory " + workingDirPath + " is not a valid directory.");
		}
		File rExecutable = new File(rExecutablePath);
		if (!rExecutable.isFile()) {
			throw new ConfigurationException("Invalid R executable: " + rExecutablePath);
		}
		File rScriptDir = new File(univariateOutlierScriptPath);
		if (!rScriptDir.isFile()) {
			throw new ConfigurationException("Univariate outlier script " + univariateOutlierScriptPath + " is not a valid file.");
		}
		
		
		logger.debug("Running check " + check.getCheckName() + " on dataset " + dataset.getDatasetName());
		
		// Get identifying field data
		List<Field> idFields = fieldRepository.findByStudyAndIsIdentifying(dataset.getStudy(), Boolean.TRUE);
		List<CsvData> idData = new ArrayList<>();
		if (idFields.size() == 0) {
			throw new ConfigurationException("No identifying fields defined");
		}
		for (Field field : idFields) {
			idData.add(csvDataRepository.findByField(field));
		}
					
		DatasetVersion datasetVersion = datasetVersionRepository.findByDatasetAndIsCurrent(dataset, Boolean.TRUE);
		Iterable<Field> fields = datasetVersion.getFields();
		for (Field field : fields) {
			if (field.getIsSkipped()
					|| field.getIsIdentifying()
					|| (dataTypesToCheck.equals("continuous") && !field.getDataType().equals("Double"))
					|| (dataTypesToCheck.equals("numeric") && !(field.getDataType().equals("Double") || field.getDataType().equals("Integer")))) {
				continue;
			}
			logger.info("Running univarate outlier check on field " + field.getFieldName());
			
			// Construct an input dataset for R script
			CsvData data = csvDataRepository.findByField(field);
			String outputData = generateOutputData(idData, data, dataset);
			
			// Write input dataset to file
			FileWriter fileWriter = null;
			File dataFile = null;
			try {
				dataFile = File.createTempFile("univariate-" + field.getFieldName() + "-", "-in.csv", workingDir);
				fileWriter = new FileWriter(dataFile);
				fileWriter.write(outputData);
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
			finally {
				if (fileWriter != null) {
					try {
						fileWriter.close();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}
			
			// Run R script
			String fileNameRoot = workingDirPath + "/" + dataFile.getName().substring(0, dataFile.getName().length() - 7);
			String infilePath = workingDirPath + "/" + dataFile.getName();
			String outfilePath = fileNameRoot + "-out.csv";
			String propfilePath = fileNameRoot + "-prop.csv";
			String sd = checkParamService.getCheckParam(check, "sd", dataset, field).getParamValue();
			int dataColNum = idData.size();
			String command =
					rExecutablePath
					+ " " + univariateOutlierScriptPath
					+ " " + infilePath
					+ " " + outfilePath
					+ " " + propfilePath
					+ " " + (dataColNum + 1)
					+ " " + sd;
			//logger.debug(command);
			try {
				Process process = Runtime.getRuntime().exec(command);
				process.waitFor();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			
			// Record params used
			CheckRun checkRun = checkRunRepository.findByCheckAndDatasetVersionAndFieldAndIsLatest(check, datasetVersion, field, Boolean.TRUE);
			if (checkRun != null) {
				checkRun.setIsLatest(Boolean.FALSE);
				checkRunRepository.save(checkRun);
			}
			CheckRun newCheckRun = new CheckRun(datasetVersion, check, field, Boolean.TRUE);
			checkRunRepository.save(newCheckRun);
			ParamUsed paramUsed = new ParamUsed("sd", sd, newCheckRun);
			paramUsedRepository.save(paramUsed);
			
			// Process result
			BufferedReader outlierReader = null;
			BufferedReader paramReader = null;
			try {
				outlierReader = new BufferedReader(new FileReader(outfilePath));
				processOutliers(outlierReader, dataColNum, datasetVersion, field, newCheckRun);
				paramReader = new BufferedReader(new FileReader(propfilePath));
				processDataProperties(paramReader, newCheckRun);
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
			finally {
				try {
					if (outlierReader != null) {
						outlierReader.close();
					}
					if (paramReader != null) {
						paramReader.close();
					}
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			
			dataFile.delete();
			new File(outfilePath).delete();
			new File(propfilePath).delete();
			
			// TODO: Remove this
//			if (true) {
//				break;
//			}
		}
	}

	private void processDataProperties(BufferedReader reader, CheckRun checkRun) throws IOException {
		String line = reader.readLine();
		String[] propNames = line.split(",");
		line = reader.readLine();
		String[] propValues = line.split(",");
		for (int i = 0; i < propNames.length; i++) {
			dataPropertyRepository.save(new DataProperty(propNames[i], propValues[i], checkRun));
		}
	}

	private void processOutliers(BufferedReader reader, int dataColNum, DatasetVersion datasetVersion,
			Field field, CheckRun checkRun) throws IOException {
		
		// Map field IDs to column numbers in the file
		Dataset dataset = datasetVersion.getDataset();
		Map<Integer, Field> colNumToFieldId = new HashMap<>();
		int subjectColNum = -1;
		String line = reader.readLine();
		String[] values = line.split(",");
		for (int i = 0; i < dataColNum; i++) {
			String value = values[i];
			
			// generateOutputData appends field IDs to the end of field names
			// in the file header separated by '.'
			int p = value.lastIndexOf(".");
			String fieldName = value.substring(0, p);
			if (fieldName.equals(dataset.getStudy().getSubjectFieldName())) {
				subjectColNum = i;
			}
			Long fieldId = Long.parseLong(value.substring(p + 1));
			
			Field idField = fieldRepository.findOne(fieldId);
			colNumToFieldId.put(i, idField);
		}
		
		// TODO: Throw exception if subjectColNum == -1
		
		// Process outlier results
		line = reader.readLine();
		int outlierColNum = dataColNum + 1;
		while (line != null) {
			values = line.split(",");
			if (values[outlierColNum].equals("TRUE")) {
				//logger.debug(line);
				Collection<IdFieldValue> idFieldValues = new ArrayList<>();
				for (int i = 0; i < dataColNum; i++) {
					idFieldValues.add(new IdFieldValue(values[i], colNumToFieldId.get(i)));
				}
				String idFieldValueHash = Observation.generateIdFieldValueHash(idFieldValues);
				Observation observation = observationRepository.findByDatasetAndIdFieldValueHash(dataset, idFieldValueHash);
				if (observation == null) {
					String subjectName = values[subjectColNum];
					Subject subject = subjectRepository.findBySubjectName(subjectName);
					observation = new Observation(subject, dataset, idFieldValueHash);
					observationRepository.save(observation);
					for (IdFieldValue idFieldValue : idFieldValues) {
						idFieldValue.setObservation(observation);
						idFieldValueRepository.save(idFieldValue);
					}
				}
				Datum datum = datumRepository.findByObservationAndField(observation, field);
				if (datum == null) {
					datum = new Datum(field, observation);
					datumRepository.save(datum);
				}
//				else {
//					logger.debug("Datum seen before: " + line);
//				}
				DatumVersion datumVersion = datumVersionRepository.findByDatumAndIsCurrent(datum, Boolean.TRUE);
				String anomalousValue = values[dataColNum];
				if (datumVersion == null) {
					datumVersion = new DatumVersion(anomalousValue, Boolean.TRUE, datum);
					datumVersionRepository.save(datumVersion);
				}
				
				else {
					if (datumVersion.getDatasetVersions().contains(datasetVersion) && !datumVersion.getValue().equals(anomalousValue)) {
						throw new DataIntegrityException("Multiple records.  Last one is: " + line);
					}
					if (!datumVersion.getValue().equals(anomalousValue)) {
						datumVersion.setIsCurrent(Boolean.FALSE);
						datumVersionRepository.save(datumVersion);
						datumVersion = new DatumVersion(anomalousValue, Boolean.TRUE, datum);
						datumVersionRepository.save(datumVersion);
					}
				}
				
				if (!datumVersion.getDatasetVersions().contains(datasetVersion)) {
					datumVersion.getDatasetVersions().add(datasetVersion);
				}
				datumVersionRepository.save(datumVersion);
				Check check = checkRun.getCheck();
				Anomaly anomaly = anomalyRepository.findOne(check, datumVersion);
				if (anomaly == null) {
					//logger.debug("Creating new anomaly");
					anomaly = new Anomaly();
					anomaly.setCheck(check);
					anomaly.getDatumVersions().add(datumVersion);
				}
//				else {
//					logger.debug("Anomaly found");
//				}
				anomaly.getCheckRuns().add(checkRun);
				anomalyRepository.save(anomaly);
			}
			line = reader.readLine();
		}
	}

	private String generateOutputData(List<CsvData> idData, CsvData data, Dataset dataset) {
		StringBuilder builder = new StringBuilder();
		
		// Create tokenizers
		List<StringTokenizer> idDataTokenizers = new ArrayList<>();
		for (CsvData idDatum : idData) {
			idDataTokenizers.add(new StringTokenizer(idDatum.getData(), ","));
		}
		StringTokenizer dataTokenizer = new StringTokenizer(data.getData(), ",");
		
		// Column headers
		int count = 0;
		for (CsvData idDatum : idData) {
			count++;
			if (count > 1) {
				builder.append(",");
			}
			builder.append(idDatum.getField().getFieldName() + "." + idDatum.getField().getFieldId());
		}
		
		// Data value
		builder.append("," + data.getField().getFieldName() + "." + data.getField().getFieldId() + "\n");
		
		// Data records
		Set<String> idStrings = new HashSet<>();
		while (dataTokenizer.hasMoreElements()) {
			count = 0;
			StringBuilder observationBuilder = new StringBuilder();
			for (StringTokenizer tok : idDataTokenizers) {
				count++;
				if (count > 1) {
					observationBuilder.append(",");
				}
				observationBuilder.append(tok.nextToken());
			}
			if (idStrings.contains(observationBuilder.toString())) {
				throw new DataIntegrityException("Duplicate observation found in dataset " + dataset.getDatasetName()
					+ ": " + observationBuilder.toString());
			}
			idStrings.add(observationBuilder.toString());
			observationBuilder.append("," + dataTokenizer.nextToken());
			builder.append(observationBuilder.toString() + "\n");
		}
		
		return builder.toString();
	}
}
