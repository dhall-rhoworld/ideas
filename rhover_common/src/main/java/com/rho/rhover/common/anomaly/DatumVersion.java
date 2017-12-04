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

import com.rho.rhover.common.study.DatasetVersion;

@Entity
public class DatumVersion {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="datum_version_id")
	private Long datumVersionId;
	
	@Column(name="value")
	private String value;
	
	@Column(name="is_current")
	@Type(type="org.hibernate.type.NumericBooleanType")
	private Boolean isCurrent;
	
	@ManyToOne
	@JoinColumn(name="datum_id")
	private Datum datum;
	
	@ManyToMany
	@JoinTable(name="datum_dataset_version", joinColumns=@JoinColumn(name="datum_version_id"),
		inverseJoinColumns=@JoinColumn(name="dataset_version_id"))
	private List<DatasetVersion> datasetVersions = new ArrayList<>();

	public DatumVersion() {
		
	}

	public DatumVersion(String value, Boolean isCurrent, Datum datum) {
		super();
		this.value = value;
		this.isCurrent = isCurrent;
		this.datum = datum;
	}

	public Long getDatumVersionId() {
		return datumVersionId;
	}

	public void setDatumVersionId(Long datumVersionId) {
		this.datumVersionId = datumVersionId;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Boolean getIsCurrent() {
		return isCurrent;
	}

	public void setIsCurrent(Boolean isCurrent) {
		this.isCurrent = isCurrent;
	}

	public Datum getDatum() {
		return datum;
	}

	public void setDatum(Datum datum) {
		this.datum = datum;
	}

	public List<DatasetVersion> getDatasetVersions() {
		return datasetVersions;
	}

	public void setDatasetVersions(List<DatasetVersion> datasetVersions) {
		this.datasetVersions = datasetVersions;
	}

}
