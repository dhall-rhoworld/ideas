package com.rho.rhover.common.anomaly;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.rho.rhover.common.study.DatasetVersion;

@Entity
public class DatumChange {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="datum_change_id")
	private Long datumChangeId;
	
	@ManyToOne
	@JoinColumn(name="old_datum_version_id")
	private DatumVersion oldDatumVersion;
	
	@ManyToOne
	@JoinColumn(name="new_datum_version_id")
	private DatumVersion newDatumVersion;
	
	@ManyToOne
	@JoinColumn(name="dataset_version_id")
	private DatasetVersion datasetVersion;

	public DatumChange() {
		
	}

	public Long getDatumChangeId() {
		return datumChangeId;
	}

	public void setDatumChangeId(Long datumChangeId) {
		this.datumChangeId = datumChangeId;
	}

	public DatumVersion getOldDatumVersion() {
		return oldDatumVersion;
	}

	public void setOldDatumVersion(DatumVersion oldDatumVersion) {
		this.oldDatumVersion = oldDatumVersion;
	}

	public DatumVersion getNewDatumVersion() {
		return newDatumVersion;
	}

	public void setNewDatumVersion(DatumVersion newDatumVersion) {
		this.newDatumVersion = newDatumVersion;
	}

	public DatasetVersion getDatasetVersion() {
		return datasetVersion;
	}

	public void setDatasetVersion(DatasetVersion datasetVersion) {
		this.datasetVersion = datasetVersion;
	}

}
