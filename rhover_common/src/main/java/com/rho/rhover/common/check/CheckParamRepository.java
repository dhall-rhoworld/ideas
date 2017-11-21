package com.rho.rhover.common.check;

import org.springframework.data.repository.CrudRepository;

import com.rho.rhover.common.study.Dataset;
import com.rho.rhover.common.study.Field;
import com.rho.rhover.common.study.Study;

public interface CheckParamRepository extends CrudRepository<CheckParam, Long> {

	CheckParam findByParamNameAndStudy(String paramName, Study study);
	
	CheckParam findByParamNameAndDataset(String paramName, Dataset dataset);
	
	CheckParam findByParamNameAndField(String paramName, Field field);
}
