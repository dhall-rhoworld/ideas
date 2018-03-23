package com.rho.rhover.common.anomaly;

public class BivariateAnomalyDto extends AnomalyDto {
	
	private Long fieldInstance1Id;
	
	private Long fieldInstance2Id;
	
	private String fieldName1;
	
	private String fieldName2;
	
	private String anomalousValue1;
	
	private String anomalousValue2;

	public BivariateAnomalyDto() {
		
	}

	public Long getFieldInstance1Id() {
		return fieldInstance1Id;
	}

	public void setFieldInstance1Id(Long fieldInstance1Id) {
		this.fieldInstance1Id = fieldInstance1Id;
	}

	public Long getFieldInstance2Id() {
		return fieldInstance2Id;
	}

	public void setFieldInstance2Id(Long fieldInstance2Id) {
		this.fieldInstance2Id = fieldInstance2Id;
	}

	public String getFieldName1() {
		return fieldName1;
	}

	public void setFieldName1(String fieldName1) {
		this.fieldName1 = fieldName1;
	}

	public String getFieldName2() {
		return fieldName2;
	}

	public void setFieldName2(String fieldName2) {
		this.fieldName2 = fieldName2;
	}

	public String getAnomalousValue1() {
		return anomalousValue1;
	}

	public void setAnomalousValue1(String anomalousValue1) {
		this.anomalousValue1 = anomalousValue1;
	}

	public String getAnomalousValue2() {
		return anomalousValue2;
	}

	public void setAnomalousValue2(String anomalousValue2) {
		this.anomalousValue2 = anomalousValue2;
	}

}
