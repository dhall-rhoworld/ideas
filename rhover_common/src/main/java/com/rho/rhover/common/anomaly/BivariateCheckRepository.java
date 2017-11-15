package com.rho.rhover.common.anomaly;

import java.util.Collection;

import org.springframework.data.repository.CrudRepository;

import com.rho.rhover.common.study.Dataset;

public interface BivariateCheckRepository extends CrudRepository<BivariateCheck, Long>{

	Collection<BivariateCheck> findByDataset1(Dataset dataset);
	
	Collection<BivariateCheck> findByDataset2(Dataset dataset);
}
