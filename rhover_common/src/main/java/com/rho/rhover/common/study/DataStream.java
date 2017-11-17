package com.rho.rhover.common.study;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

@Entity
public class DataStream {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="data_stream_id")
	private Long dataStreamId;
	
	@Column(name="data_stream_name")
	private String dataStreamName;
	
	@ManyToOne
	@JoinColumn(name="study_id")
	private Study study;
	
	@ManyToMany(mappedBy="dataStreams")
	private Set<DatasetVersion> datasetVersions = new HashSet<>();
	
	public DataStream() {
		
	}

	public DataStream(String dataStreamName, Study study) {
		super();
		this.dataStreamName = dataStreamName;
		this.study = study;
	}

	public Long getDataStreamId() {
		return dataStreamId;
	}

	public void setDataStreamId(Long dataStreamId) {
		this.dataStreamId = dataStreamId;
	}

	public String getDataStreamName() {
		return dataStreamName;
	}

	public void setDataStreamName(String dataStreamName) {
		this.dataStreamName = dataStreamName;
	}

	public Study getStudy() {
		return study;
	}

	public void setStudy(Study study) {
		this.study = study;
	}

	public Set<DatasetVersion> getDatasetVersions() {
		return datasetVersions;
	}

	public void setDatasetVersions(Set<DatasetVersion> datasetVersions) {
		this.datasetVersions = datasetVersions;
	}
	
	public void addDatasetVersion(DatasetVersion datasetVersion) {
		datasetVersions.add(datasetVersion);
	}
}
