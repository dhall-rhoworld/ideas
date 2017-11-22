package com.rho.rhover.common.study;

import org.springframework.data.repository.CrudRepository;

public interface DatasetRepository extends CrudRepository<Dataset, Long>{

	Dataset findByFilePath(String filePath);

	Iterable<Dataset> findByStudy(Study study);
	
}
