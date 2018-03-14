package com.rho.rhover.web.service;

import java.util.List;

import com.rho.rhover.common.study.StudyDbVersion;
import com.rho.rhover.web.reporting.DatasetLoadOverview;
import com.rho.rhover.web.reporting.StudyLoadOverview;

public interface ReportingService {

	List<StudyLoadOverview> getStudyLoadOverviews();
	
	List<DatasetLoadOverview> getAllDatasetLoadOverviews(StudyDbVersion studyDbVersion);
	
	List<DatasetLoadOverview> getNewDatasetLoadOverviews(StudyDbVersion studyDbVersion);
	
	List<DatasetLoadOverview> getModifiedDatasetLoadOverviews(StudyDbVersion studyDbVersion);
}
