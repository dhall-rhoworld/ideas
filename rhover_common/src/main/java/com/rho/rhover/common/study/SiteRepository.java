package com.rho.rhover.common.study;

import org.springframework.data.repository.CrudRepository;

public interface SiteRepository extends CrudRepository<Site, Long> {

	Site findByStudyAndSiteName(Study study, String siteName);
}
