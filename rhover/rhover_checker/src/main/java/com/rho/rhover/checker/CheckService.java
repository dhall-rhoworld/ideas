package com.rho.rhover.checker;

import com.rho.rhover.common.check.Check;
import com.rho.rhover.common.study.Dataset;
import com.rho.rhover.common.study.Study;

public interface CheckService {

	void runUnivariateCheck(Check check, Dataset dataset);
	
	void runBivariateChecks(Check check, Study study);
}
