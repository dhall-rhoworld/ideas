package com.rho.rhover.common.study;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class MergeField {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="merge_field_id")
	private Long mergeFieldId;
	
	@ManyToOne
	@JoinColumn(name="field_instance_id_1")
	private FieldInstance fieldInstance1;
	
	@ManyToOne
	@JoinColumn(name="field_instance_id_2")
	private FieldInstance fieldInstance2;

	public MergeField() {
		
	}

	public Long getMergeFieldId() {
		return mergeFieldId;
	}

	public void setMergeFieldId(Long mergeFieldId) {
		this.mergeFieldId = mergeFieldId;
	}

	public FieldInstance getFieldInstance1() {
		return fieldInstance1;
	}

	public void setFieldInstance1(FieldInstance fieldInstance1) {
		this.fieldInstance1 = fieldInstance1;
	}

	public FieldInstance getFieldInstance2() {
		return fieldInstance2;
	}

	public void setFieldInstance2(FieldInstance fieldInstance2) {
		this.fieldInstance2 = fieldInstance2;
	}

}
