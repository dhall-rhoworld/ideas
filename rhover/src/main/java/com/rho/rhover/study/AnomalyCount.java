package com.rho.rhover.study;

public class AnomalyCount {

	private Long entityId;
	private String entityName;
	private Integer numAnomalies;
	private Integer numUnviewedAnomalies;
	
	public AnomalyCount() {
		
	}

	public AnomalyCount(Long entityId, String entityName, Integer numAnomalies, Integer numUnviewedAnomalies) {
		super();
		this.entityId = entityId;
		this.entityName = entityName;
		this.numAnomalies = numAnomalies;
		this.numUnviewedAnomalies = numUnviewedAnomalies;
	}

	public Long getEntityId() {
		return entityId;
	}

	public void setEntityId(Long entityId) {
		this.entityId = entityId;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public Integer getNumAnomalies() {
		return numAnomalies;
	}

	public void setNumAnomalies(Integer numAnomalies) {
		this.numAnomalies = numAnomalies;
	}

	public Integer getNumUnviewedAnomalies() {
		return numUnviewedAnomalies;
	}

	public void setNumUnviewedAnomalies(Integer numUnviewedAnomalies) {
		this.numUnviewedAnomalies = numUnviewedAnomalies;
	}
}
