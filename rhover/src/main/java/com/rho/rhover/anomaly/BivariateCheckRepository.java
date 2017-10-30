package com.rho.rhover.anomaly;

import org.springframework.data.repository.CrudRepository;

import com.rho.rhover.study.Dataset;

public interface BivariateCheckRepository extends CrudRepository<BivariateCheck, Long>{

	Iterable<BivariateCheck> findByDataset(Dataset dataset);
}
