package com.rho.rhover.common.anomaly;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.rho.rhover.common.check.Check;
import com.rho.rhover.common.check.CheckRun;

public interface AnomalyRepository extends CrudRepository<Anomaly, Long>{

	@Query("select a from Anomaly a where a.check = ?1 and ?2 member a.datumVersions")
	Anomaly findOne(Check check, DatumVersion datumVersion);
	
	@Query("select a from Anomaly a where ?1 member a.checkRuns")
	List<Anomaly> findByCheckRun(CheckRun checkRun);
}
