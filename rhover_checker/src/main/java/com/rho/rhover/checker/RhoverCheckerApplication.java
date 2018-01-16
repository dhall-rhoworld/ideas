package com.rho.rhover.checker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.rho.rhover.common.check.Check;
import com.rho.rhover.common.check.CheckRepository;
import com.rho.rhover.common.study.Dataset;
import com.rho.rhover.common.study.DatasetRepository;
import com.rho.rhover.common.study.Study;
import com.rho.rhover.common.study.StudyRepository;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {"com.rho.rhover.common"})
@EntityScan("com.rho.rhover.common")
@ComponentScan({"com.rho.rhover.checker", "com.rho.rhover.common.check"})
public class RhoverCheckerApplication implements CommandLineRunner {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private StudyRepository studyRepository;
	
	@Autowired
	private CheckRepository checkRepository;
	
	@Autowired
	private DatasetRepository datasetRepository;
	
	@Autowired
	private CheckService checkService;

	public static void main(String[] args) {
		SpringApplication.run(RhoverCheckerApplication.class, args);
	}

	@Override
	public void run(String... arg0) throws Exception {
		Iterable<Study> studies = studyRepository.findAll();
		for (Study study : studies) {
			logger.info("Running data checks on study " + study.getStudyName());
			
			// Run univarite outlier checks
			logger.info("Running univariate outlier check on study " + study.getStudyName());
			Check check = checkRepository.findByCheckName("UNIVARIATE_OUTLIER");
			Iterable<Dataset> datasets = datasetRepository.findByStudy(study);
			for (Dataset dataset : datasets) {
				if (!dataset.getIsChecked()) {
					continue;
				}
				checkService.runUnivariateCheck(check, dataset);
			}
			
			// Run bivariate outlier checks
//			logger.info("Running bivariate outlier checks on study " + study.getStudyName());
//			check = checkRepository.findByCheckName("BIVARIATE_OUTLIER");
//			checkService.runBivariateChecks(check, study);
		}
	}
}
