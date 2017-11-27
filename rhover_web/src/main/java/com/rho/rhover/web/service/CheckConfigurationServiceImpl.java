package com.rho.rhover.web.service;

import java.util.Collection;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rho.rhover.common.check.Check;
import com.rho.rhover.common.check.CheckParam;
import com.rho.rhover.common.check.CheckParamRepository;
import com.rho.rhover.common.check.CheckParamService;
import com.rho.rhover.common.study.Dataset;
import com.rho.rhover.common.study.DatasetRepository;
import com.rho.rhover.common.study.Study;

@Service
public class CheckConfigurationServiceImpl implements CheckConfigurationService {
	
	@Autowired
	private CheckParamRepository checkParamRepository;
	
	@Autowired
	private DatasetRepository datasetRepository;

	@Override
	@Transactional
	public void saveStudyCheckConfiguration(Study study, Check check, Map<String, String> params,
			Collection<Long> checkedDatasetIds) {
		
		// Save check parameters
		for (String paramName : params.keySet()) {
			String paramValue = params.get(paramName);
			CheckParam checkParam = checkParamRepository.findByCheckAndStudyAndParamName(check, study, paramName);
			if (checkParam == null) {
				checkParam = new CheckParam(paramName, "STUDY", check);
				checkParam.setStudy(study);
			}
			checkParam.setParamValue(paramValue);
			checkParamRepository.save(checkParam);
		}
		
		// Save datasets to be checked
		Iterable<Dataset> datasets = datasetRepository.findByStudy(study);
		for (Dataset dataset : datasets) {
			dataset.setIsChecked(Boolean.FALSE);
			datasetRepository.save(dataset);
		}
		for (Long datasetId : checkedDatasetIds) {
			Dataset dataset = datasetRepository.findOne(datasetId);
			dataset.setIsChecked(Boolean.TRUE);
			datasetRepository.save(dataset);
		}
	}

}
