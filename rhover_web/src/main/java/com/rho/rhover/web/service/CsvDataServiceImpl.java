package com.rho.rhover.web.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rho.rhover.common.check.CheckRun;
import com.rho.rhover.common.study.Field;
import com.rho.rhover.common.study.FieldRepository;
import com.rho.rhover.common.study.Study;

@Service
public class CsvDataServiceImpl implements CsvDataService {
	
	@Autowired
	private FieldRepository fieldRepository;

	@Override
	public String getCsvData(CheckRun checkRun) {
		List<Field> fields = fieldRepository.findByDatasetVersionAndIsIdentifying(checkRun.getDatasetVersion(), Boolean.TRUE);
		Study study = checkRun.getDatasetVersion().getDataset().getStudy();
		Field subjectField = fieldRepository.findByStudyAndFieldName(study, study.getSubjectFieldName());
		Field siteField = fieldRepository.findByStudyAndFieldName(study, study.getSiteFieldName());
		
		if (!fields.contains(subjectField)) {
			fields.add(subjectField);
		}
		if (!fields.contains(siteField)) {
			fields.add(siteField);
		}
		return "Hello " + checkRun.getField().getFieldLabel();
	}

}
