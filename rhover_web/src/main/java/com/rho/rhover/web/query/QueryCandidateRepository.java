package com.rho.rhover.web.query;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.rho.rhover.common.anomaly.Anomaly;
import com.rho.rhover.common.study.Study;

public interface QueryCandidateRepository extends CrudRepository<QueryCandidate, Long> {

	@Query("select qc from QueryCandidate qc where qc.anomaly.field.study = ?1 and qc.queryStatus = ?2")
	List<QueryCandidate> findByStudyAndQueryStatus(Study study, QueryStatus queryStatus);
	
	QueryCandidate findByAnomaly(Anomaly anomaly);

	@Query("select qc from QueryCandidate qc where qc.anomaly.field.study = ?1")
	List<QueryCandidate> findByStudy(Study study);
}
