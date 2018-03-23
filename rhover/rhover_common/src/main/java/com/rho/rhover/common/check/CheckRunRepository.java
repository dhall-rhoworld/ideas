package com.rho.rhover.common.check;

import org.springframework.data.repository.CrudRepository;

import com.rho.rhover.common.study.DatasetVersion;
import com.rho.rhover.common.study.Field;

public interface CheckRunRepository extends CrudRepository<CheckRun, Long> {

	CheckRun findByCheckAndDatasetVersionAndFieldAndIsLatest(Check check, DatasetVersion datasetVersion, Field field, Boolean isLatest);
	
	CheckRun findByBivariateCheckAndDatasetVersionAndBivariateDatasetVersion2AndIsLatest(BivariateCheck biCheck, DatasetVersion datasetVersion,
			DatasetVersion bivariateDatasetVersion2, Boolean isLatest);
}
