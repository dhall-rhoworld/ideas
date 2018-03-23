package com.rho.rhover.common.check;

import java.util.Set;

import com.rho.rhover.common.study.Dataset;
import com.rho.rhover.common.study.Field;
import com.rho.rhover.common.study.Study;

public interface CheckParamService {

	CheckParam getCheckParam(Check check, String paramName, Study study);
	
	CheckParam getCheckParam(Check check, String paramName, Dataset dataset);
	
	CheckParam getCheckParam(Check check, String paramName, Dataset dataset, Field field);
	
	Set<CheckParam> getAllCheckParams(Check check, Study study);
	
	Set<CheckParam> getAllCheckParams(Check check, Dataset dataset, Field field);
	
	Set<CheckParam> getAllCheckParams(Check check, BivariateCheck biCheck);
	
	CheckParam getCheckParam(Check check, String paramName, BivariateCheck biCheck);
}
