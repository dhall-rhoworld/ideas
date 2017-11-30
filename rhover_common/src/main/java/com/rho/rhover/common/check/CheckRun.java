package com.rho.rhover.common.check;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Type;

import com.rho.rhover.common.study.DatasetVersion;
import com.rho.rhover.common.study.Field;

@Entity
public class CheckRun {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="check_run_id")
	private Long checkRunId;
	
	@ManyToOne
	@JoinColumn(name="dataset_version_id")
	private DatasetVersion datasetVersion;
	
	@ManyToOne
	@JoinColumn(name="check_id")
	private Check check;
	
	@ManyToOne
	@JoinColumn(name="field_id")
	private Field field;
	
	@Column(name="is_latest")
	@Type(type="org.hibernate.type.NumericBooleanType")
	private Boolean isLatest;

	public CheckRun() {
		
	}
	
	public CheckRun(DatasetVersion datasetVersion, Check check, Field field, Boolean isLatest) {
		super();
		this.datasetVersion = datasetVersion;
		this.check = check;
		this.field = field;
		this.isLatest = isLatest;
	}

	public Long getCheckRunId() {
		return checkRunId;
	}

	public void setCheckRunId(Long checkRunId) {
		this.checkRunId = checkRunId;
	}

	public DatasetVersion getDatasetVersion() {
		return datasetVersion;
	}

	public void setDatasetVersion(DatasetVersion datasetVersion) {
		this.datasetVersion = datasetVersion;
	}

	public Check getCheck() {
		return check;
	}

	public void setCheck(Check check) {
		this.check = check;
	}

	public Field getField() {
		return field;
	}

	public void setField(Field field) {
		this.field = field;
	}

	public Boolean getIsLatest() {
		return isLatest;
	}

	public void setIsLatest(Boolean isLatest) {
		this.isLatest = isLatest;
	}

}
