package com.rho.rhover.common.study;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface DatasetRepository extends CrudRepository<Dataset, Long>{

	Dataset findByFilePath(String filePath);

	List<Dataset> findByStudy(Study study);
	
}
