package com.rho.rhover.checker;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rho.rhover.common.check.Check;
import com.rho.rhover.common.study.Dataset;
import com.rho.rhover.common.study.DatasetVersion;
import com.rho.rhover.common.study.DatasetVersionRepository;
import com.rho.rhover.common.study.Field;

@Service
public class CheckServiceImpl implements CheckService {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private DatasetVersionRepository datasetVersionRepository;

	@Override
	@Transactional
	public void runUnivariateCheck(Check check, Dataset dataset) {
		logger.debug("Running check " + check.getCheckName() + " on dataset " + dataset.getDatasetName());
		DatasetVersion datasetVersion = datasetVersionRepository.findByDatasetAndIsCurrent(dataset, Boolean.TRUE);
		Iterable<Field> fields = datasetVersion.getFields();
		for (Field field : fields) {
			logger.info("Running univarate outlier check on field " + field.getFieldName());
		}
	}

}
