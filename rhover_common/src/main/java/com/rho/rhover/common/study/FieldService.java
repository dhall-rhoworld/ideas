package com.rho.rhover.common.study;

import java.util.List;

public interface FieldService {

	List<Field> findPotentiallyIdentiableFields(Study study);
	
	void setIdentifiableFields(Study study, Iterable<Long> fieldIds);
}
