package com.rho.rhover.common.anomaly;

import org.springframework.data.repository.CrudRepository;

public interface DatumVersionRepository extends CrudRepository<DatumVersion, Long> {

	DatumVersion findByDatumAndIsCurrent(Datum datum, Boolean isCurrent);
}
