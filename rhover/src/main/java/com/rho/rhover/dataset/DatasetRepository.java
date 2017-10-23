package com.rho.rhover.dataset;

import org.springframework.data.repository.CrudRepository;

import com.rho.rhover.study.Study;

public interface DatasetRepository extends CrudRepository<Dataset, Long>{

	Iterable<Dataset> findByStudy(Study study);
}
