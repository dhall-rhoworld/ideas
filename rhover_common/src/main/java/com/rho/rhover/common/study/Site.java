package com.rho.rhover.common.study;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Site {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="site_id")
	private Long siteId;
	
	@Column(name="site_name")
	private String siteName;
	
	@ManyToOne
	@JoinColumn(name="study_id")
	private Study study;
	
	public Site() {
		
	}

	public Long getSiteId() {
		return siteId;
	}

	public void setSiteId(Long siteId) {
		this.siteId = siteId;
	}

	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	public Study getStudy() {
		return study;
	}

	public void setStudy(Study study) {
		this.study = study;
	}
}
