package com.rho.rhover.common.check;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.rho.rhover.common.study.Dataset;
import com.rho.rhover.common.study.Field;
import com.rho.rhover.common.study.Study;

public interface CheckParamRepository extends CrudRepository<CheckParam, Long> {

	List<CheckParam> findByCheckAndStudy(Check check, Study study);
	
	List<CheckParam> findByCheckAndDataset(Check check, Dataset dataset);
	
	List<CheckParam> findByCheckAndField(Check check, Field field);
	
	List<CheckParam> findByCheckAndParamScope(Check check, String paramScope);
	
	CheckParam findByCheckAndFieldAndParamName(Check check, Field field, String paramName);
	
	CheckParam findByCheckAndDatasetAndParamName(Check check, Dataset dataset, String paramName);
	
	CheckParam findByCheckAndStudyAndParamName(Check check, Study study, String paramName);
}
