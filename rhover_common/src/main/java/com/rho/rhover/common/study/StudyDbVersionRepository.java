package com.rho.rhover.common.study;

import org.springframework.data.repository.CrudRepository;

public interface StudyDbVersionRepository extends CrudRepository<StudyDbVersion, Long> {

	StudyDbVersion findByStudyAndIsCurrent(Study study, Boolean isCurrent);
}
