package com.rho.rhover.web.query;

import org.springframework.data.repository.CrudRepository;

public interface QueryStatusRepository extends CrudRepository<QueryStatus, Long> {

	QueryStatus findByQueryStatusName(String queryStatusName);
}
