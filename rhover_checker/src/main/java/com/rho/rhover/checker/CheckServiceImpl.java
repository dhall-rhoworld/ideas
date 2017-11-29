package com.rho.rhover.checker;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rho.rhover.common.check.Check;
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
	
	@Value("${working.dir}")
	private String workingDirPath;
	
	@Value("${r.exec.path}")
	private String rExecutablePath;
	
	@Value("${r.script.dir}")
	private String rScriptDirPath;

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
		File rScriptDir = new File(rScriptDirPath);
		if (!rScriptDir.isDirectory()) {
			throw new ConfigurationException("R script directory " + rScriptDirPath + " is not a valid directory.");
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
			
			// Construct an output dataset
			CsvData data = csvDataRepository.findByField(field);
			String outputData = generateOutputData(idData, data);
			
			// Write output dataset to file
			FileWriter fileWriter = null;
			try {
				File dataFile = File.createTempFile("univariate-" + field.getFieldName() + "-", ".csv", workingDir);
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
			if (true) {
				break;
			}
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
			builder.append(idDatum.getField().getFieldName() + "-" + idDatum.getField().getFieldId());
		}
		builder.append("," + data.getField().getFieldName() + "-" + data.getField().getFieldId() + "\n");
		
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
