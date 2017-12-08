package com.rho.rhover.common.check;

import org.springframework.data.repository.CrudRepository;

import com.rho.rhover.common.study.FieldInstance;

public interface CorrelationRepository extends CrudRepository<Correlation, Long> {

	Correlation findByFieldInstance1AndFieldInstance2(FieldInstance fieldInstance1, FieldInstance fieldInstance2);
}
