package com.rho.rhover.common.check;

import org.springframework.data.repository.CrudRepository;

import com.rho.rhover.common.study.FieldInstance;

public interface BivariateCheckRepository extends CrudRepository<BivariateCheck, Long> {

	BivariateCheck findByXFieldInstanceAndYFieldInstance(FieldInstance xFieldInstance, FieldInstance yFieldInstance);
}
