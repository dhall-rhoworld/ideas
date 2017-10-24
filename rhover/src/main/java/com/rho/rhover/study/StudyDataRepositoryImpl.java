package com.rho.rhover.study;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.StringTokenizer;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import tech.tablesaw.api.Table;

@Service
public class StudyDataRepositoryImpl implements StudyDataRepository {
	
	private static final String REPO_PATH = "C:/RhoVer";
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public String getAllDataFieldValues(Long dataFieldId) {
		
		String fname = REPO_PATH + "/" + getDatasetName(dataFieldId) + ".csv";
		String dataFieldName = getDataFieldName(dataFieldId);
		
		Table t1;
		try {
			t1 = Table.read().csv(fname);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Table t2 = Table.create("t2");
		t2.addColumn(t1.column("RecruitID"));
		t2.addColumn(t1.column("event"));
		t2.addColumn(t1.column(dataFieldName));
		
		OutputStream
	}

	private int getColumnIndex(BufferedReader reader, String dataFieldName) throws IOException {
		String line = reader.readLine();
		StringTokenizer tok = new StringTokenizer(line, "\",\"");
		int index = -1;
		int count = 0;
		while (tok.hasMoreTokens() && index < 0) {
			String str = tok.nextToken();
			if (str.equals(dataFieldName)) {
				index = count;
			}
			count++;
		}
		return index;
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
		return jdbcTemplate.queryForObject(sql, String.class);
	}
}
