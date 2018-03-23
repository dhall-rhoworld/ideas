package com.rho.rhover.web.query;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class QueryStatus {
	
	@Id
	@Column(name="query_status_id")
	private Long queryStatusId;
	
	@Column(name="query_status_name")
	private String queryStatusName;
	
	@Column(name="query_status_label")
	private String queryStatusLabel;

	public QueryStatus() {
		
	}

	public Long getQueryStatusId() {
		return queryStatusId;
	}

	public void setQueryStatusId(Long queryStatusId) {
		this.queryStatusId = queryStatusId;
	}

	public String getQueryStatusLabel() {
		return queryStatusLabel;
	}

	public void setQueryStatusLabel(String queryStatusLabel) {
		this.queryStatusLabel = queryStatusLabel;
	}

	public String getQueryStatusName() {
		return queryStatusName;
	}

	public void setQueryStatusName(String queryStatusName) {
		this.queryStatusName = queryStatusName;
	}

}
