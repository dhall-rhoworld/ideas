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

import org.hibernate.annotations.Type;

import com.rho.rhover.common.check.Check;
import com.rho.rhover.common.check.CheckRun;
import com.rho.rhover.common.study.Field;
import com.rho.rhover.common.study.Phase;
import com.rho.rhover.common.study.Site;
import com.rho.rhover.common.study.Subject;

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
	
	@ManyToMany
	@JoinTable(name="anomaly_datum_version_2", joinColumns = @JoinColumn(name="anomaly_id"),
	inverseJoinColumns = @JoinColumn(name="datum_version_id"))
	private List<DatumVersion> bivariateDatumVersions2 = new ArrayList<>();
	
	@ManyToOne
	@JoinColumn(name="check_id")
	private Check check;
	
	@ManyToOne
	@JoinColumn(name="subject_id")
	private Subject subject;
	
	@ManyToOne
	@JoinColumn(name="site_id")
	private Site site;
	
	@ManyToOne
	@JoinColumn(name="field_id")
	private Field field;
	
	@ManyToOne
	@JoinColumn(name="phase_id")
	private Phase phase;
	
	@Column(name="record_id")
	private String recordId;
	
	@Column(name="has_been_viewed")
	@Type(type="org.hibernate.type.NumericBooleanType")
	private Boolean hasBeenViewed = Boolean.FALSE;
	
	@Column(name="is_an_issue")
	@Type(type="org.hibernate.type.NumericBooleanType")
	private Boolean isAnIssue = Boolean.TRUE;

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

	public Boolean getHasBeenViewed() {
		return hasBeenViewed;
	}

	public void setHasBeenViewed(Boolean hasBeenViewed) {
		this.hasBeenViewed = hasBeenViewed;
	}

	public Boolean getIsAnIssue() {
		return isAnIssue;
	}

	public void setIsAnIssue(Boolean isAnIssue) {
		this.isAnIssue = isAnIssue;
	}
	
	public Subject getSubject() {
		return subject;
	}

	public void setSubject(Subject subject) {
		this.subject = subject;
	}
	
	public Site getSite() {
		return site;
	}

	public void setSite(Site site) {
		this.site = site;
	}

	public Field getField() {
		return field;
	}

	public void setField(Field field) {
		this.field = field;
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

	public List<DatumVersion> getBivariateDatumVersions2() {
		return bivariateDatumVersions2;
	}

	public void setBivariateDatumVersions2(List<DatumVersion> bivariateDatumVersions2) {
		this.bivariateDatumVersions2 = bivariateDatumVersions2;
	}

	public DatumVersion getCurrentDatumVersion() {
		DatumVersion current = null;
		for (DatumVersion version : datumVersions) {
			if (version.getIsCurrent()) {
				current = version;
				break;
			}
		}
		return current;
	}
}
