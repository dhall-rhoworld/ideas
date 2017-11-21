package com.rho.rhover.web.dto;

import java.util.ArrayList;
import java.util.List;

import com.rho.rhover.common.check.CheckParam;

public class CheckParamDto {

	private String checkName;
	
	private String paramName;
	
	private String paramValue;

	public CheckParamDto(String checkName, String paramName, String paramValue) {
		super();
		this.checkName = checkName;
		this.paramName = paramName;
		this.paramValue = paramValue;
	}

	public String getCheckName() {
		return checkName;
	}

	public void setCheckName(String checkName) {
		this.checkName = checkName;
	}

	public String getParamName() {
		return paramName;
	}

	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	public String getParamValue() {
		return paramValue;
	}

	public void setParamValue(String paramValue) {
		this.paramValue = paramValue;
	}
	
	public static List<CheckParamDto> toCheckParamDtos(Iterable<CheckParam> params) {
		List<CheckParamDto> dtos = new ArrayList<>();
		for (CheckParam param : params) {
			dtos.add(new CheckParamDto(param.getCheck().getCheckName(), param.getParamName(), param.getParamValue()));
		}
		return dtos;
	}
}
