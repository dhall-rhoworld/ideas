package com.rho.rhover.common.study;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface LoaderIssueRepository extends CrudRepository<LoaderIssue, Long> {

	List<LoaderIssue> findByStudyDbVersion(StudyDbVersion studyDbVersion);
}
