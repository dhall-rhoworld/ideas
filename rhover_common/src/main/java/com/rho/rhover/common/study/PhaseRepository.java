package com.rho.rhover.common.study;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface PhaseRepository extends CrudRepository<Phase, Long> {

	Phase findByPhaseName(String phaseName);

	List<Phase> findByStudy(Study study);

}
