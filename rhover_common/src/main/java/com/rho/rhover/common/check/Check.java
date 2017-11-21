package com.rho.rhover.common.check;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="checks")
public class Check {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="check_id")
	private Long checkId;
	
	@Column(name="check_name")
	private String checkName;
	
	public Check() {
		
	}

	public Long getCheckId() {
		return checkId;
	}

	public void setCheckId(Long checkId) {
		this.checkId = checkId;
	}

	public String getCheckName() {
		return checkName;
	}

	public void setCheckName(String checkName) {
		this.checkName = checkName;
	}
}
