package com.rho.rhover.common.anomaly;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.rho.rhover.common.study.DatasetVersion;

public interface DatumChangeRepository extends CrudRepository<DatumChange, Long> {

	List<DatumChange> findByDatasetVersion(DatasetVersion datasetVersion);
}
