package com.rho.rhover.common.check;

import java.util.List;

import com.rho.rhover.common.study.FieldInstance;

public interface CorrelationFinder {

	List<Correlation> findAllCorrelatedFields(FieldInstance fieldInstance, double minCorrelationCoeff);
	
}
