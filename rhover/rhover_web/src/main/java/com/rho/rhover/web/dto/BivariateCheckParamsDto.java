package com.rho.rhover.web.dto;

public class BivariateCheckParamsDto {
	
	private Long bivariateCheckId;
	
	private Boolean useDefaults;
	
	private Double sdResidual;
	
	private Double sdDensity;

	public BivariateCheckParamsDto() {
		
	}

	public Long getBivariateCheckId() {
		return bivariateCheckId;
	}

	public void setBivariateCheckId(Long bivariateCheckId) {
		this.bivariateCheckId = bivariateCheckId;
	}

	public Boolean getUseDefaults() {
		return useDefaults;
	}

	public void setUseDefaults(Boolean useDefaults) {
		this.useDefaults = useDefaults;
	}

	public Double getSdResidual() {
		return sdResidual;
	}

	public void setSdResidual(Double sdResidual) {
		this.sdResidual = sdResidual;
	}

	public Double getSdDensity() {
		return sdDensity;
	}

	public void setSdDensity(Double sdDensity) {
		this.sdDensity = sdDensity;
	}

}
