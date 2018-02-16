package com.rho.rhover.common.study;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface PhaseRepository extends CrudRepository<Phase, Long> {

	Phase findByPhaseName(String phaseName);

	List<Phase> findByStudy(Study study);

	@Query("select distinct o.phase from Observation o where o.dataset = ?1")
	List<Phase> findByDataset(Dataset dataset);
}
