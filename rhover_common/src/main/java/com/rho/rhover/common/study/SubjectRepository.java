package com.rho.rhover.common.study;

import org.springframework.data.repository.CrudRepository;

public interface SubjectRepository extends CrudRepository<Subject, Long> {

	Subject findBySubjectName(String subjectName);
}
