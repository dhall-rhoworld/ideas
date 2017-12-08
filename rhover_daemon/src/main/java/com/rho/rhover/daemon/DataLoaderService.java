package com.rho.rhover.daemon;

import com.rho.rhover.common.study.Study;

public interface DataLoaderService {

	boolean updateStudy(Study study);
	
	void calculateAndSaveCorrelations(Study study);
}
