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
import com.rho.rhover.common.check.BivariateCheck;
import com.rho.rhover.common.check.BivariateCheckRepository;
import com.rho.rhover.common.check.Check;
import com.rho.rhover.common.check.CheckParam;
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
import com.rho.rhover.common.study.Site;
import com.rho.rhover.common.study.SiteRepository;
import com.rho.rhover.common.study.Study;
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
	private SiteRepository siteRepository;
	
	@Autowired
	private DatumRepository datumRepository;
	
	@Autowired
	private DatumVersionRepository datumVersionRepository;
	
	@Autowired
	private AnomalyRepository anomalyRepository;
	
	@Autowired
	private DataPropertyRepository dataPropertyRepository;
	
	@Autowired
	private BivariateCheckRepository bivariateCheckRepository;
	
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
		logger.debug("Running check " + check.getCheckName() + " on dataset " + dataset.getDatasetName());
		
		// Get working directory
		File workingDir = new File(workingDirPath);
		if (!workingDir.isDirectory()) {
			throw new ConfigurationException("Working directory " + workingDirPath + " is not a valid directory.");
		}
		
		// Get reference to R executable
		File rExecutable = new File(rExecutablePath);
		if (!rExecutable.isFile()) {
			throw new ConfigurationException("Invalid R executable: " + rExecutablePath);
		}
		
		// Get reference to univariate check R script
		File rScriptDir = new File(univariateOutlierScriptPath);
		if (!rScriptDir.isFile()) {
			throw new ConfigurationException("Univariate outlier script " + univariateOutlierScriptPath + " is not a valid file.");
		}
		
		// Get identifying fields (i.e. set of fields that uniquely identifies a clinical data record) specified by study admin
		List<Field> idFields = fieldRepository.findByStudyAndIsIdentifying(dataset.getStudy(), Boolean.TRUE);
		List<CsvData> idData = new ArrayList<>();
		if (idFields.size() == 0) {
			throw new ConfigurationException("No identifying fields defined");
		}
		
		// If not included by study admin, add the subject and site fields to set of identifying fields
		boolean subjectIdIncluded = false;
		boolean siteIdIncluded = false;
		Study study = dataset.getStudy();
		for (Field field : idFields) {
			idData.add(csvDataRepository.findByFieldAndDataset(field, dataset));
			if (field.getFieldName().equals(study.getSubjectFieldName())) {
				subjectIdIncluded = true;
			}
			else if (field.getFieldName().equals(study.getSiteFieldName())) {
				siteIdIncluded = true;
			}
		}
		if (!subjectIdIncluded) {
			idData.add(csvDataRepository.findByFieldAndDataset(fieldRepository.findByStudyAndFieldName(study, study.getSubjectFieldName()), dataset));
		}
		if (!siteIdIncluded) {
			idData.add(csvDataRepository.findByFieldAndDataset(fieldRepository.findByStudyAndFieldName(study, study.getSiteFieldName()), dataset));
		}
		
		// Determine which data types should be checked as specified by the study admin.  Possible values include
		// 'numeric', 'continuous', and 'custom'.  If the latter, the study admin will have selected individual fields.
		String dataTypesToCheck = checkParamService.getCheckParam(check, "data_types", dataset).getParamValue();
		
		// Iterate over fields and perform data checks
		DatasetVersion datasetVersion = datasetVersionRepository.findByDatasetAndIsCurrent(dataset, Boolean.TRUE);
		Iterable<Field> fields = datasetVersion.getFields();
		
		for (Field field : fields) {
			
			// Determine if check should be run on the field
			if (!shouldCheckField(field, dataTypesToCheck, check, datasetVersion)) {
				continue;
			}
			logger.info("Running univarate outlier check on study " + study.getStudyName() + ", field " + field.getFieldName());
			
			// Construct an input dataset for R script
			CsvData data = csvDataRepository.findByFieldAndDataset(field, dataset);
			String rScriptInput = generateRScriptInput(idData, data, dataset);
			
			// Write R script input dataset to file
			FileWriter fileWriter = null;
			File dataFile = null;
			try {
				dataFile = File.createTempFile("univariate-" + field.getFieldName() + "-", "-in.csv", workingDir);
				fileWriter = new FileWriter(dataFile);
				fileWriter.write(rScriptInput);
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
			
			// Run R script.  The script will produce 2 output files:  (1) A copy of the input file with an additional
			// boolean column indicating if the records is an outlier, and (2) A file containing properties of the dataset
			// computed by the R script, e.g. standard deviation.
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
			try {
				Process process = Runtime.getRuntime().exec(command);
				process.waitFor();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			
			// Save parameter values used in the check
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
		}
	}
	
	//
	// Should given univariate check be run on given field?
	//
	private boolean shouldCheckField(Field field, String dataTypesToCheck, Check check, DatasetVersion datasetVersion) {
		boolean checkable = !(field.getIsSkipped()
			|| field.getIsIdentifying()
			|| (dataTypesToCheck.equals("continuous") && !field.getDataType().equals("Double"))
			|| (dataTypesToCheck.equals("numeric") && !(field.getDataType().equals("Double") || field.getDataType().equals("Integer"))));
		if (checkable) {
			CheckRun latestRun = checkRunRepository.findByCheckAndDatasetVersionAndFieldAndIsLatest(check, datasetVersion, field, Boolean.TRUE);
			if (latestRun != null) {
				checkable = false;
				Set<CheckParam> params = checkParamService.getAllCheckParams(check, datasetVersion.getDataset(), field);
				for (CheckParam param : params) {
					ParamUsed paramUsed = paramUsedRepository.findByCheckRunAndParamName(latestRun, param.getParamName());
					if (paramUsed != null && !param.getParamValue().equals(paramUsed.getParamValue())) {
						checkable = true;
						break;
					}
				}
			}
		}
		return checkable;
	}

	//
	// Save data properties computed by the R script, e.g. standard deviation
	//
	private void processDataProperties(BufferedReader reader, CheckRun checkRun) throws IOException {
		String line = reader.readLine();
		String[] propNames = line.split(",");
		line = reader.readLine();
		String[] propValues = line.split(",");
		for (int i = 0; i < propNames.length; i++) {
			dataPropertyRepository.save(new DataProperty(propNames[i], propValues[i], checkRun));
		}
	}

	//
	// Save outliers identified by R script as new anomalies
	//
	private void processOutliers(BufferedReader reader, int dataColNum, DatasetVersion datasetVersion,
			Field field, CheckRun checkRun) throws IOException {
		
		// Map field IDs to column numbers in the file
		Dataset dataset = datasetVersion.getDataset();
		Map<Integer, Field> colNumToFieldId = new HashMap<>();
		int subjectColNum = -1;
		int siteColNum = -1;
		String line = reader.readLine();
		String[] values = line.split(",");
		for (int i = 0; i < dataColNum; i++) {
			String value = values[i];
			
			// Note: generateOutputData() appends field IDs to the end of field names
			// in the file header separated by '.'
			int p = value.lastIndexOf(".");
			String fieldName = value.substring(0, p);
			if (fieldName.equals(dataset.getStudy().getSubjectFieldName())) {
				subjectColNum = i;
			}
			else if (fieldName.equals(dataset.getStudy().getSiteFieldName())) {
				siteColNum = i;
			}
			Long fieldId = Long.parseLong(value.substring(p + 1));
			
			Field idField = fieldRepository.findOne(fieldId);
			colNumToFieldId.put(i, idField);
		}
		
		// TODO: Throw exception if subjectColNum == -1
		
		// Process R script output file
		line = reader.readLine();
		int outlierColNum = dataColNum + 1;
		while (line != null) {
			values = line.split(",");
			
			// Case: Record is an outlier
			if (values[outlierColNum].equals("TRUE")) {
				
				// Extract ID field values
				Collection<IdFieldValue> idFieldValues = new ArrayList<>();
				for (int i = 0; i < dataColNum; i++) {
					idFieldValues.add(new IdFieldValue(values[i], colNumToFieldId.get(i)));
				}
				
				// Hash ID field values, which is needed to fetch an observation instance from the database
				String idFieldValueHash = Observation.generateIdFieldValueHash(idFieldValues);
				
				// Fetch or create new observation
				Observation observation = observationRepository.findByDatasetAndIdFieldValueHash(dataset, idFieldValueHash);
				if (observation == null) {
					observation = new Observation(dataset, idFieldValueHash);
					observationRepository.save(observation);
					for (IdFieldValue idFieldValue : idFieldValues) {
						idFieldValue.setObservation(observation);
						idFieldValueRepository.save(idFieldValue);
					}
				}
				
				// Fetch or save new datum
				Datum datum = datumRepository.findByObservationAndField(observation, field);
				if (datum == null) {
					datum = new Datum(field, observation);
					datumRepository.save(datum);
				}
				
				// Fetch or save new datum version
				DatumVersion datumVersion = datumVersionRepository.findByDatumAndIsCurrent(datum, Boolean.TRUE);
				String anomalousValue = values[dataColNum];
				if (datumVersion == null) {
					datumVersion = new DatumVersion(anomalousValue, Boolean.TRUE, datum);
					datumVersionRepository.save(datumVersion);
				}
				
				// If there is an existing datum version, update if warrented
				else {
					
					// Check if the record is a duplicate
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
				
				// Update or save new anomaly
				Check check = checkRun.getCheck();
				Anomaly anomaly = anomalyRepository.findOne(check, datumVersion);
				if (anomaly == null) {
					//logger.debug("Creating new anomaly");
					String subjectName = values[subjectColNum];
					Subject subject = subjectRepository.findBySubjectName(subjectName);
					String siteName = values[siteColNum];
					Site site = siteRepository.findByStudyAndSiteName(dataset.getStudy(), siteName);
					anomaly = new Anomaly();
					anomaly.setCheck(check);
					anomaly.getDatumVersions().add(datumVersion);
					anomaly.setSite(site);
					anomaly.setSubject(subject);
					anomaly.setField(field);
				}
				anomaly.getCheckRuns().add(checkRun);
				anomalyRepository.save(anomaly);
			}
			
			line = reader.readLine();
		}
	}

	//
	// Generate input data file for R script
	//
	private String generateRScriptInput(List<CsvData> idData, CsvData data, Dataset dataset) {
		StringBuilder builder = new StringBuilder();
		
		// Create tokenizers
		List<StringTokenizer> idDataTokenizers = new ArrayList<>();
		for (CsvData idDatum : idData) {
			idDataTokenizers.add(new StringTokenizer(idDatum.getData(), ","));
		}
		StringTokenizer dataTokenizer = new StringTokenizer(data.getData(), ",");
		
		// ID column headers
		int count = 0;
		for (CsvData idDatum : idData) {
			count++;
			if (count > 1) {
				builder.append(",");
			}
			builder.append(idDatum.getField().getFieldName() + "." + idDatum.getField().getFieldId());
		}
		
		// Checked column header
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
			String value = dataTokenizer.nextToken();
			if (!(value == null || value.equals("null"))) {
				observationBuilder.append("," + value);
				builder.append(observationBuilder.toString() + "\n");
			}
		}
		
		return builder.toString();
	}

	@Override
	public void runBivariateChecks(Check check, Study study) {
		
		// Get working directory
		File workingDir = new File(workingDirPath);
		if (!workingDir.isDirectory()) {
			throw new ConfigurationException("Working directory " + workingDirPath + " is not a valid directory.");
		}
		
		List<BivariateCheck> biChecks = bivariateCheckRepository.findByStudy(study);
		for (BivariateCheck biCheck : biChecks) {
			
			// Retrieve parameters
			logger.debug("Checking " + biCheck.getxFieldInstance().getField().getDisplayName()
					+ " and " + biCheck.getyFieldInstance().getField().getDisplayName());
			Set<CheckParam> params = checkParamService.getAllCheckParams(check, biCheck);
			for (CheckParam param : params) {
				logger.debug("Param " + param.getParamName() + ": " + param.getParamValue()
						+ " [" + param.getParamScope() + "]");
			}
			
			String rInputData = generateRInputData(biCheck);
		}
	}

	private String generateRInputData(BivariateCheck biCheck) {
		// TODO Auto-generated method stub
		return null;
	}
}
