package com.rho.rhover.common.anomaly;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import com.rho.rhover.common.check.Check;
import com.rho.rhover.common.check.CheckRun;

@Entity
public class Anomaly {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="anomaly_id")
	private Long anomalyId;
	
	@ManyToMany
	@JoinTable(name="anomaly_check_run", joinColumns = @JoinColumn(name="anomaly_id"),
	inverseJoinColumns = @JoinColumn(name="check_run_id"))
	private List<CheckRun> checkRuns = new ArrayList<>();
	
	@ManyToMany
	@JoinTable(name="anomaly_datum_version", joinColumns = @JoinColumn(name="anomaly_id"),
	inverseJoinColumns = @JoinColumn(name="datum_version_id"))
	private List<DatumVersion> datumVersions = new ArrayList<>();
	
	@ManyToOne
	@JoinColumn(name="check_id")
	private Check check;

	public Anomaly() {
		
	}

	public Long getAnomalyId() {
		return anomalyId;
	}

	public void setAnomalyId(Long anomalyId) {
		this.anomalyId = anomalyId;
	}

	public List<DatumVersion> getDatumVersions() {
		return datumVersions;
	}

	public void setDatumVersions(List<DatumVersion> datumVersions) {
		this.datumVersions = datumVersions;
	}

	public List<CheckRun> getCheckRuns() {
		return checkRuns;
	}

	public void setCheckRuns(List<CheckRun> checkRuns) {
		this.checkRuns = checkRuns;
	}

	public Check getCheck() {
		return check;
	}

	public void setCheck(Check check) {
		this.check = check;
	}

}
