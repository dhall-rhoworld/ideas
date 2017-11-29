package com.rho.rhover.common.study;

import org.springframework.data.repository.CrudRepository;

public interface CsvDataRepository extends CrudRepository<CsvData, Long> {

	CsvData findByField(Field field);
}
