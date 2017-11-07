package com.rho.rhover.study;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.rho.rhover.anomaly.Anomaly;
import com.rho.rhover.anomaly.AnomalyRepository;
import com.rho.rhover.anomaly.BivariateCheck;
import com.rho.rhover.anomaly.BivariateCheckRepository;

@Service
public class StudyDataRepositoryImpl implements StudyDataRepository {
	
	private static final String REPO_PATH = "C:/RhoVer";
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private AnomalyRepository anomalyRepository;
	
	@Autowired
	private DataFieldRepository dataFieldRepository;
	
	@Autowired
	private BivariateCheckRepository bivariateCheckRepository;

	@Override
	public String getUnivariateData(Long dataFieldId) {
		DataField dataField = dataFieldRepository.findOne(dataFieldId);
		String fname = REPO_PATH
				+ "/" + dataField.getDataset().getStudy().getStudyName()
				+ "/" + dataField.getDataset().getDatasetName()
				+ "/" + dataField.getDataFieldName() + ".csv";
		String data = "";
		BufferedReader reader = null;
		Map<String, Long> anomalies = buildAnomalySet(dataFieldId);
		//logger.debug(anomalies.toString());
		try {
			StringBuilder builder = new StringBuilder();
			reader = new BufferedReader(new FileReader(fname));
			String header = reader.readLine();
			if (header != null) {
				builder.append(header + ",anomaly_id\n");
				String record = reader.readLine();
				while (record != null) {
					String[] fields = record.split(",");
					if (fields.length == 5) {
						String key = fields[1] + fields[2];
						String anomalyIdValue = ",0\n";
						if (anomalies.containsKey(key)) {
							anomalyIdValue = "," + anomalies.get(key) + "\n";
						}
						builder.append(record + anomalyIdValue);
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
		
//		String sql = "update anomaly set has_been_viewed = 1 where data_field_id = " + dataFieldId;
//		jdbcTemplate.update(sql);
		markAnomaliesAsViewed(dataFieldId);
		return data;
	}
	
	
	private Map<String, Long> buildAnomalySet(Long dataFieldId) {
		Map<String, Long> map = new HashMap<>();
		Iterable<Anomaly> anomalies = anomalyRepository.getCurrentAnomalies(dataFieldId);
		anomalies.forEach(anomaly -> map.put(anomaly.getRecruitId() + anomaly.getEvent(),
				anomaly.getAnomalyId()));
		return map;
	}

	@Override
	public String getBivariateData(Long bivariateCheckId) {
		BivariateCheck check = bivariateCheckRepository.findOne(bivariateCheckId);
		StringBuilder builder = new StringBuilder();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(check.getFilePath()));
			String line = reader.readLine();
			while (line != null) {
				builder.append(line + "\n");
				line = reader.readLine();
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			try {
				if (reader != null) {
					reader.close();
				}
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return builder.toString();
	}


	@Override
	public void markAnomaliesAsViewed(Long dataFieldId) {
		String sql = "update anomaly set has_been_viewed = 1 where data_field_id = " + dataFieldId;
		jdbcTemplate.update(sql);
	}
	
}
