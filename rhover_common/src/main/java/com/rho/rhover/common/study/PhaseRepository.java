package com.rho.rhover.common.study;

import org.springframework.data.repository.CrudRepository;

public interface PhaseRepository extends CrudRepository<Phase, Long> {

	Phase findByPhaseName(String phaseName);

}
