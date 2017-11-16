package com.rho.rhover.common.study;

import org.springframework.data.repository.CrudRepository;

public interface DataLocationRepository extends CrudRepository<DataLocation, Long> {

	DataLocation findByFolderPath(String folderPath);
}
