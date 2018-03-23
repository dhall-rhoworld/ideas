package com.rho.rhover.common.check;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.rho.rhover.common.study.Dataset;
import com.rho.rhover.common.study.Field;
import com.rho.rhover.common.study.Study;

public interface CheckParamRepository extends CrudRepository<CheckParam, Long> {

	List<CheckParam> findByCheckAndStudyAndIsCurrent(Check check, Study study, Boolean isCurrent);
	
	List<CheckParam> findByCheckAndDatasetAndIsCurrent(Check check, Dataset dataset, Boolean isCurrent);
	
	List<CheckParam> findByCheckAndFieldAndIsCurrent(Check check, Field field, Boolean isCurrent);
	
	List<CheckParam> findByCheckAndParamScopeAndIsCurrent(Check check, String paramScope, Boolean isCurrent);
	
	CheckParam findByCheckAndFieldAndParamNameAndIsCurrent(Check check, Field field, String paramName, Boolean isCurrent);
	
	CheckParam findByCheckAndDatasetAndParamNameAndIsCurrent(Check check, Dataset dataset, String paramName, Boolean isCurrent);
	
	CheckParam findByCheckAndStudyAndParamNameAndIsCurrent(Check check, Study study, String paramName, Boolean isCurrent);

	CheckParam findByCheckAndParamScopeAndParamNameAndIsCurrent(Check check, String string, String paramName, Boolean isCurrent);
}
