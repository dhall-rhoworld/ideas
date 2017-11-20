package com.rho.rhover.common.study;

import org.springframework.data.repository.CrudRepository;

public interface FieldRepository extends CrudRepository<Field, Long>{

	Field findByStudyAndFieldName(Study study, String fieldName);
	
	Iterable<Field> findByStudyAndIsIdentifying(Study study, Boolean isIdentifying);

	Iterable<Field> findByStudy(Study study);
	
}
