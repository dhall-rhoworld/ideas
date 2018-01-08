package com.rho.rhover.common.check;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.rho.rhover.common.study.FieldInstance;
import com.rho.rhover.common.study.Study;

public interface BivariateCheckRepository extends CrudRepository<BivariateCheck, Long> {

	BivariateCheck findByXFieldInstanceAndYFieldInstance(FieldInstance xFieldInstance, FieldInstance yFieldInstance);
	
	List<BivariateCheck> findByStudy(Study study);
	
}
