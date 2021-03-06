package com.rho.rhover.common.study;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface SubjectRepository extends CrudRepository<Subject, Long> {

	@Query("select s from Subject s where s.subjectName = ?1 and s.site.study = ?2")
	Subject findBySubjectNameAndStudy(String subjectName, Study study);

	@Query("select s from Subject s where s.site.study = ?1")
	List<Subject> findByStudy(Study study);
}
