package com.rho.rhover.checker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
			if (field.getIsSkipped() || field.getIsIdentifying()) {
				continue;
			}
			logger.info("Running univarate outlier check on field " + field.getFieldName());
			
			// Construct an input dataset for R script
			CsvData data = csvDataRepository.findByField(field);
			String outputData = generateOutputData(idData, data);
			
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
			String paramfilePath = fileNameRoot + "-param.csv";
			String sd = checkParamService.getCheckParam(check, "sd", dataset, field).getParamValue();
			int dataColNum = idData.size();
			String command =
					rExecutablePath
					+ " " + univariateOutlierScriptPath
					+ " " + infilePath
					+ " " + outfilePath
					+ " " + paramfilePath
					+ " " + (dataColNum + 1)
					+ " " + sd;
			logger.debug(command);
			try {
				Process process = Runtime.getRuntime().exec(command);
				process.waitFor();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			
			// Process result
			BufferedReader outlierReader = null;
			BufferedReader paramReader = null;
			try {
				outlierReader = new BufferedReader(new FileReader(outfilePath));
				processOutliers(outlierReader, dataColNum);
				paramReader = new BufferedReader(new FileReader(paramfilePath));
				processDataParams(paramReader);
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
						
					}
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
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
			
			//dataFile.delete();
			
			// TODO: Remove this
			if (true) {
				break;
			}
		}
	}

	private void processDataParams(BufferedReader reader) {
		// TODO Auto-generated method stub
		
	}

	private void processOutliers(BufferedReader reader, int dataColNum) throws IOException {
		
		// Map field IDs to column numbers in the file
		Map<Integer, Long> colNumToFieldId = new HashMap<>();
		String line = reader.readLine();
		String[] values = line.split(",");
		for (int i = 0; i <= dataColNum; i++) {
			String value = values[i];
			
			// generateOutputData appends field IDs to the end of field names
			// in the file header separated by '.'
			Long fieldId = Long.parseLong(value.substring(value.lastIndexOf(".") + 1));
			
			colNumToFieldId.put(i, fieldId);
		}
		
		// Process outlier results
		line = reader.readLine();
		int outlierColNum = dataColNum + 1;
		while (line != null) {
			values = line.split(",");
			if (values[outlierColNum].equals("TRUE")) {
				logger.debug("Outlier: " + line);
			}
			line = reader.readLine();
		}
	}

	private String generateOutputData(List<CsvData> idData, CsvData data) {
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
		builder.append("," + data.getField().getFieldName() + "." + data.getField().getFieldId() + "\n");
		
		// Data records
		while (dataTokenizer.hasMoreElements()) {
			count = 0;
			for (StringTokenizer tok : idDataTokenizers) {
				count++;
				if (count > 1) {
					builder.append(",");
				}
				builder.append(tok.nextToken());
			}
			builder.append("," + dataTokenizer.nextToken() + "\n");
		}
		
		return builder.toString();
	}
}
