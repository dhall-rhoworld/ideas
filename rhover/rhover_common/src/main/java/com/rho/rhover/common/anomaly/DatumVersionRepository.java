package com.rho.rhover.common.anomaly;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.rho.rhover.common.study.Dataset;

public interface DatumVersionRepository extends CrudRepository<DatumVersion, Long> {

	DatumVersion findByDatumAndIsCurrent(Datum datum, Boolean isCurrent);

	@Query("select dv from DatumVersion dv where dv.datum.observation.dataset = ?1 and dv.isCurrent = ?2")
	List<DatumVersion> findByDatasetAndIsCurrent(Dataset dataset, Boolean isCurrent);
}
