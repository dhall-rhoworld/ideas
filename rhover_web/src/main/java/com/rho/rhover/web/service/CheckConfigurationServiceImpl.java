package com.rho.rhover.web.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rho.rhover.common.check.Check;
import com.rho.rhover.common.check.CheckParam;
import com.rho.rhover.common.check.CheckParamRepository;
import com.rho.rhover.common.study.Dataset;
import com.rho.rhover.common.study.DatasetRepository;
import com.rho.rhover.common.study.DatasetVersion;
import com.rho.rhover.common.study.DatasetVersionRepository;
import com.rho.rhover.common.study.Field;
import com.rho.rhover.common.study.FieldRepository;
import com.rho.rhover.common.study.Study;

@Service
public class CheckConfigurationServiceImpl implements CheckConfigurationService {
	
	@Autowired
	private CheckParamRepository checkParamRepository;
	
	@Autowired
	private DatasetRepository datasetRepository;
	
	@Autowired
	private FieldRepository fieldRepository;
	
	@Autowired
	private DatasetVersionRepository datasetVersionRepository;

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

	@Override
	@Transactional
	public void saveDatasetCheckConfiguration(Dataset dataset, Check check, Boolean useStudyDefaults,
			Map<String, String> datasetParamValues, Map<Long, Map<String, String>> fieldParamValues, Collection<Long> skippedFieldIds) {
		
		// Save/update/delete dataset-level check parameters
		List<CheckParam> datasetParams = checkParamRepository.findByCheckAndDataset(check, dataset);
		if (useStudyDefaults) {
			checkParamRepository.delete(datasetParams);
		}
		else {
			List<CheckParam> globalParams = checkParamRepository.findByCheckAndParamScope(check, "GLOBAL");
			Set<String> globalParamNames = new HashSet<>();
			for (CheckParam globalParam : globalParams) {
				globalParamNames.add(globalParam.getParamName());
			}
			for (String paramName : datasetParamValues.keySet()) {
				String paramValue = datasetParamValues.get(paramName);
				CheckParam param = checkParamRepository.findByCheckAndDatasetAndParamName(check, dataset, paramName);
				if (param == null) {
					param = new CheckParam(paramName, "DATASET", check);
					param.setDataset(dataset);
				}
				param.setParamValue(paramValue);
				checkParamRepository.save(param);
				globalParamNames.remove(paramName);
			}
			for (String paramName : globalParamNames) {
				CheckParam param = checkParamRepository.findByCheckAndDatasetAndParamName(check, dataset, paramName);
				if (param == null) {
					param = new CheckParam(paramName, "DATASET", check);
					param.setDataset(dataset);
				}
				param.setParamValue("off");
				checkParamRepository.save(param);
			}
		}
		
		// Save/update skipped fields
		DatasetVersion datasetVersion = datasetVersionRepository.findByDatasetAndIsCurrent(dataset, Boolean.TRUE);
		for (Field field : datasetVersion.getFields()) {
			field.setIsSkipped(Boolean.FALSE);
			fieldRepository.save(field);
		}
		for (Long fieldId : skippedFieldIds) {
			Field field = fieldRepository.findOne(fieldId);
			field.setIsSkipped(Boolean.TRUE);
			fieldRepository.save(field);
		}
		
		// Save/update field param values
		for (Field field : datasetVersion.getFields()) {
			List<CheckParam> fieldParams = checkParamRepository.findByCheckAndField(check, field);
			checkParamRepository.delete(fieldParams);
		}
		for (Long fieldId : fieldParamValues.keySet()) {
			Field field = fieldRepository.findOne(fieldId);
			Map<String, String> namesAndValues = fieldParamValues.get(fieldId);
			for (String paramName : namesAndValues.keySet()) {
				String paramValue = namesAndValues.get(paramName);
				CheckParam param = new CheckParam(paramName, "FIELD", check);
				param.setParamValue(paramValue);
				param.setField(field);
				checkParamRepository.save(param);
			}
		}
	}

}
