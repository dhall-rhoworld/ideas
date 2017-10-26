package com.rho.rhover.study;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rho.rhover.anomaly.Anomaly;
import com.rho.rhover.anomaly.AnomalyRepository;

@Service
public class StudyDataRepositoryImpl implements StudyDataRepository {
	
	private static final String REPO_PATH = "C:/RhoVer";
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private AnomalyRepository anomalyRepository;
	
	@Autowired
	private DataFieldRepository dataFieldRepository;

	@Override
	public String getAllDataFieldValues(Long dataFieldId) {
		DataField dataField = dataFieldRepository.findOne(dataFieldId);
		String fname = REPO_PATH
				+ "/" + dataField.getDataset().getStudy().getStudyName()
				+ "/" + dataField.getDataset().getDatasetName()
				+ "/" + dataField.getDataFieldName() + ".csv";
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
	
}
