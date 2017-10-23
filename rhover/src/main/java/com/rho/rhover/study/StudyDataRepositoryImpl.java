package com.rho.rhover.study;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class StudyDataRepositoryImpl implements StudyDataRepository {
	
	private static final String REPO_PATH = "C:/RhoVer";
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public String getAllDataFieldValues(Long dataFieldId) {
		
		String fname = REPO_PATH + "/" + getDatasetName(dataFieldId) + ".csv";
		StringBuilder builder = new StringBuilder();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(fname));
			int colIndex = getColumnIndex(reader, getDataFieldName(dataFieldId));
			int count = 0;
			String line = reader.readLine();
			while (line != null) {
				String[] fields = line.split(",");
				String value = fields[colIndex];
				if (value.startsWith("\"")) {
					value = value.substring(1, value.length() - 1);
				}
				if (NumberUtils.isCreatable(value)) {
					count++;
					if (count > 1) {
						builder.append("\n");
					}
					builder.append(value);
				}
				line = reader.readLine();
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return builder.toString();
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
