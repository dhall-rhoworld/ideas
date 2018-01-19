package com.rho.rhover.common.study;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface FieldRepository extends CrudRepository<Field, Long>{

	Field findByStudyAndFieldName(Study study, String fieldName);

	Iterable<Field> findByStudy(Study study);
	
	List<Field> findAll(Iterable<Long> ids);
	
	@Query("select f from Field f where ?1 member f.datasetVersions and f.isIdentifying = ?2")
	List<Field> findByDatasetVersionAndIsIdentifying(DatasetVersion datasetVersion, Boolean isIdentifying);
}
