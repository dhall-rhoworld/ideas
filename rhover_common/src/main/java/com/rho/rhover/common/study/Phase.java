package com.rho.rhover.common.study;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
public class Phase implements Comparable<Phase> {
	
	@Transient
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Transient
	private final Pattern numberPattern = Pattern.compile("[0-9]+");
	
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

	@Override
	public int compareTo(Phase other) {
		Matcher matcher = numberPattern.matcher(this.phaseName);
		boolean thisIsNumeric = matcher.find();
		matcher = numberPattern.matcher(other.phaseName);
		boolean otherIsNumeric = matcher.find();
		int value = 0;
		if (thisIsNumeric && !otherIsNumeric) {
			value = -1;
		}
		else if (!thisIsNumeric && otherIsNumeric) {
			value = 1;
		}
		else {
			value = this.phaseName.compareTo(other.phaseName);
		}
		return value;
	}

}
