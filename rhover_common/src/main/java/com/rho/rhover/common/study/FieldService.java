package com.rho.rhover.common.study;

public interface FieldService {

	Iterable<Field> findPotentiallyIdentiableFields(Study study);
	
	void setIdentifiableFields(Study study, Iterable<Long> fieldIds);
}
