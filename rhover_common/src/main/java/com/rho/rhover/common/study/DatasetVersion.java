package com.rho.rhover.common.study;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
	
	@Column(name="num_records")
	private Integer numRecords;
	
	@ManyToOne
	@JoinColumn(name="dataset_id")
	private Dataset dataset;
	
	@ManyToMany
	@JoinTable(name="dataset_version_stream", joinColumns = @JoinColumn(name="dataset_version_id"),
			inverseJoinColumns = @JoinColumn(name="data_stream_id"))
	private Set<DataStream> dataStreams = new HashSet<>();
	
	@ManyToMany
	@JoinTable(name="dataset_version_field", joinColumns = @JoinColumn(name="dataset_version_id"),
			inverseJoinColumns = @JoinColumn(name="field_id"))
	private Set<Field> fields = new HashSet<>();
	
	@ManyToMany(mappedBy = "datasetVersions")
	private Set<StudyDbVersion> studyDbVersions = new HashSet<>();
	
	public DatasetVersion() {
		
	}
	
	public DatasetVersion(String datasetVersionName, Boolean isCurrent, Dataset dataset, Integer numRecords) {
		super();
		this.datasetVersionName = datasetVersionName;
		this.isCurrent = isCurrent;
		this.dataset = dataset;
		this.numRecords = numRecords;
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
	
	public void addDataStream(DataStream dataStream) {
		dataStreams.add(dataStream);
	}
	
	public void addField(Field field) {
		fields.add(field);
	}

	public Set<Field> getFields() {
		return fields;
	}

	public void setFields(Set<Field> fields) {
		this.fields = fields;
	}

	public Set<StudyDbVersion> getStudyDbVersions() {
		return studyDbVersions;
	}

	public void setStudyDbVersions(Set<StudyDbVersion> studyDbVersions) {
		this.studyDbVersions = studyDbVersions;
	}
	
	public void addStudyDbVersion(StudyDbVersion studyDbVersion) {
		this.studyDbVersions.add(studyDbVersion);
	}

	public Integer getNumRecords() {
		return numRecords;
	}

	public void setNumRecords(Integer numRecords) {
		this.numRecords = numRecords;
	}
	
	public String getNumericFieldSummary() {
		int numNumeric = 0;
		int numContinuous = 0;
		int numInteger = 0;
		StringBuilder integerBuilder = new StringBuilder("INTEGER VARIABLES: ");
		StringBuilder continuousBuilder = new StringBuilder("CONTINUOUS VARIABLES: ");
		for (Field field : fields) {
			if (field.getIsNumeric()) {
				numNumeric++;
				StringBuilder builder = null;
				if (field.getDataType().equals("Double")) {
					numContinuous++;
					if (numContinuous > 1) {
						continuousBuilder.append(" | ");
					}
					builder = continuousBuilder;
				}
				else if (field.getDataType().equals("Integer")) {
					numInteger++;
					if (numInteger > 1) {
						integerBuilder.append(" | ");
					}
					builder = integerBuilder;
				}
				if (field.getFieldLabel() == null || field.getFieldLabel().length() == 0) {
					builder.append(field.getFieldName());
				}
				else {
					builder.append(field.getFieldLabel());
				}
			}
		}
		if (numNumeric == 0) {
			return "No numeric variables";
		}
		String continuousString = continuousBuilder.toString();
		if (numContinuous == 0) {
			continuousString = "CONTINUOUS_VARIABLES: None";
		}
		String integerString = integerBuilder.toString();
		if (numInteger == 0) {
			integerString = "INTEGER_VARIABLES: None";
		}
		return continuousString + "   " + integerString;
	}
	
	public int getNumNonIdentifyingNumericFields() {
		int count = 0;
		for (Field field : fields) {
			if (field.getIsNumeric() && !field.getIsIdentifying()) {
				count++;
			}
		}
		return count;
	}
	
	public int getNumNonIdentifyingContinuousFields() {
		int count = 0;
		for (Field field : fields) {
			if (field.getDataType().equals("Double") && !field.getIsIdentifying()) {
				count++;
			}
		}
		return count;
	}
}
