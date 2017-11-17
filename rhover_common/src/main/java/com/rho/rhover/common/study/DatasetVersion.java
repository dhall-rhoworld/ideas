package com.rho.rhover.common.study;

import java.util.HashSet;
import java.util.Set;

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

@Entity
public class DatasetVersion {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="dataset_version_id")
	private Long datasetVersionId;
	
	@Column(name="dataset_version_name")
	private String datasetVersionName;
	
	@Column(name="is_current")
	@Type(type="org.hibernate.type.NumericBooleanType")
	private Boolean isCurrent = Boolean.FALSE;
	
	@ManyToOne
	@JoinColumn(name="dataset_id")
	private Dataset dataset;
	
	@ManyToMany
	@JoinTable(name="dataset_version_stream", joinColumns = @JoinColumn(name="dataset_id"),
			inverseJoinColumns = @JoinColumn(name="data_stream_id"))
	private Set<DataStream> dataStreams = new HashSet<>();
	
	@ManyToMany
	@JoinTable(name="dataset_version_field", joinColumns = @JoinColumn(name="dataset_version_id"),
			inverseJoinColumns = @JoinColumn(name="field_id"))
	private Set<Field> fields = new HashSet<>();
	
	public DatasetVersion() {
		
	}
	
	public DatasetVersion(String datasetVersionName, Boolean isCurrent, Dataset dataset) {
		super();
		this.datasetVersionName = datasetVersionName;
		this.isCurrent = isCurrent;
		this.dataset = dataset;
	}

	public Long getDatasetVersionId() {
		return datasetVersionId;
	}

	public void setDatasetVersionId(Long datasetVersionId) {
		this.datasetVersionId = datasetVersionId;
	}

	public String getDatasetVersionName() {
		return datasetVersionName;
	}

	public void setDatasetVersionName(String datasetVersionName) {
		this.datasetVersionName = datasetVersionName;
	}

	public Boolean getIsCurrent() {
		return isCurrent;
	}

	public void setIsCurrent(Boolean isCurrent) {
		this.isCurrent = isCurrent;
	}

	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	public Set<DataStream> getDataStreams() {
		return dataStreams;
	}

	public void setDataStreams(Set<DataStream> dataStreams) {
		this.dataStreams = dataStreams;
	}
	
}
