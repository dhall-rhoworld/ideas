package com.rho.rhover.common.anomaly;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.rho.rhover.common.study.Dataset;
import com.rho.rhover.common.study.Field;

public interface DatumRepository extends CrudRepository<Datum, Long> {

	Datum findByObservationAndField(Observation observation, Field field);

	@Query("select d from Datum d where d.observation.dataset = ?1")
	List<Datum> findByDataset(Dataset dataset);
}
