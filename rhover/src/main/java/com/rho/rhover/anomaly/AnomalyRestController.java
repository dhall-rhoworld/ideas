package com.rho.rhover.anomaly;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rho.rhover.study.StudyDataRepository;

@RestController
@RequestMapping("/rest/anomaly")
public class AnomalyRestController {
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private StudyDataRepository studyDataRepository;

	@RequestMapping("data")
	public ResponseEntity<String> getData(@RequestParam("data_field_id") Long dataFieldId) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "text/plain; charset=utf-8");
		String data = studyDataRepository.getAllDataFieldValues(dataFieldId);
		logger.debug(data);
		return new ResponseEntity<String>(data, headers, HttpStatus.OK);
	}
}
