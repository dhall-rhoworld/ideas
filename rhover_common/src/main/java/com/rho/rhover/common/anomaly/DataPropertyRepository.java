package com.rho.rhover.common.anomaly;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.rho.rhover.common.check.CheckRun;

public interface DataPropertyRepository extends CrudRepository<DataProperty, Long> {

	DataProperty findByCheckRunAndDataPropertyName(CheckRun checkRun, String dataPropertyName);
}
