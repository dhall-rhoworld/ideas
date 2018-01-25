package com.rho.rhover.common.study;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Phase {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="phase_id")
	private Long phaseId;
	
	@Column(name="phase_name")
	private String phaseName;
	
	@ManyToOne
	@JoinColumn(name="study_id")
	private Study study;

	public Phase() {
		
	}

	public Phase(String phaseName, Study study) {
		super();
		this.phaseName = phaseName;
		this.study = study;
	}

	public Long getPhaseId() {
		return phaseId;
	}

	public void setPhaseId(Long phaseId) {
		this.phaseId = phaseId;
	}

	public String getPhaseName() {
		return phaseName;
	}

	public void setPhaseName(String phaseName) {
		this.phaseName = phaseName;
	}

	public Study getStudy() {
		return study;
	}

	public void setStudy(Study study) {
		this.study = study;
	}

}
