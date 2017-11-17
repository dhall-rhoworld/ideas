package com.rho.rhover.common.study;

import org.springframework.data.repository.CrudRepository;

public interface DataStreamRepository extends CrudRepository<DataStream, Long> {

	public DataStream findByStudyAndDataStreamName(Study study, String dataStreamName);
}
