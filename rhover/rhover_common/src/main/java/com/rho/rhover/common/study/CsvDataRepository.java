package com.rho.rhover.common.study;

import org.springframework.data.repository.CrudRepository;

public interface CsvDataRepository extends CrudRepository<CsvData, Long> {

	CsvData findByFieldAndDataset(Field field, Dataset dataset);
}
