package com.rho.rhover.daemon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

import com.rho.rhover.common.study.Study;
import com.rho.rhover.common.study.StudyRepository;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {"com.rho.rhover.common"})
@EntityScan("com.rho.rhover.common")
@ComponentScan("com.rho.rhover")
@EnableAsync
public class RhoverDaemonApplication implements CommandLineRunner {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private StudyRepository studyRepository;
	
	@Autowired
	private DataLoaderService dataLoaderService;

	public static void main(String[] args) {
		SpringApplication.run(RhoverDaemonApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		logger.info("Starting up RhoVer Daemon");
		
//		while (true) {
//			
//			logger.debug("Checking for new data");
//			Thread.sleep(5000);
//		}
		
		Iterable<Study> studies = studyRepository.findAll();
		for (Study study : studies) {
			try {
				dataLoaderService.updateStudy(study);
//				if (changedData) {
//					dataLoaderService.calculateAndSaveCorrelations(study);
//				}
			}
			catch (SourceDataException e) {
				logger.error("Data error encountered: " + e.getMessage());
				
				// TODO: Send notification to user
			}
		}
	}
}
