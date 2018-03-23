package com.rho.rhover.common.anomaly;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class AnomalyResolution {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="anomaly_resolution_id")
	private Long anomalyResolutionId;
	
	@Column(name="anomaly_resolution_code")
	private String anomalyResolutionCode;
	
	@Column(name="anomaly_resolution_description")
	private String anomalyResolutionDescription;

	public AnomalyResolution() {
		
	}

	public Long getAnomalyResolutionId() {
		return anomalyResolutionId;
	}

	public void setAnomalyResolutionId(Long anomalyResolutionId) {
		this.anomalyResolutionId = anomalyResolutionId;
	}

	public String getAnomalyResolutionCode() {
		return anomalyResolutionCode;
	}

	public void setAnomalyResolutionCode(String anomalyResolutionCode) {
		this.anomalyResolutionCode = anomalyResolutionCode;
	}

	public String getAnomalyResolutionDescription() {
		return anomalyResolutionDescription;
	}

	public void setAnomalyResolutionDescription(String anomalyResolutionDescription) {
		this.anomalyResolutionDescription = anomalyResolutionDescription;
	}

}
