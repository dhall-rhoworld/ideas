package com.rho.rhover.common.study;

import java.util.List;

public interface FieldService {

	List<Field> findPotentiallyIdentiableFields(Study study);
	
	List<Field> findPotentialMergeFields(Dataset dataset1, Dataset dataset2);
	
	int getNumRecords(FieldInstance fieldInstance);
	
}
