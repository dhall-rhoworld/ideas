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
			if (oldCheckParam == null || !oldCheckParam.getParamValue().equals(paramValue)) {
				CheckParam newCheckParam = new CheckParam(paramName, paramValue, "STUDY", check, userSession);
				newCheckParam.setStudy(study);
				checkParamRepository.save(newCheckParam);
				if (oldCheckParam == null) {
					Event event = Event.newNewCheckParamEvent(userSession, newCheckParam);
					eventRepository.save(event);
				}
				else {
					oldCheckParam.setIsCurrent(Boolean.FALSE);
					checkParamRepository.save(oldCheckParam);
					CheckParamChange change = new CheckParamChange(oldCheckParam, newCheckParam, userSession);
					CheckParamChangeRepository.save(change);
					Event event = Event.newModifiedCheckParamEvent(userSession, change);
					eventRepository.save(event);
				}
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
			for (CheckParam param : datasetParams) {
				param.setIsCurrent(Boolean.FALSE);
				checkParamRepository.save(param);
				Event event = Event.newDeactivateCheckParamEvent(userSession, param);
				eventRepository.save(event);
			}
		}
		else {
			List<CheckParam> globalParams = checkParamRepository.findByCheckAndParamScopeAndIsCurrent(check, "GLOBAL", Boolean.TRUE);
			Set<String> globalParamNames = new HashSet<>();
			for (CheckParam globalParam : globalParams) {
				globalParamNames.add(globalParam.getParamName());
			}
			for (String paramName : datasetParamValues.keySet()) {
				String paramValue = datasetParamValues.get(paramName);
				CheckParam oldParam = checkParamRepository.findByCheckAndDatasetAndParamNameAndIsCurrent(check, dataset, paramName, Boolean.TRUE);
				if (oldParam == null || !oldParam.getParamValue().equals(paramValue)) {
					CheckParam newParam = new CheckParam(paramName, paramValue, "DATASET", check, userSession);
					newParam.setDataset(dataset);
					checkParamRepository.save(newParam);
					if (oldParam == null) {
						Event event = Event.newNewCheckParamEvent(userSession, newParam);
						eventRepository.save(event);
					}
					else {
						oldParam.setIsCurrent(Boolean.FALSE);
						checkParamRepository.save(oldParam);
						CheckParamChange change = new CheckParamChange(oldParam, newParam, userSession);
						CheckParamChangeRepository.save(change);
						Event event = Event.newModifiedCheckParamEvent(userSession, change);
						eventRepository.save(event);
					}
				}
				globalParamNames.remove(paramName);
			}
			
			// Checkbox parameters (i.e. will not have an HTTP query parameter if 'off')
			for (String paramName : globalParamNames) {
				CheckParam oldParam = checkParamRepository.findByCheckAndDatasetAndParamNameAndIsCurrent(check, dataset, paramName, Boolean.TRUE);
				if (oldParam == null || !oldParam.getParamValue().equals("off")) {
					CheckParam newParam = new CheckParam(paramName, "off", "DATASET", check, userSession);
					newParam.setDataset(dataset);
					checkParamRepository.save(newParam);
					if (oldParam == null) {
						Event event = Event.newNewCheckParamEvent(userSession, newParam);
						eventRepository.save(event);
					}
					else {
						oldParam.setIsCurrent(Boolean.FALSE);
						checkParamRepository.save(oldParam);
						CheckParamChange change = new CheckParamChange(oldParam, newParam, userSession);
						CheckParamChangeRepository.save(change);
						Event event = Event.newModifiedCheckParamEvent(userSession, change);
						eventRepository.save(event);
					}
				}
			}
		}
		
		// Save/update skipped fields
		DatasetVersion datasetVersion = datasetVersionRepository.findByDatasetAndIsCurrent(dataset, Boolean.TRUE);
		for (Field field : datasetVersion.getFields()) {
			if (!field.getIsSkipped() && skippedFieldIds.contains(field.getFieldId())) {
				field.setIsSkipped(Boolean.TRUE);
				fieldRepository.save(field);
				Event event = Event.newAddSkipEvent(userSession, field);
				eventRepository.save(event);
			}
			else if (field.getIsSkipped() && !skippedFieldIds.contains(field.getFieldId())) {
				field.setIsSkipped(Boolean.FALSE);
				fieldRepository.save(field);
				Event event = Event.newRemoveSkipEvent(userSession, field);
				eventRepository.save(event);
			}
		}
		
		// Save/update field param values
		for (Field field : datasetVersion.getFields()) {
			List<CheckParam> fieldParams = checkParamRepository.findByCheckAndFieldAndIsCurrent(check, field, Boolean.TRUE);
			if (field.getIsSkipped()) {
				for (CheckParam param : fieldParams) {
					param.setIsCurrent(Boolean.FALSE);
					checkParamRepository.save(param);
					Event event = Event.newDeactivateCheckParamEvent(userSession, param);
					eventRepository.save(event);
				}
			}
			else {
				if (fieldParamValues.containsKey(field.getFieldId())) {
					Map<String, String> nameAndValues = fieldParamValues.get(field.getFieldId());
					for (String paramName : nameAndValues.keySet()) {
						String paramValue = nameAndValues.get(paramName);
						CheckParam oldParam = checkParamRepository.findByCheckAndFieldAndParamNameAndIsCurrent(check, field, paramName, Boolean.TRUE);
						if (oldParam == null || !oldParam.getParamValue().equals(paramValue)) {
							CheckParam newParam = new CheckParam(paramName, paramValue, "FIELD", check, userSession);
							newParam.setField(field);
							checkParamRepository.save(newParam);
							field.getCheckParams().put(paramName, newParam);
							if (oldParam == null) {
								Event event = Event.newNewCheckParamEvent(userSession, newParam);
								eventRepository.save(event);
							}
							else {
								oldParam.setIsCurrent(Boolean.FALSE);
								checkParamRepository.save(oldParam);
								CheckParamChange change = new CheckParamChange(oldParam, newParam, userSession);
								CheckParamChangeRepository.save(change);
								Event event = Event.newModifiedCheckParamEvent(userSession, change);
								eventRepository.save(event);
							}
						}
					}
				}
				else {
					for (String paramName : field.getCheckParams().keySet()) {
						CheckParam param = field.getCheckParam(paramName);
						param.setIsCurrent(Boolean.FALSE);
						checkParamRepository.save(param);
						field.getCheckParams().remove(paramName);
						fieldRepository.save(field);
						Event event = Event.newDeactivateCheckParamEvent(userSession, param);
						eventRepository.save(event);
					}
				}
			}
		}
	}

}
