package com.rho.rhover.checker;

import com.rho.rhover.common.check.Check;
import com.rho.rhover.common.study.Dataset;

public interface CheckService {

	void runUnivariateCheck(Check check, Dataset dataset);
}
