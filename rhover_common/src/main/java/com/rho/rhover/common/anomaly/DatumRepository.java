package com.rho.rhover.common.anomaly;

import org.springframework.data.repository.CrudRepository;

import com.rho.rhover.common.study.Field;

public interface DatumRepository extends CrudRepository<Datum, Long> {

	Datum findByObservationAndField(Observation observation, Field field);
}
