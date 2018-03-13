package com.rho.rhover.common.anomaly;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.rho.rhover.common.study.Dataset;
import com.rho.rhover.common.study.DatasetVersion;
import com.rho.rhover.common.study.Phase;
import com.rho.rhover.common.study.Site;
import com.rho.rhover.common.study.Subject;

@Entity
public class Observation {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="observation_id")
	private Long observationId;
	
	@ManyToOne
	@JoinColumn(name="dataset_id")
	private Dataset dataset;
	
	@ManyToOne
	@JoinColumn(name="site_id")
	private Site site;
	
	@ManyToOne
	@JoinColumn(name="subject_id")
	private Subject subject;
	
	@ManyToOne
	@JoinColumn(name="phase_id")
	private Phase phase;
	
	@Column(name="record_id")
	private String recordId;
	
	@ManyToOne
	@JoinColumn(name="first_dataset_version_id")
	private DatasetVersion firstDatasetVersion;

	public Observation() {
		
	}

	public Observation(Dataset dataset, Subject subject, Site site, Phase phase, String recordId) {
		super();
		this.dataset = dataset;
		this.subject = subject;
		this.site = site;
		this.phase = phase;
		this.recordId = recordId;
	}

	public Long getObservationId() {
		return observationId;
	}

	public void setObservationId(Long observationId) {
		this.observationId = observationId;
	}

	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	public Subject getSubject() {
		return subject;
	}

	public void setSubject(Subject subject) {
		this.subject = subject;
	}

	public Phase getPhase() {
		return phase;
	}

	public void setPhase(Phase phase) {
		this.phase = phase;
	}

	public String getRecordId() {
		return recordId;
	}

	public void setRecordId(String recordId) {
		this.recordId = recordId;
	}

	public Site getSite() {
		return site;
	}

	public void setSite(Site site) {
		this.site = site;
	}

	public DatasetVersion getFirstDatasetVersion() {
		return firstDatasetVersion;
	}

	public void setFirstDatasetVersion(DatasetVersion firstDatasetVersion) {
		this.firstDatasetVersion = firstDatasetVersion;
	}

}
