package com.rho.rhover.common.study;

import org.springframework.data.repository.CrudRepository;

public interface DatasetVersionRepository extends CrudRepository<DatasetVersion, Long> {

	DatasetVersion findByDatasetAndIsCurrent(Dataset dataset, Boolean isCurrent);

}
