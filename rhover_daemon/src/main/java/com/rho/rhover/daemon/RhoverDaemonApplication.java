package com.rho.rhover.daemon;

import java.io.File;
import java.io.FilenameFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.rho.rhover.common.study.DataLocation;
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

	public static void main(String[] args) {
		SpringApplication.run(RhoverDaemonApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		logger.info("Starting up RhoVer Daemon");
		
		// Look for new datasets
		logger.info("Looking for new datasets");
		Iterable<Study> studies = studyRepository.findAll();
		for (Study study : studies) {
			logger.info("Looking for new datasets in study " + study.getStudyName());
			Iterable<DataLocation> locations = study.getDataLocations();
			for (DataLocation location : locations) {
				logger.info("Looking for new datasets in folder " + location.getFolderPath());
				File folder = new File(location.getFolderPath());
				File[] files = folder.listFiles(new FilenameFilter() {
					
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith(".sas7bdat");
					}
				});
				for (int i = 0; i < files.length; i++) {
					File file = files[i];
					String filePath = file.getAbsolutePath();
					logger.info("Looking for dataset associated with file " + filePath);
					Dataset dataset = datasetRepository.findByFilePath(filePath);
					if (dataset == null) {
						logger.info("No dataset associated with file " + filePath);
						String datasetName = file.getName().substring(0, file.getName().indexOf(".sas7bdat"));
						dataset = new Dataset(datasetName, study, filePath, location);
						logger.info("Saving new dataset with name: " + datasetName);
						datasetRepository.save(dataset);
					}
				}
			}
		}
	}
}
