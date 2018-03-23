package com.rho.rhover.common.study;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface SiteRepository extends CrudRepository<Site, Long> {

	Site findByStudyAndSiteName(Study study, String siteName);

	List<Site> findByStudy(Study study);
}
