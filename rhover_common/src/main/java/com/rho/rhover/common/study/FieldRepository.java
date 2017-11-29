package com.rho.rhover.common.study;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface FieldRepository extends CrudRepository<Field, Long>{

	Field findByStudyAndFieldName(Study study, String fieldName);
	
	List<Field> findByStudyAndIsIdentifying(Study study, Boolean isIdentifying);

	Iterable<Field> findByStudy(Study study);
}
