package com.rho.rhover.common.check;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.rho.rhover.common.study.FieldInstance;
import com.rho.rhover.common.study.Study;

public interface CorrelationRepository extends CrudRepository<Correlation, Long> {

	Correlation findByFieldInstance1AndFieldInstance2(FieldInstance fieldInstance1, FieldInstance fieldInstance2);

	List<Correlation> findByStudy(Study study);

}
