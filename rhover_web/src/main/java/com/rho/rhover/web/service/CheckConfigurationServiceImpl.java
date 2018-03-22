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
import com.rho.rhover.common.check.CheckParamChange;
import com.rho.rhover.common.check.CheckParamChangeRepository;
import com.rho.rhover.common.check.CheckParamRepository;
import com.rho.rhover.common.session.Event;
import com.rho.rhover.common.session.EventRepository;
import com.rho.rhover.common.session.UserSession;
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
	
	@Autowired
	private CheckParamChangeRepository CheckParamChangeRepository;
	
	@Autowired
	private EventRepository eventRepository;

	@Override
	@Transactional
	public void saveStudyCheckConfiguration(Study study, Check check, Map<String, String> params,
			Collection<Long> checkedDatasetIds, UserSession userSession) {
		
		// Save check parameters
		for (String paramName : params.keySet()) {
			String paramValue = params.get(paramName);
			CheckParam oldCheckParam = checkParamRepository.findByCheckAndStudyAndParamNameAndIsCurrent(check, study, paramName, Boolean.TRUE);
			CheckParam newCheckParam = new CheckParam(paramName, paramValue, "STUDY", check, userSession);
			newCheckParam.setStudy(study);
			checkParamRepository.save(newCheckParam);
			if (oldCheckParam != null) {
				oldCheckParam.setIsCurrent(Boolean.FALSE);
				checkParamRepository.save(oldCheckParam);
				CheckParamChange change = new CheckParamChange(oldCheckParam, newCheckParam, userSession);
				CheckParamChangeRepository.save(change);
				Event event = Event.newModifiedCheckParamEvent(userSession, change);
				eventRepository.save(event);
			}
			else {
				Event event = Event.newNewCheckParamEvent(userSession, newCheckParam);
				eventRepository.save(event);
			}
		}
		
		// Save datasets to be checked
		Iterable<Dataset> datasets = datasetRepository.findByStudy(study);
		for (Dataset dataset : datasets) {
			if (!dataset.getIsChecked() && checkedDatasetIds.contains(dataset.getDatasetId())) {
				dataset.setIsChecked(Boolean.TRUE);
				datasetRepository.save(dataset);
				Event event = Event.newAddDatasetCheckEvent(userSession, dataset);
				eventRepository.save(event);
			}
			else if (dataset.getIsChecked() && !checkedDatasetIds.contains(dataset.getDatasetId())) {
				dataset.setIsChecked(Boolean.FALSE);
				datasetRepository.save(dataset);
				Event event = Event.newRemoveDatasetCheckEvent(userSession, dataset);
				eventRepository.save(event);
			}
		}
	}

	@Override
	@Transactional
	public void saveDatasetCheckConfiguration(Dataset dataset, Check check, Boolean useStudyDefaults,
			Map<String, String> datasetParamValues, Map<Long, Map<String, String>> fieldParamValues,
			Collection<Long> skippedFieldIds, UserSession userSession) {
		
		// Save/update/delete dataset-level check parameters
		List<CheckParam> datasetParams = checkParamRepository.findByCheckAndDatasetAndIsCurrent(check, dataset, Boolean.TRUE);
		if (useStudyDefaults) {
			checkParamRepository.delete(datasetParams);
		}
		else {
			List<CheckParam> globalParams = checkParamRepository.findByCheckAndParamScopeAndIsCurrent(check, "GLOBAL", Boolean.TRUE);
			Set<String> globalParamNames = new HashSet<>();
			for (CheckParam globalParam : globalParams) {
				globalParamNames.add(globalParam.getParamName());
			}
			for (String paramName : datasetParamValues.keySet()) {
				String paramValue = datasetParamValues.get(paramName);
				CheckParam param = checkParamRepository.findByCheckAndDatasetAndParamNameAndIsCurrent(check, dataset, paramName, Boolean.TRUE);
				if (param == null) {
					param = new CheckParam(paramName, "DATASET", check, userSession);
					param.setDataset(dataset);
				}
				param.setParamValue(paramValue);
				checkParamRepository.save(param);
				globalParamNames.remove(paramName);
			}
			for (String paramName : globalParamNames) {
				CheckParam param = checkParamRepository.findByCheckAndDatasetAndParamNameAndIsCurrent(check, dataset, paramName, Boolean.TRUE);
				if (param == null) {
					param = new CheckParam(paramName, "DATASET", check, userSession);
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
			List<CheckParam> fieldParams = checkParamRepository.findByCheckAndFieldAndIsCurrent(check, field, Boolean.TRUE);
			for (CheckParam param : fieldParams) {
				field.getCheckParams().remove(param.getParamName());
			}
			fieldRepository.save(field);
			checkParamRepository.delete(fieldParams);
		}
		
		for (Long fieldId : fieldParamValues.keySet()) {
			Field field = fieldRepository.findOne(fieldId);
			Map<String, String> namesAndValues = fieldParamValues.get(fieldId);
			for (String paramName : namesAndValues.keySet()) {
				String paramValue = namesAndValues.get(paramName);
				CheckParam param = new CheckParam(paramName, "FIELD", check, userSession);
				param.setParamValue(paramValue);
				param.setField(field);
				checkParamRepository.save(param);
				field.getCheckParams().put(paramName, param);
			}
			fieldRepository.save(field);
		}
	}

}
