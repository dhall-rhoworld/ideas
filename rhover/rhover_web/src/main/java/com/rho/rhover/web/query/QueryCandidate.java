package com.rho.rhover.web.query;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.rho.rhover.common.anomaly.Anomaly;
import com.rho.rhover.common.study.Study;

@Entity
@Table(name="query_candidate")
public class QueryCandidate {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="query_candidate_id")
	private Long queryCandidateId;
	
	@ManyToOne
	@JoinColumn(name="anomaly_id")
	private Anomaly anomaly;
	
	@Column(name="created_on")
	private Date createdOn;
	
	@Column(name="created_by")
	private String createdBy;
	
	@ManyToOne
	@JoinColumn(name="query_status_id")
	private QueryStatus queryStatus;

	public QueryCandidate() {
		
	}

	public Long getQueryCandidateId() {
		return queryCandidateId;
	}

	public void setQueryCandidateId(Long queryCandidateId) {
		this.queryCandidateId = queryCandidateId;
	}

	public Anomaly getAnomaly() {
		return anomaly;
	}

	public void setAnomaly(Anomaly anomaly) {
		this.anomaly = anomaly;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public QueryStatus getQueryStatus() {
		return queryStatus;
	}

	public void setQueryStatus(QueryStatus queryStatus) {
		this.queryStatus = queryStatus;
	}
	
}
