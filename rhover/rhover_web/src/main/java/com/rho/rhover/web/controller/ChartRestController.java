package com.rho.rhover.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rho.rhover.common.check.Check;
import com.rho.rhover.common.check.CheckRepository;
import com.rho.rhover.common.check.CheckRun;
import com.rho.rhover.common.check.CheckRunRepository;
import com.rho.rhover.common.study.CsvDataService;
import com.rho.rhover.common.study.DatasetVersion;
import com.rho.rhover.common.study.Field;
import com.rho.rhover.common.study.FieldInstance;
import com.rho.rhover.common.study.FieldInstanceRepository;
import com.rho.rhover.common.study.CsvDataService.HeaderOption;

@RestController
@RequestMapping("/rest/chart")
public class ChartRestController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private CsvDataService csvDataService;
	
	@Autowired
	private CheckRunRepository checkRunRepository;
	
	@Autowired
	private CheckRepository checkRepository;
	
	@Autowired
	private FieldInstanceRepository fieldInstanceRepository;

	@RequestMapping("/univariate_data")
	public String getUnivariateData(@RequestParam("field_instance_id") Long fieldInstanceId) {
		logger.info("Fetching data for field instance: " + fieldInstanceId);
		FieldInstance fieldInstance = fieldInstanceRepository.findOne(fieldInstanceId);
		Field field = fieldInstance.getField();
		DatasetVersion datasetVersion = field.getCurrentDatasetVersion(fieldInstance.getDataset());
		Check check = checkRepository.findByCheckName("UNIVARIATE_OUTLIER");
    	CheckRun checkRun = checkRunRepository.findByCheckAndDatasetVersionAndFieldAndIsLatest(check, datasetVersion, field, Boolean.TRUE);
    	String data = null;
    	if (checkRun != null) {
    		data = csvDataService.getCsvData(checkRun);
    	}
    	else {
    		data = csvDataService.getCurrentDataAndIdFieldsAsCsv(fieldInstance, HeaderOption.FIELD_DISPLAY_NAMES);
    	}
		return data;
	}

}
