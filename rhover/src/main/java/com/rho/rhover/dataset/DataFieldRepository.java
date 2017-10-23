package com.rho.rhover.dataset;

import org.springframework.data.repository.CrudRepository;

public interface DataFieldRepository extends CrudRepository<DataField, Long>{

	Iterable<DataField> findByDataset(Dataset dataset);
}
