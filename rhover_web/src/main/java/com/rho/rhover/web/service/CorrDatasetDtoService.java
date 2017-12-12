package com.rho.rhover.web.service;

import java.util.Collection;

import com.rho.rhover.common.study.Dataset;
import com.rho.rhover.common.study.Field;
import com.rho.rhover.common.study.Study;
import com.rho.rhover.web.dto.CorrDatasetDto;

public interface CorrDatasetDtoService {

	Collection<CorrDatasetDto> getCorrDatasetDtos(Study study);
	
	Collection<CorrDatasetDto> getCorrelatedFields(Field field, Dataset dataset);
}
