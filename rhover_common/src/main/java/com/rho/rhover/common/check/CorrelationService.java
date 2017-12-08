package com.rho.rhover.common.check;

import com.rho.rhover.common.study.FieldInstance;

public interface CorrelationService {

	Correlation getCorrelationWithAnyFieldOrder(FieldInstance fieldInstance1, FieldInstance fieldInstance2);
	
	void save(Correlation correlation);
}
