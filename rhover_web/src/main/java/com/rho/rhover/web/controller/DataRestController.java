package com.rho.rhover.web.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rho.rhover.common.check.CsvDataService;
import com.rho.rhover.common.study.Field;
import com.rho.rhover.common.study.FieldInstance;
import com.rho.rhover.common.study.FieldInstanceRepository;
import com.rho.rhover.common.study.FieldRepository;
import com.rho.rhover.common.study.MergeField;
import com.rho.rhover.common.study.Study;

@RestController
@RequestMapping("/rest/data")
public class DataRestController {
	
	@Autowired
	private FieldInstanceRepository fieldInstanceRepository;
	
	@Autowired
	private FieldRepository fieldRepository;
	
	@Autowired
	private CsvDataService csvDataService;

	@RequestMapping("/bivariate")
	public String getBivariateDataAsCsv(
			@RequestParam("field_instance_id_1") Long fieldInstanceId1,
			@RequestParam("field_instance_id_2") Long fieldInstanceId2) {
		FieldInstance fi1 = fieldInstanceRepository.findOne(fieldInstanceId1);
		FieldInstance fi2 = fieldInstanceRepository.findOne(fieldInstanceId2);
		String csv =
			"Height,Weight\n" +
			"68,150\n" +
			"70,175\n" +
			"59,200\n";
		if (fi1.getDataset().equals(fi2.getDataset())) {
			csv = csvDataService.getAsCsv(fi1.getDataset(), Boolean.TRUE, fi1.getField(), fi2.getField());
		}
		else {
			Study study = fi1.getField().getStudy();
			Field subjectField = fieldRepository.findByStudyAndFieldName(study, study.getSubjectFieldName());
			List<MergeField> mergeFields = new ArrayList<>();
			MergeField mergeField = new MergeField();
			mergeField.setFieldInstance1(fieldInstanceRepository.findByFieldAndDataset(subjectField, fi1.getDataset()));
			mergeField.setFieldInstance2(fieldInstanceRepository.findByFieldAndDataset(subjectField, fi2.getDataset()));
			mergeFields.add(mergeField);
			List<FieldInstance> dataFields = new ArrayList<>();
			dataFields.add(fi1);
			dataFields.add(fi2);
			csv = csvDataService.mergeToCsv(mergeFields, dataFields, Boolean.FALSE, Boolean.TRUE);
		}
		return csv;
	}
}
