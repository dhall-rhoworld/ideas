package com.rho.rhover.web.controller;

import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rho.rhover.common.anomaly.Anomaly;
import com.rho.rhover.common.anomaly.AnomalyRepository;
import com.rho.rhover.web.query.QueryCandidate;
import com.rho.rhover.web.query.QueryCandidateRepository;

@RestController
@RequestMapping("/rest/query")
public class QueryRestController {
	
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private QueryCandidateRepository queryCandidateRepository;
	
	@Autowired
	private AnomalyRepository anomalyRepository;

	@RequestMapping(value="/add", method=RequestMethod.POST)
	public ResponseEntity<Integer> addQueryCandidates(@RequestParam("anomaly_ids") String anomalyIds, Principal user) {
		String[] tokens = anomalyIds.split(",");
		for (String token : tokens) {
			Long anomalyId = new Long(token);
			Anomaly anomaly = anomalyRepository.findOne(anomalyId);
			QueryCandidate qc = queryCandidateRepository.findByAnomaly(anomaly);
			if (qc == null) {
				qc = new QueryCandidate();
				qc.setAnomaly(anomaly);
				qc.setCreatedBy(user.getName());
			}
			qc.setIsActive(Boolean.TRUE);
			queryCandidateRepository.save(qc);
		}
		return new ResponseEntity<>(tokens.length, HttpStatus.OK);
	}
}
