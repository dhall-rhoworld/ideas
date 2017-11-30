package com.rho.rhover.common.check;

import org.springframework.data.repository.CrudRepository;

public interface ParamUsedRepository extends CrudRepository<ParamUsed, Long> {

	ParamUsed findByCheckRunAndParamName(CheckRun checkRun, String paramName);
}
