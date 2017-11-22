package com.rho.rhover.common.check;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rho.rhover.common.study.Dataset;
import com.rho.rhover.common.study.DatasetVersion;
import com.rho.rhover.common.study.DatasetVersionRepository;
import com.rho.rhover.common.study.Field;
import com.rho.rhover.common.study.Study;

@Service
public class CheckParamServiceImpl implements CheckParamService {
	
	@Autowired
	private CheckParamRepository checkParamRepository;
	
	@Autowired
	private DatasetVersionRepository datasetVersionRepository;

	@Override
	public CheckParam getCheckParam(Check check, String paramName, Study study) {
		CheckParam param = checkParamRepository.findByCheckAndStudyAndParamName(check, study, paramName);
		if (param == null) {
			param = checkParamRepository.findByCheckAndParamScopeAndParamName(check, "GLOBAL", paramName);
		}
		return param;
	}

	@Override
	public CheckParam getCheckParam(Check check, String paramName, Dataset dataset) {
		CheckParam param = checkParamRepository.findByCheckAndDatasetAndParamName(check, dataset, paramName);
		if (param == null) {
			param = getCheckParam(check, paramName, dataset.getStudy());
		}
		return param;
	}

	@Override
	public CheckParam getCheckParam(Check check, String paramName, Dataset dataset, Field field) {
		CheckParam param = checkParamRepository.findByCheckAndFieldAndParamName(check, field, paramName);
		if (param == null) {
			param = getCheckParam(check, paramName, dataset);
		}
		return param;
	}

}
