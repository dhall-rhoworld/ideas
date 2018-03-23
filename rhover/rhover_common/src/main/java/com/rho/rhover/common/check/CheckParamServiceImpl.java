package com.rho.rhover.common.check;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rho.rhover.common.study.Dataset;
import com.rho.rhover.common.study.Field;
import com.rho.rhover.common.study.Study;

@Service
public class CheckParamServiceImpl implements CheckParamService {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private CheckParamRepository checkParamRepository;
	
	@Override
	public CheckParam getCheckParam(Check check, String paramName, Study study) {
		CheckParam param = checkParamRepository.findByCheckAndStudyAndParamNameAndIsCurrent(check, study, paramName, Boolean.TRUE);
		if (param == null) {
			param = checkParamRepository.findByCheckAndParamScopeAndParamNameAndIsCurrent(check, "GLOBAL", paramName, Boolean.TRUE);
		}
		return param;
	}

	@Override
	public CheckParam getCheckParam(Check check, String paramName, Dataset dataset) {
		CheckParam param = checkParamRepository.findByCheckAndDatasetAndParamNameAndIsCurrent(check, dataset, paramName, Boolean.TRUE);
		if (param == null) {
			param = getCheckParam(check, paramName, dataset.getStudy());
		}
		return param;
	}

	@Override
	public CheckParam getCheckParam(Check check, String paramName, Dataset dataset, Field field) {
		CheckParam param = checkParamRepository.findByCheckAndFieldAndParamNameAndIsCurrent(check, field, paramName, Boolean.TRUE);
		if (param == null) {
			param = getCheckParam(check, paramName, dataset);
		}
		return param;
	}

	@Override
	public Set<CheckParam> getAllCheckParams(Check check, Study study) {
		List<CheckParam> globalParams = checkParamRepository.findByCheckAndParamScopeAndIsCurrent(check, "GLOBAL", Boolean.TRUE);
		Set<CheckParam> params = new HashSet<>();
		for (CheckParam globalParam : globalParams) {
			logger.debug(globalParam.getParamName() + " -> " + globalParam.getParamValue());
			CheckParam studyParam = getCheckParam(check, globalParam.getParamName(), study);
			if (studyParam != null) {
				params.add(studyParam);
			}
			else {
				params.add(globalParam);
			}
		}
		return params;
	}

	@Override
	public Set<CheckParam> getAllCheckParams(Check check, Dataset dataset, Field field) {
		List<CheckParam> globalParams = checkParamRepository.findByCheckAndParamScopeAndIsCurrent(check, "GLOBAL", Boolean.TRUE);
		Set<CheckParam> params = new HashSet<>();
		for (CheckParam globalParam : globalParams) {
			params.add(getCheckParam(check, globalParam.getParamName(), dataset, field));
		}
		return params;
	}

	@Override
	public Set<CheckParam> getAllCheckParams(Check check, BivariateCheck biCheck) {
		Map<String, CheckParam> paramMap = biCheck.getCheckParams();
		Set<CheckParam> params = new HashSet<>();
		if (paramMap.size() > 0) {
			params.addAll(paramMap.values());
		}
		else {
			params = getAllCheckParams(check, biCheck.getStudy());
		}
		return params;
	}

	@Override
	public CheckParam getCheckParam(Check check, String paramName, BivariateCheck biCheck) {
		CheckParam param = biCheck.getCheckParams().get(paramName);
		if (param == null) {
			param = getCheckParam(check, paramName, biCheck.getStudy());
		}
		return param;
	}
}
