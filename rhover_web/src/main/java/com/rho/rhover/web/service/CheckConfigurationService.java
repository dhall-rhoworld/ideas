package com.rho.rhover.web.service;

import java.util.Collection;
import java.util.Map;

import com.rho.rhover.common.check.Check;
import com.rho.rhover.common.study.Study;

public interface CheckConfigurationService {

	void saveStudyCheckConfiguration(Study study, Check check,
			Map<String, String> params, Collection<Long> checkedDatasetIds);
}
