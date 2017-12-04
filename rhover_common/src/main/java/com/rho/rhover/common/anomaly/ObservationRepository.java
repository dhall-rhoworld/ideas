package com.rho.rhover.common.anomaly;

import org.springframework.data.repository.CrudRepository;

import com.rho.rhover.common.study.Dataset;

public interface ObservationRepository extends CrudRepository<Observation, Long> {

	Observation findByDatasetAndIdFieldValueHash(Dataset dataset, String idFieldValueHash);
}
