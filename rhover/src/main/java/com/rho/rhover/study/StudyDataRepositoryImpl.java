package com.rho.rhover.study;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.rho.rhover.anomaly.Anomaly;
import com.rho.rhover.anomaly.AnomalyRepository;

@Service
public class StudyDataRepositoryImpl implements StudyDataRepository {
	
	private static final String REPO_PATH = "C:/RhoVer";
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private AnomalyRepository anomalyRepository;

	@Override
	public String getAllDataFieldValues(Long dataFieldId) {
		String fname = REPO_PATH + "/" + getStudyName(dataFieldId) + "/" + getDatasetName(dataFieldId) + "/" + getDataFieldName(dataFieldId) + ".csv";
		String data = "";
		BufferedReader reader = null;
		Set<String> anomalies = buildAnomalySet(dataFieldId);
		//logger.debug(anomalies.toString());
		try {
			StringBuilder builder = new StringBuilder();
			reader = new BufferedReader(new FileReader(fname));
			String header = reader.readLine();
			if (header != null) {
				builder.append(header + ",is_anomaly\n");
				String record = reader.readLine();
				while (record != null) {
					String[] fields = record.split(",");
					if (fields.length == 4) {
						String key = fields[1] + fields[2];
						String isAnomalyField = ",0\n";
						if (anomalies.contains(key)) {
							isAnomalyField = ",1\n";
						}
						builder.append(record + isAnomalyField);
					}
					record = reader.readLine();
				}
			}
			//data = FileUtils.readFileToString(new File(fname));
			data = builder.toString();
			//logger.debug(data);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		finally {
			try {
				reader.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return data;
	}
	
	private Set<String> buildAnomalySet(Long dataFieldId) {
		Set<String> set = new HashSet<>();
		Iterable<Anomaly> anomalies = anomalyRepository.getCurrentAnomalies(dataFieldId);
		anomalies.forEach(anomaly -> set.add(anomaly.getRecruitId() + anomaly.getEvent()));
		return set;
	}
	
	public String getDataFieldName(Long dataFieldId) {
		String sql = "select data_field_name from data_field where data_field_id = " + dataFieldId;
		return jdbcTemplate.queryForObject(sql, String.class);
	}
	
	public Long getDatasetId(Long dataFieldId) {
		String sql = "select ds.dataset_id\r\n" + 
				"from dataset ds\r\n" + 
				"join data_field df on df.dataset_id = ds.dataset_id\r\n" + 
				"where df.data_field_id = " + dataFieldId;
		return jdbcTemplate.queryForObject(sql, Long.class);
	}
	
	public String getDatasetName(Long dataFieldId) {
		String sql = "select ds.dataset_name\r\n" + 
				"from dataset ds\r\n" + 
				"join data_field df on df.dataset_id = ds.dataset_id\r\n" + 
				"where df.data_field_id = " + dataFieldId;
		String name = jdbcTemplate.queryForObject(sql, String.class);
		return name.replaceAll("/", "_per_");
	}
	
	public Long getStudyId(Long dataFieldId) {
		String sql = "select s.study_id\r\n" + 
				"from study s\r\n" + 
				"join dataset ds on ds.study_id = s.study_id\r\n" + 
				"join data_field df on df.dataset_id = ds.dataset_id\r\n" + 
				"where df.data_field_id = " + dataFieldId;
		return jdbcTemplate.queryForObject(sql, Long.class);
	}
	
	public String getStudyName(Long dataFieldId) {
		String sql = "select s.study_name\r\n" + 
				"from study s\r\n" + 
				"join dataset ds on ds.study_id = s.study_id\r\n" + 
				"join data_field df on df.dataset_id = ds.dataset_id\r\n" + 
				"where df.data_field_id = " + dataFieldId;
		return jdbcTemplate.queryForObject(sql, String.class);	
	}

	@Override
	public Double getLowerThreshold(Long dataFieldId) {
		String sql = "select lower_threshold from data_field where data_field_id = " + dataFieldId;
		return jdbcTemplate.queryForObject(sql, Double.class);
	}

	@Override
	public Double getUpperThreshold(Long dataFieldId) {
		String sql = "select upper_threshold from data_field where data_field_id = " + dataFieldId;
		return jdbcTemplate.queryForObject(sql, Double.class);
	}
}
