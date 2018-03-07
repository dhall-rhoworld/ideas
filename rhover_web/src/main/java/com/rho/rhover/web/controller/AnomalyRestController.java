package com.rho.rhover.web.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rho.rhover.common.anomaly.Anomaly;
import com.rho.rhover.common.anomaly.AnomalyRepository;
import com.rho.rhover.common.anomaly.AnomalyRepositoryOld;
import com.rho.rhover.common.check.CheckRun;
import com.rho.rhover.common.check.CheckRunRepository;
import com.rho.rhover.common.study.CsvDataService;
import com.rho.rhover.common.study.StudyDataRepository;

@RestController
@RequestMapping("/rest/anomaly")
public class AnomalyRestController {
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private StudyDataRepository studyDataRepository;
	
	@Autowired
	private AnomalyRepository anomalyRepository;
	
	@Autowired
	private AnomalyRepositoryOld anomalyRepositoryOld;
	
	@Autowired
	private CsvDataService csvDataService;
	
	@Autowired
	private CheckRunRepository checkRunRepository;

	// TODO: I don't think this is used anymore and can be removed
	@RequestMapping("/data/univariate")
	public ResponseEntity<String> getUnivariateData(@RequestParam("data_field_id") Long dataFieldId) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "text/plain; charset=utf-8");
		String data = studyDataRepository.getUnivariateData(dataFieldId);
		return new ResponseEntity<String>(data, headers, HttpStatus.OK);
	}
	
	// TODO: Consider moving to DataController
	@RequestMapping("/data/univariate_outliers")
	public String getUnivariateOutliers(@RequestParam("check_run_id") Long checkRunId) {
		CheckRun checkRun = checkRunRepository.findOne(checkRunId);
		return csvDataService.getCsvData(checkRun);
	}
	
	// TODO: I don't think this is used anymore and can be removed
	@RequestMapping("/data/bivariate")
	public ResponseEntity<String> getBivariateData(@RequestParam("bivariate_check_id") Long bivariateCheckId) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "text/plain; charset=utf-8");
		String data = studyDataRepository.getBivariateData(bivariateCheckId);
		return new ResponseEntity<String>(data, headers, HttpStatus.OK);
	}
	
	// TODO: May be able to consolidate with getUnivariateOutliers
	@RequestMapping("/data/bivariate_outliers")
	public String getBivariteOutliers(@RequestParam("check_run_id") Long checkRunId) {
		CheckRun checkRun = checkRunRepository.findOne(checkRunId);
		return csvDataService.getCsvData(checkRun);
	}
	
	@RequestMapping("/not_an_issue")
	public ResponseEntity<Integer> notAnIssue(@RequestParam("anomaly_ids") String anomalyIds) {
		logger.info("Not an issue");
		String[] ids = anomalyIds.split(",");
		for (String id : ids) {
			Anomaly anomaly = anomalyRepository.findOne(new Long(id));
			anomaly.setIsAnIssue(Boolean.FALSE);
			anomalyRepository.save(anomaly);
		}
		return new ResponseEntity<Integer>(ids.length, HttpStatus.OK);
	}
	
	// TODO: Complete functionality so that inliers can marked as issues in database.
	@RequestMapping("/is_an_issue")
	public ResponseEntity<Integer> isAnIssue(
			@RequestParam("data_field_id") Long dataFieldId,
			@RequestParam("recruit_ids") String recruitIdString,
			@RequestParam("events") String eventString) {
		String[] recruitIds = recruitIdString.split(",");
		String[] events = eventString.replaceAll("%26", "&").split(",");
		anomalyRepositoryOld.setIsAnIssue(dataFieldId, recruitIds, events, true);
		return new ResponseEntity<Integer>(0, HttpStatus.OK);
	}
	
	@RequestMapping(value="/set_univariate_sd", method=RequestMethod.POST)
	public ResponseEntity<Integer> setUnivariateSd(
			@RequestParam("check_run_id") Long checkRunId,
			@RequestParam("num_sd") Double numSd) {
		logger.info("checkRunId: " + checkRunId + ", numSd: " + numSd);
		return new ResponseEntity<Integer>(0, HttpStatus.OK);
	}
	
}
