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
import com.rho.rhover.web.query.QueryStatus;
import com.rho.rhover.web.query.QueryStatusRepository;

@RestController
@RequestMapping("/rest/query")
public class QueryRestController {
	
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private QueryCandidateRepository queryCandidateRepository;
	
	@Autowired
	private AnomalyRepository anomalyRepository;
	
	@Autowired
	private QueryStatusRepository queryStatusRepository;

	@RequestMapping(value="/add", method=RequestMethod.POST)
	public ResponseEntity<Integer> addQueryCandidates(@RequestParam("anomaly_ids") String anomalyIds, Principal user) {
		QueryStatus queryStatus = queryStatusRepository.findByQueryStatusName("NOT-OPENED");
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
			qc.setQueryStatus(queryStatus);
			queryCandidateRepository.save(qc);
		}
		return new ResponseEntity<>(tokens.length, HttpStatus.OK);
	}
	
	@RequestMapping(value="/set_status", method=RequestMethod.POST)
	public ResponseEntity<Integer> setStatus(
			@RequestParam("query_candidate_id") Long queryCandidateId,
			@RequestParam("query_status_id") Long queryStatusId) {
		QueryCandidate qc = queryCandidateRepository.findOne(queryCandidateId);
		QueryStatus qs = queryStatusRepository.findOne(queryStatusId);
		qc.setQueryStatus(qs);
		queryCandidateRepository.save(qc);
		return new ResponseEntity<Integer>(1, HttpStatus.OK);
	}
}
