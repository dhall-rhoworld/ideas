package com.rho.rhover.common.anomaly;

import org.springframework.data.repository.CrudRepository;

import com.rho.rhover.common.study.Dataset;
import com.rho.rhover.common.study.Phase;
import com.rho.rhover.common.study.Site;
import com.rho.rhover.common.study.Subject;

public interface ObservationRepository extends CrudRepository<Observation, Long> {

	Observation findByDatasetAndSubjectAndPhaseAndRecordId(Dataset dataset, Subject subject, Phase phase, String recordId);
	
	Observation findByDatasetAndSubjectAndPhaseAndSiteAndRecordId(Dataset dataset, Subject subject, Phase phase, Site site, String recordId);
}
