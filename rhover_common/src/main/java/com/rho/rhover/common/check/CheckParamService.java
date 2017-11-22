package com.rho.rhover.common.check;

import com.rho.rhover.common.study.Dataset;
import com.rho.rhover.common.study.Field;
import com.rho.rhover.common.study.Study;

public interface CheckParamService {

	CheckParam getCheckParam(Check check, String paramName, Study study);
	
	CheckParam getCheckParam(Check check, String paramName, Dataset dataset);
	
	CheckParam getCheckParam(Check check, String paramName, Dataset dataset, Field field);
}
