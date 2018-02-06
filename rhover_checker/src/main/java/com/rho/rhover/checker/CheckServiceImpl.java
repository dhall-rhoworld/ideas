package com.rho.rhover.checker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rho.rhover.common.anomaly.Anomaly;
import com.rho.rhover.common.anomaly.AnomalyRepository;
import com.rho.rhover.common.anomaly.DataProperty;
import com.rho.rhover.common.anomaly.DataPropertyRepository;
import com.rho.rhover.common.anomaly.Datum;
import com.rho.rhover.common.anomaly.DatumRepository;
import com.rho.rhover.common.anomaly.DatumVersion;
import com.rho.rhover.common.anomaly.DatumVersionRepository;
import com.rho.rhover.common.anomaly.Observation;
import com.rho.rhover.common.anomaly.ObservationRepository;
import com.rho.rhover.common.check.BivariateCheck;
import com.rho.rhover.common.check.BivariateCheckRepository;
import com.rho.rhover.common.check.Check;
import com.rho.rhover.common.check.CheckParam;
import com.rho.rhover.common.check.CheckParamService;
import com.rho.rhover.common.check.CheckRun;
import com.rho.rhover.common.check.CheckRunRepository;
import com.rho.rhover.common.check.ParamUsed;
import com.rho.rhover.common.check.ParamUsedRepository;
import com.rho.rhover.common.study.CsvDataService;
import com.rho.rhover.common.study.Dataset;
import com.rho.rhover.common.study.DatasetVersion;
import com.rho.rhover.common.study.DatasetVersionRepository;
import com.rho.rhover.common.study.Field;
import com.rho.rhover.common.study.FieldInstance;
import com.rho.rhover.common.study.FieldInstanceRepository;
import com.rho.rhover.common.study.Phase;
import com.rho.rhover.common.study.PhaseRepository;
import com.rho.rhover.common.study.Study;
import com.rho.rhover.common.study.Subject;
import com.rho.rhover.common.study.SubjectRepository;
import com.rho.rhover.common.util.IOUtils;

@Service
public class CheckServiceImpl implements CheckService {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private DatasetVersionRepository datasetVersionRepository;
	
	@Autowired
	private CheckParamService checkParamService;
	
	@Autowired
	private CheckRunRepository checkRunRepository;
	
	@Autowired
	private ParamUsedRepository paramUsedRepository;
	
	@Autowired
	private ObservationRepository observationRepository;
	
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
	
	@Autowired
	private BivariateCheckRepository bivariateCheckRepository;
	
	@Autowired
	private FieldInstanceRepository fieldInstanceRepository;
	
	@Autowired
	private PhaseRepository phaseRepository;
	
	@Autowired
	private CsvDataService csvDataService;
	
	@Value("${working.dir}")
	private String workingDirPath;
	
	@Value("${r.exec.path}")
	private String rExecutablePath;
	
	@Value("${univariate.outlier.script.path}")
	private String univariateOutlierScriptPath;
	
	@Value("${bivariate.outlier.script.path}")
	private String bivariateOutlierScriptPath;
	
	private File workingDir;
	
	File rExecutable;
	
	File univariateOutlierScript;
	
	@PostConstruct
	public void init() {
		workingDir = new File(workingDirPath);
		if (!workingDir.isDirectory()) {
			throw new ConfigurationException("Working directory " + workingDirPath + " is not a valid directory.");
		}
		rExecutable = new File(rExecutablePath);
		if (!rExecutable.isFile()) {
			throw new ConfigurationException("Invalid R executable: " + rExecutablePath);
		}
		univariateOutlierScript = new File(univariateOutlierScriptPath);
		if (!univariateOutlierScript.isFile()) {
			throw new ConfigurationException("Univariate outlier script " + univariateOutlierScriptPath + " is not a valid file.");
		}
	}
	

	// TODO: Only run checks if a minumum number of records are present
	@Override
	@Transactional
	public void runUnivariateCheck(Check check, Dataset dataset) {
		logger.debug("Running check " + check.getCheckName() + " on dataset " + dataset.getDatasetName());
		Study study = dataset.getStudy();
		
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
			FieldInstance fieldInstance = fieldInstanceRepository.findByFieldAndDataset(field, dataset);
			String rScriptInput = generateRInputData(dataset, fieldInstance);
			
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
				IOUtils.close(fileWriter);
			}
			
			// Run R script.  The script will produce 2 output files:  (1) A copy of the input file with an additional
			// boolean column indicating if the records is an outlier, and (2) A file containing properties of the dataset
			// computed by the R script, e.g. standard deviation.
			String fileNameRoot = workingDirPath + "/" + dataFile.getName().substring(0, dataFile.getName().length() - 7);
			String infilePath = workingDirPath + "/" + dataFile.getName();
			String outfilePath = fileNameRoot + "-out.csv";
			String propfilePath = fileNameRoot + "-prop.csv";
			String sd = checkParamService.getCheckParam(check, "sd", dataset, field).getParamValue();
			int dataColNum = 3;
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
				processOutliers(outlierReader, datasetVersion, field, newCheckRun, false, null, null);
				paramReader = new BufferedReader(new FileReader(propfilePath));
				processDataProperties(paramReader, newCheckRun);
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
			finally {
				IOUtils.close(outlierReader);
				IOUtils.close(paramReader);
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
			|| field.getStudy().isFieldIdentifying(field)
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
	private void processOutliers(BufferedReader reader, DatasetVersion datasetVersion,
			Field field, CheckRun checkRun, boolean isBivariate,
			DatasetVersion datasetVersion2, Field field2) throws IOException {
		
		int outlierColNum = 4;
		if (isBivariate) {
			outlierColNum = 5;
		}
		
		// Read past header
		reader.readLine();
		
		// Process data
		String line = reader.readLine();
		while (line != null) {
			String[] values = line.split(",");
			if (values[outlierColNum].equals("TRUE")) {
				
				Subject subject = subjectRepository.findBySubjectName(values[0]);
				Phase phase = phaseRepository.findByPhaseName(values[1]);
				String recordId = values[2];
				String anomalousValue = values[3];
				
				// Update or save new anomaly
				Check check = checkRun.getCheck();
				Anomaly anomaly = null;
				DatumVersion datumVersion = fetchOrCreateDatumVersionAndDependentObjects(
						recordId, subject, phase, field, datasetVersion, anomalousValue);
				DatumVersion datumVersion2 = null;
				if (isBivariate) {
					String anomalousValue2 = values[4];
					datumVersion2 = fetchOrCreateDatumVersionAndDependentObjects(
							recordId, subject, phase, field2, datasetVersion2, anomalousValue2);
					anomaly = anomalyRepository.findOne(check, datumVersion, datumVersion2);
				}
				else {
					anomaly = anomalyRepository.findOne(check, datumVersion);
				}
				if (anomaly == null) {
					//logger.debug("Creating new anomaly");
					FieldInstance fieldInstance = fieldInstanceRepository.findByFieldAndDataset(field2, datasetVersion.getDataset());
					anomaly = new Anomaly();
					anomaly.setCheck(check);
					anomaly.getDatumVersions().add(datumVersion);
					anomaly.setSite(subject.getSite());
					anomaly.setSubject(subject);
					anomaly.setField(field);
					anomaly.setPhase(phase);
					anomaly.setRecordId(recordId);
					anomaly.setFieldInstance(fieldInstance);
					if (isBivariate) {
						anomaly.getBivariateDatumVersions2().add(datumVersion2);
						anomaly.setBivariateField2(field2);
						FieldInstance fieldInstance2 = fieldInstanceRepository.findByFieldAndDataset(field2, datasetVersion2.getDataset());
						anomaly.setBivariateFieldInstance2(fieldInstance2);
					}
				}
				anomaly.getCheckRuns().add(checkRun);
				anomalyRepository.save(anomaly);
			}
			
			line = reader.readLine();
		}
	}

	private DatumVersion fetchOrCreateDatumVersionAndDependentObjects(String recordId, Subject subject, Phase phase,
			Field field, DatasetVersion datasetVersion, String anomalousValue) {
		
		// Fetch or create new observation
		Observation observation = observationRepository.findByDatasetAndSubjectAndPhaseAndRecordId(datasetVersion.getDataset(), subject, phase, recordId);
		if (observation == null) {
//			logger.debug("Creating new observation for subject: " + subject.getSubjectName() + ", phase: " + phase.getPhaseName()
//				+ ", recordId: " + recordId);
			observation = new Observation(datasetVersion.getDataset(), subject, phase, recordId);
			observationRepository.save(observation);
		}
		
		// Fetch or save new datum
		Datum datum = datumRepository.findByObservationAndField(observation, field);
		if (datum == null) {
			//logger.debug("Creating new datum for field: " + field.getDisplayName());
			datum = new Datum(field, observation);
			datumRepository.save(datum);
		}
		
		// Fetch or save new datum version
		DatumVersion datumVersion = datumVersionRepository.findByDatumAndIsCurrent(datum, Boolean.TRUE);
		
		if (datumVersion == null) {
			//logger.debug("Creating new datum version");
			datumVersion = new DatumVersion(anomalousValue, Boolean.TRUE, datum);
			datumVersionRepository.save(datumVersion);
		}
		
		if (!datumVersion.getDatasetVersions().contains(datasetVersion)) {
			datumVersion.getDatasetVersions().add(datasetVersion);
		}
		datumVersionRepository.save(datumVersion);
		
		return datumVersion;
	}

	@Override
	@Transactional
	public void runBivariateChecks(Check check, Study study) {
		
		// Get working directory
		File workingDir = new File(workingDirPath);
		if (!workingDir.isDirectory()) {
			throw new ConfigurationException("Working directory " + workingDirPath + " is not a valid directory.");
		}
		
		List<BivariateCheck> biChecks = bivariateCheckRepository.findByStudy(study);
		for (BivariateCheck biCheck : biChecks) {
			
			// Retrieve parameters
//			logger.debug("Checking " + biCheck.getxFieldInstance().getField().getDisplayName()
//					+ " and " + biCheck.getyFieldInstance().getField().getDisplayName());
			Set<CheckParam> params = checkParamService.getAllCheckParams(check, biCheck);
			for (CheckParam param : params) {
//				logger.debug("Param " + param.getParamName() + ": " + param.getParamValue()
//						+ " [" + param.getParamScope() + "]");
			}
			
			// Generate input and output files
			BufferedWriter writer = null;
			ScriptIoFiles scriptIoFiles = null;
			try {
				scriptIoFiles = generateScriptIoFiles("bivariate-" + biCheck.getxFieldInstance().getFieldInstanceId()
						+ "-" + biCheck.getyFieldInstance().getFieldInstanceId() + "-");
				String rInputData = generateRInputData(biCheck, study);
				writer = new BufferedWriter(new FileWriter(scriptIoFiles.inputDataFile));
				writer.write(rInputData);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			finally {
				IOUtils.close(writer);
			}
			
			// Run R script
			CheckParam sdResidual = checkParamService.getCheckParam(check, "sd-residual", biCheck);
			CheckParam sdDensity = checkParamService.getCheckParam(check, "sd-density", biCheck);
			CheckParam numNearestNeighbors = checkParamService.getCheckParam(check, "num-nearest-neighbors", biCheck);
			String command =
					rExecutablePath
					+ " " + bivariateOutlierScriptPath
					+ " " + scriptIoFiles.inputDataFile
					+ " " + scriptIoFiles.outputOutlierFile
					+ " " + scriptIoFiles.outputStatsFile
					+ " 4"
					+ " " + sdResidual.getParamValue()
					+ " " + numNearestNeighbors.getParamValue()
					+ " " + sdDensity.getParamValue();
			logger.debug(command);
			try {
				Process process = Runtime.getRuntime().exec(command);
				process.waitFor();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			
			// Save parameter values used in the check
			DatasetVersion datasetVersion1 = datasetVersionRepository.findByDatasetAndIsCurrent(
					biCheck.getxFieldInstance().getDataset(), Boolean.TRUE);
			DatasetVersion datasetVersion2 = datasetVersionRepository.findByDatasetAndIsCurrent(
					biCheck.getyFieldInstance().getDataset(), Boolean.TRUE);
			CheckRun oldCheckRun = checkRunRepository.findByBivariateCheckAndDatasetVersionAndBivariateDatasetVersion2AndIsLatest(
					biCheck, datasetVersion1, datasetVersion2, Boolean.TRUE);
			if (oldCheckRun != null) {
				oldCheckRun.setIsLatest(Boolean.FALSE);
				checkRunRepository.save(oldCheckRun);
			}
			CheckRun newCheckRun = new CheckRun(datasetVersion1, datasetVersion2, check, biCheck, Boolean.TRUE);
			checkRunRepository.save(newCheckRun);
			ParamUsed paramUsed = new ParamUsed("sd-residual", sdResidual.getParamValue(), newCheckRun);
			paramUsedRepository.save(paramUsed);
			paramUsed = new ParamUsed("num-nearest-neighbors", numNearestNeighbors.getParamValue(), newCheckRun);
			paramUsedRepository.save(paramUsed);
			paramUsed = new ParamUsed("sd-density", sdDensity.getParamValue(), newCheckRun);
			paramUsedRepository.save(paramUsed);
			
			// Process outlier data file
			BufferedReader outlierReader = null;
			BufferedReader statPropsReader = null;
			try {
				outlierReader = new BufferedReader(new FileReader(scriptIoFiles.outputOutlierFile));
				Field field1 = biCheck.getxFieldInstance().getField();
				Field field2 = biCheck.getyFieldInstance().getField();
				processOutliers(outlierReader, datasetVersion1, field1, newCheckRun, true, datasetVersion2, field2);
				statPropsReader = new BufferedReader(new FileReader(scriptIoFiles.outputStatsFile));
				processDataProperties(statPropsReader, newCheckRun);
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
			finally {
				IOUtils.close(outlierReader);
				IOUtils.close(statPropsReader);
			}
		}
	}

	private String generateRInputData(BivariateCheck biCheck, Study study) {
		String dataStr = null;
		
		// Case: fields in same dataset
		if (biCheck.fieldsInSameDataset()) {
			dataStr = generateRInputData(biCheck.getxFieldInstance().getDataset(), biCheck.getxFieldInstance(), biCheck.getyFieldInstance());
		}
		
		return dataStr;
	}
	
	private String generateRInputData(Dataset dataset, FieldInstance ...dataFieldInstances) {
		List<FieldInstance> fieldInstances = new ArrayList<>();
		Study study = dataset.getStudy();
		fieldInstances.add(fieldInstanceRepository.findByFieldAndDataset(study.getSubjectField(), dataset));
		fieldInstances.add(fieldInstanceRepository.findByFieldAndDataset(study.getPhaseField(), dataset));
		fieldInstances.add(fieldInstanceRepository.findByFieldAndDataset(study.getRecordIdField(), dataset));
		for (FieldInstance data : dataFieldInstances) {
			fieldInstances.add(data);
		}
		return csvDataService.getCsvData(fieldInstances, false, true);
	}
	
	private ScriptIoFiles generateScriptIoFiles(String prefix) throws IOException {
		ScriptIoFiles sif = new ScriptIoFiles();
		sif.inputDataFile = File.createTempFile(prefix, "-in.csv", workingDir);
		String root = sif.inputDataFile.getName().substring(0, sif.inputDataFile.getName().length() - 7);
		sif.outputOutlierFile = new File(workingDir, root + "-out.csv");
		sif.outputStatsFile = new File(workingDir, root + "-stats.csv");
		return sif;
	}
	
	private class ScriptIoFiles {
		private File inputDataFile;
		private File outputOutlierFile;
		private File outputStatsFile;
		
		private void removeAll() {
			if (inputDataFile.exists()) {
				inputDataFile.delete();
			}
			if (outputOutlierFile.exists()) {
				outputOutlierFile.delete();
			}
			if (outputStatsFile.exists()) {
				outputStatsFile.delete();
			}
		}
	}
}
