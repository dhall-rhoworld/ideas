package com.rho.rhover.web.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rho.rhover.common.check.BivariateCheck;
import com.rho.rhover.common.check.BivariateCheckRepository;
import com.rho.rhover.common.check.Correlation;
import com.rho.rhover.common.check.CorrelationRepository;
import com.rho.rhover.common.study.Dataset;
import com.rho.rhover.common.study.Field;
import com.rho.rhover.common.study.FieldInstance;
import com.rho.rhover.common.study.Study;
import com.rho.rhover.web.dto.CorrDatasetDto;
import com.rho.rhover.web.dto.CorrFieldDto;

@Service
public class CorrDatasetDtoServiceImpl implements CorrDatasetDtoService {
	
	private static final int DISPLAY_LENGTH = 30;
	
	@Autowired
	private CorrelationRepository correlationRepository;
	
	@Autowired
	private BivariateCheckRepository bivariateCheckRepository;

	@Override
	public Collection<CorrDatasetDto> getCorrDatasetDtos(Study study) {
		List<BivariateCheck> checks = bivariateCheckRepository.findByStudy(study);
		Map<Long, Set<String>> checkMap = new HashMap<>();
		for (BivariateCheck check : checks) {
			Long sourceId = check.getxFieldInstance().getFieldInstanceId();
			Long targetId = check.getyFieldInstance().getFieldInstanceId();
			Set<String> targetIds = checkMap.get(sourceId);
			if (targetIds == null) {
				targetIds = new HashSet<>();
				checkMap.put(sourceId, targetIds);
			}
			targetIds.add(targetId.toString());
		}
		List<Correlation> correlations = correlationRepository.findByStudy(study);
		Map<String, CorrDatasetDto> dtoMap = new HashMap<>();
		for (Correlation correlation : correlations) {
			if (correlation.getCoefficient() > 0.6) {
				FieldInstance fieldInstance1 = correlation.getFieldInstance1();
				FieldInstance fieldInstance2 = correlation.getFieldInstance2();
				Dataset dataset1 = correlation.getFieldInstance1().getDataset();
				Dataset dataset2 = correlation.getFieldInstance2().getDataset();
				updateDtoMap(dtoMap, dataset1, fieldInstance1, fieldInstance2, checkMap);
				updateDtoMap(dtoMap, dataset2, fieldInstance2, fieldInstance1, checkMap);
			}
		}
		return dtoMap.values();
	}

	private void updateDtoMap(Map<String, CorrDatasetDto> dtoMap, Dataset dataset, FieldInstance fieldInstance1, FieldInstance
			fieldInstance2, Map<Long, Set<String>> checkMap) {
		CorrDatasetDto dto = dtoMap.get(dataset.getDatasetName());
		if (dto == null) {
			dto = new CorrDatasetDto();
			dto.setDatasetId(dataset.getDatasetId().toString());
			dto.setDatasetName(dataset.getDatasetName());
			dtoMap.put(dataset.getDatasetName(), dto);
		}
		List<CorrFieldDto> fieldDtos = dto.getFields();
		CorrFieldDto fieldDto = null;
		for (CorrFieldDto d : fieldDtos) {
			if (d.getFieldInstanceId().equals(fieldInstance1.getFieldInstanceId().toString())) {
				fieldDto = d;
				break;
			}
		}
		if (fieldDto == null) {
			fieldDto = new CorrFieldDto();
			fieldDto.setFieldName(fieldInstance1.getField().getTruncatedDisplayName(DISPLAY_LENGTH));
			fieldDto.setFieldInstanceId(fieldInstance1.getFieldInstanceId().toString());
			fieldDto.setFieldLabel(fieldInstance1.getField().getFieldLabel());
			Set<String> targetIds = checkMap.get(fieldInstance1.getFieldInstanceId());
			if (targetIds != null) {
				fieldDto.setCheckFieldInstanceIds(targetIds);
			}
			fieldDtos.add(fieldDto);
		}
		fieldDto.getCorrelatedFieldInstanceIds().add(fieldInstance2.getFieldInstanceId().toString());
	}

	@Override
	public Collection<CorrDatasetDto> getCorrelatedFields(Field field, Dataset dataset) {
		// TODO Auto-generated method stub
		return null;
	}
}
