package com.rho.rhover.common.study;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Type;

@Entity
public class FieldInstance {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="field_instance_id")
	private Long fieldInstanceId;
	
	@ManyToOne
	@JoinColumn(name="field_id")
	private Field field;
	
	@ManyToOne
	@JoinColumn(name="dataset_id")
	private Dataset dataset;
	
	@Column(name="is_potential_splitter")
	@Type(type="org.hibernate.type.NumericBooleanType")
	private Boolean isPotentialSplitter = Boolean.FALSE;
	
	@Column(name="is_potential_splittee")
	@Type(type="org.hibernate.type.NumericBooleanType")
	private Boolean isPotentialSplittee = Boolean.FALSE;
	
	@ManyToOne
	@JoinColumn(name="first_dataset_version_id")
	private DatasetVersion firstDatasetVersion;

	public FieldInstance() {
		
	}

	public FieldInstance(Field field, Dataset dataset) {
		super();
		this.field = field;
		this.dataset = dataset;
	}

	public Long getFieldInstanceId() {
		return fieldInstanceId;
	}

	public void setFieldInstanceId(Long fieldInstanceId) {
		this.fieldInstanceId = fieldInstanceId;
	}

	public Field getField() {
		return field;
	}

	public void setField(Field field) {
		this.field = field;
	}

	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	public Boolean getIsPotentialSplitter() {
		return isPotentialSplitter;
	}

	public void setIsPotentialSplitter(Boolean isPotentialSplitter) {
		this.isPotentialSplitter = isPotentialSplitter;
	}

	public Boolean getIsPotentialSplittee() {
		return isPotentialSplittee;
	}

	public void setIsPotentialSplittee(Boolean isPotentialSplittee) {
		this.isPotentialSplittee = isPotentialSplittee;
	}

	public DatasetVersion getFirstDatasetVersion() {
		return firstDatasetVersion;
	}

	public void setFirstDatasetVersion(DatasetVersion firstDatasetVersion) {
		this.firstDatasetVersion = firstDatasetVersion;
	}

}
