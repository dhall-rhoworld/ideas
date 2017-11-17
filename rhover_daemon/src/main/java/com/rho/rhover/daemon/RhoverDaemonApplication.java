package com.rho.rhover.daemon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.rho.rhover.common.study.DatasetVersion;
import com.rho.rhover.common.study.DatasetVersionRepository;
import com.rho.rhover.common.study.Site;
import com.rho.rhover.common.study.SiteRepository;
import com.rho.rhover.common.study.Study;
import com.rho.rhover.common.study.StudyRepository;
import com.rho.rhover.common.study.Subject;
import com.rho.rhover.common.study.SubjectRepository;

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
	
	@Autowired
	private DatasetVersionRepository datasetVersionRepository;
	
	@Autowired
	private SiteRepository siteRepository;
	
	@Autowired
	private SubjectRepository subjectRepository;

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
			processNewDatasets(study, newFiles);
		}
		
	}

	private void processNewDatasets(Study study, Collection<File> newFiles) {
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
			
			// Save new dataset version
			String datasetVersionName = studyDbService.generateDatasetVersionName(file);
			DatasetVersion version = new DatasetVersion(datasetVersionName, Boolean.TRUE, dataset);
			datasetVersionRepository.save(version);
			logger.info("Dataset version saved: " + datasetVersionName);
			
			// Save subjects and sites
			saveAnyNewSubjectsAndSites(study, df, file);
		}
	}

	private void saveAnyNewSubjectsAndSites(Study study, DataFrame df, File file) {
		if (!df.getColNames().contains(study.getSiteFieldName())) {
			logger.error("File " + file.getAbsolutePath() + " does not contain site field " + study.getSiteFieldName());
			
			// TODO: Add user notification call
			return;
		}
		Map<String, Site> siteMap = new HashMap<>();
		Set<?> siteNames = df.getUniqueValues(study.getSiteFieldName());
		for (Object siteName : siteNames) {
			Site site = siteRepository.findByStudyAndSiteName(study, siteName.toString());
			if (site == null) {
				logger.info("Saving new site: " + siteName.toString());
				site = new Site(siteName.toString(), study);
				siteRepository.save(site);
			}
			siteMap.put(siteName.toString(), site);
		}
		
		if (!df.getColNames().contains(study.getSubjectFieldName())) {
			logger.error("File " + file.getAbsolutePath() + " does not contain subject field " + study.getSubjectFieldName());
			
			// TODO: Add user notification call
			return;
		}
		Set<?> subjectNames = df.getUniqueValues(study.getSubjectFieldName());
		Map<String, String> siteNameMap = generateSubjectNameToSiteNameMapping(study, df);
		for (Object subjectName : subjectNames) {
			Subject subject = subjectRepository.findBySubjectName(subjectName.toString());
			if (subject == null) {
				//logger.info("Saving new subject: " + subjectName.toString());
				String siteName = siteNameMap.get(subjectName.toString());
				if (siteName == null) {
					logger.warn("Subject " + subjectName + " does not have an associated site in file " + file.getAbsolutePath() + ".  Not saving.");
					
					// TODO: Add user notification call
				}
				else {
					Site site = siteMap.get(siteName);
					subject = new Subject(subjectName.toString(), site);
				}
			}
		}
	}
	
	
	private Map<String, String> generateSubjectNameToSiteNameMapping(Study study, DataFrame df) {
		Map<String, String> map = new HashMap<>();
		List<?> subjectNames = df.getField(study.getSubjectFieldName());
		List<?> siteNames = df.getField(study.getSiteFieldName());
		int n = subjectNames.size();
		for (int i = 0; i < n; i++) {
			map.put(subjectNames.get(i).toString(), siteNames.get(i).toString());
		}
		return map;
	}
}
