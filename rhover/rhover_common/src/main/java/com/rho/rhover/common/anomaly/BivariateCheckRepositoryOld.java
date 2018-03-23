package com.rho.rhover.common.anomaly;

import java.util.Collection;

import org.springframework.data.repository.CrudRepository;

import com.rho.rhover.common.study.Dataset;

public interface BivariateCheckRepositoryOld extends CrudRepository<BivariateCheckOld, Long>{

	Collection<BivariateCheckOld> findByDataset1(Dataset dataset);
	
	Collection<BivariateCheckOld> findByDataset2(Dataset dataset);
}
