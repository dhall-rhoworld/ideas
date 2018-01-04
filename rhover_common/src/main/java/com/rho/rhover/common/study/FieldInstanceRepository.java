package com.rho.rhover.common.study;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface FieldInstanceRepository extends CrudRepository<FieldInstance, Long> {

	FieldInstance findByFieldAndDataset(Field field, Dataset dataset);
	
	List<FieldInstance> findByDataset(Dataset dataset);
	
	@Query("select fi from FieldInstance fi where fi.field.study = ?1 and fi.field.dataType = ?2")
	List<FieldInstance> findByStudyAndDataType(Study study, String dataType);
	
	@Query("select fi from FieldInstance fi where fi.dataset = ?1 and fi.field.dataType = ?2")
	List<FieldInstance> findByDatasetAndDataType(Dataset dataset, String dataType);
}
