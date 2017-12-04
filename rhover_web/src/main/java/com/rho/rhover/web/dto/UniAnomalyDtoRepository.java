package com.rho.rhover.web.dto;

import java.util.List;

public interface UniAnomalyDtoRepository {

	List<UniAnomalyDto> findByCheckRunId(Long checkRunId);
}
