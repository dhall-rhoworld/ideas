package com.rho.rhover.common.study;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface FieldRepository extends CrudRepository<Field, Long>{

	Field findByStudyAndFieldName(Study study, String fieldName);

	Iterable<Field> findByStudy(Study study);
	
	List<Field> findAll(Iterable<Long> ids);
}
