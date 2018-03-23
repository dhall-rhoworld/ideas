package com.rho.rhover.daemon;

import java.io.File;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rho.rhover.common.study.Study;

@Service
public class FastDataLoaderService implements DataLoaderService {
	
	
	@Autowired
	private StudyDbService studyDbService;
	
	@Autowired
	private DataSource dataSource;
	
	@Value("${working.dir}")
	private String workingDirPath;

	@Override
	public void updateStudy(Study study) {
		File workingDir = new File(workingDirPath);
		DataLoader loader = new DataLoader(study, workingDir, dataSource, studyDbService);
		loader.loadData();
	}
	
	@Override
	public void calculateAndSaveCorrelations(Study study) {
		// TODO Auto-generated method stub
		
	}

}
