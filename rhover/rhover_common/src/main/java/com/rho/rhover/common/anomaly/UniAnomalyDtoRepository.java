package com.rho.rhover.common.anomaly;

import java.util.List;

public interface UniAnomalyDtoRepository {

	List<UniAnomalyDto> findByCheckRunId(Long checkRunId);

	List<UniAnomalyDto> findByCheckRunIdAndSiteId(Long checkRunId, Long siteId);
	
	List<UniAnomalyDto> findByCheckRunIdAndSubjectId(Long checkRunId, Long subjectId);
}
