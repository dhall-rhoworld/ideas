package com.rho.rhover.web.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rho.rhover.common.check.Correlation;
import com.rho.rhover.common.check.CorrelationRepository;
import com.rho.rhover.common.study.Dataset;
import com.rho.rhover.common.study.Field;
import com.rho.rhover.common.study.Study;
import com.rho.rhover.web.dto.CorrDatasetDto;
import com.rho.rhover.web.dto.CorrFieldDto;

@Service
public class CorrDatasetDtoServiceImpl implements CorrDatasetDtoService {
	
	private static final int DISPLAY_LENGTH = 30;
	
	@Autowired
	private CorrelationRepository correlationRepository;

	@Override
	public Collection<CorrDatasetDto> getCorrDatasetDtos(Study study) {
		List<Correlation> correlations = correlationRepository.findByStudy(study);
		Map<String, CorrDatasetDto> dtoMap = new HashMap<>();
		for (Correlation correlation : correlations) {
			if (correlation.getCoefficient() > 0.6) {
				Field field1 = correlation.getFieldInstance1().getField();
				Field field2 = correlation.getFieldInstance2().getField();
				Dataset dataset1 = correlation.getFieldInstance1().getDataset();
				Dataset dataset2 = correlation.getFieldInstance2().getDataset();
				updateDtoMap(dtoMap, dataset1, field1, field2);
				updateDtoMap(dtoMap, dataset2, field2, field1);
			}
		}
		return dtoMap.values();
	}

	private void updateDtoMap(Map<String, CorrDatasetDto> dtoMap, Dataset dataset, Field field1, Field field2) {
		CorrDatasetDto dto = dtoMap.get(dataset.getDatasetName());
		if (dto == null) {
			dto = new CorrDatasetDto();
			dto.setDatasetName(dataset.getDatasetName());
			dtoMap.put(dataset.getDatasetName(), dto);
		}
		List<CorrFieldDto> fieldDtos = dto.getFields();
		CorrFieldDto fieldDto = null;
		for (CorrFieldDto d : fieldDtos) {
			if (d.getFieldId().equals(field1.getFieldId().toString())) {
				fieldDto = d;
				break;
			}
		}
		if (fieldDto == null) {
			fieldDto = new CorrFieldDto();
			fieldDto.setFieldName(field1.getTruncatedDisplayName(DISPLAY_LENGTH));
			fieldDto.setFieldId(field1.getFieldId().toString());
			fieldDtos.add(fieldDto);
		}
		fieldDto.getCorrelatedFields().add(field2.getFieldId().toString());
	}
}
