package com.rho.rhover.common.session;

import org.springframework.data.repository.CrudRepository;

public interface UserSessionRepository extends CrudRepository<UserSession, Long> {

	UserSession findByWebSessionId(String webSessionId);
}
