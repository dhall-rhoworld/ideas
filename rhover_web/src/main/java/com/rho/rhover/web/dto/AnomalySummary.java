package com.rho.rhover.web.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Aggregate counts of anomalies associated with some entity, which may be a study, dataset, or data field
 * @author dhall
 *
 */
public class AnomalySummary {

	private Long entityId;
	private String entityName;
	private Integer numAnomalies;
	private Integer numUnviewedAnomalies;
	private String attribute;
	private Long entityId2;
	private String entityName2;
	
	public AnomalySummary() {
		
	}

	public AnomalySummary(Long entityId, String entityName, Integer numAnomalies, Integer numUnviewedAnomalies) {
		super();
		this.entityId = entityId;
		this.entityName = entityName;
		this.numAnomalies = numAnomalies;
		this.numUnviewedAnomalies = numUnviewedAnomalies;
	}
	
	public AnomalySummary(Long entityId, String entityName, Integer numAnomalies, Integer numUnviewedAnomalies,
			Long entityId2, String entityName2) {
		super();
		this.entityId = entityId;
		this.entityName = entityName;
		this.numAnomalies = numAnomalies;
		this.numUnviewedAnomalies = numUnviewedAnomalies;
		this.entityId2 = entityId2;
		this.entityName2 = entityName2;
	}

	public AnomalySummary(Long entityId, String entityName, Integer numAnomalies, Integer numUnviewedAnomalies, String attribute) {
		this(entityId, entityName, numAnomalies, numUnviewedAnomalies);
		this.setAttribute(attribute);
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

	public String getAttribute() {
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	public Long getEntityId2() {
		return entityId2;
	}

	public void setEntityId2(Long entityId2) {
		this.entityId2 = entityId2;
	}

	public String getEntityName2() {
		return entityName2;
	}

	public void setEntityName2(String entityName2) {
		this.entityName2 = entityName2;
	}
	
}
