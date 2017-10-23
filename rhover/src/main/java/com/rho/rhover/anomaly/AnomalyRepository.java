package com.rho.rhover.anomaly;

public interface AnomalyRepository {

	Iterable<Anomaly> getCurrentAnomalies(Long dataFieldId);
}
