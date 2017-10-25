package com.rho.rhover.study;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class StudyDataRepositoryImpl implements StudyDataRepository {
	
	private static final String REPO_PATH = "C:/RhoVer";
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public String getAllDataFieldValues(Long dataFieldId) {
		String fname = REPO_PATH + "/" + getStudyName(dataFieldId) + "/" + getDatasetName(dataFieldId) + "/" + getDataFieldName(dataFieldId) + ".csv";
		String data = "";
		try {
			data = FileUtils.readFileToString(new File(fname));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return data;
	}
	
	private String getDataFieldName(Long dataFieldId) {
		String sql = "select data_field_name from data_field where data_field_id = " + dataFieldId;
		return jdbcTemplate.queryForObject(sql, String.class);
	}
	
	private String getDatasetName(Long dataFieldId) {
		String sql = "select ds.dataset_name\r\n" + 
				"from dataset ds\r\n" + 
				"join data_field df on df.dataset_id = ds.dataset_id\r\n" + 
				"where df.data_field_id = " + dataFieldId;
		String name = jdbcTemplate.queryForObject(sql, String.class);
		return name.replaceAll("/", "_per_");
	}
	
	private String getStudyName(Long dataFieldId) {
		String sql = "select s.study_name\r\n" + 
				"from study s\r\n" + 
				"join dataset ds on ds.study_id = s.study_id\r\n" + 
				"join data_field df on df.dataset_id = ds.dataset_id\r\n" + 
				"where df.data_field_id = " + dataFieldId;
		return jdbcTemplate.queryForObject(sql, String.class);	
	}
}
