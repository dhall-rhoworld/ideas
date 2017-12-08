package com.rho.rhover.common.study;

import org.springframework.data.repository.CrudRepository;

public interface FieldInstanceRepository extends CrudRepository<FieldInstance, Long> {

	FieldInstance findByFieldAndDataset(Field field, Dataset dataset);
}
