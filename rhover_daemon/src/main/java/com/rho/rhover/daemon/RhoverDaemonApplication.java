package com.rho.rhover.daemon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.epam.parso.Column;
import com.epam.parso.SasFileReader;
import com.epam.parso.impl.SasFileReaderImpl;
import com.rho.rhover.common.study.DataLocation;
import com.rho.rhover.common.study.DataLocationRepository;
import com.rho.rhover.common.study.Dataset;
import com.rho.rhover.common.study.DatasetRepository;
import com.rho.rhover.common.study.Study;
import com.rho.rhover.common.study.StudyRepository;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {"com.rho.rhover.common"})
@EntityScan("com.rho.rhover.common")
public class RhoverDaemonApplication implements CommandLineRunner {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private StudyRepository studyRepository;
	
	@Autowired
	private DatasetRepository datasetRepository;
	
	@Autowired
	private StudyDbService studyDbService;
	
	@Autowired
	private DataLocationRepository dataLocationRepository;

	public static void main(String[] args) {
		SpringApplication.run(RhoverDaemonApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		logger.info("Starting up RhoVer Daemon");
		
		Iterable<Study> studies = studyRepository.findAll();
		for (Study study : studies) {
			logger.info("Processing study: " + study.getStudyName());
			Collection<File> modifiedFiles = studyDbService.getModifiedDataFiles(study);
			logger.debug("Found " + modifiedFiles.size() + " modified data files in study " + study.getStudyName());
			Collection<File> newFiles = studyDbService.getNewDataFiles(study);
			logger.info("Found " + newFiles.size() + " new data files in study " + study.getStudyName());
			
			// Process new datasets
			for (File file : newFiles) {
				logger.debug("Saving new dataset for " + file.getAbsolutePath());
				
				// Extract data stream name, which will be the name for the new dataset
				DataFrame df = DataFrame.extractSasData(file);
				List<String> fields = df.getColNames();
				if (!fields.contains(study.getFormFieldName())) {
					logger.error("Data file " + file.getAbsolutePath() + " does not contain data stream field " + study.getFormFieldName());
					
					// TODO: Add user notification call
					continue;
				}
				Set<String> values = (Set<String>)df.getUniqueValues(study.getFormFieldName());
				if (values.size() != 1) {
					logger.error("Data file " + file.getAbsolutePath() + " contains data from more than one data stream");
					
					// TODO: Add user notification call
					continue;
				}
				String datasetName = values.iterator().next();
				
				// Save new dataset
				DataLocation location = dataLocationRepository.findByFolderPath(file.getParentFile().getAbsolutePath().replace("\\", "/"));
				Dataset dataset = new Dataset(datasetName, study, file.getAbsolutePath().replace("\\", "/"), location);
				datasetRepository.save(dataset);
				logger.info("Dataset " + datasetName + " saved");
			}
		}
		
	}
}
