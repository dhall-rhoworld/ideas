package com.rho.rhover.web.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rho.rhover.common.check.CheckRun;
import com.rho.rhover.common.study.CsvData;
import com.rho.rhover.common.study.CsvDataRepository;
import com.rho.rhover.common.study.Field;
import com.rho.rhover.common.study.FieldRepository;
import com.rho.rhover.common.study.Study;
import com.rho.rhover.web.dto.UniAnomalyDto;
import com.rho.rhover.web.dto.UniAnomalyDtoRepository;

@Service
public class CsvDataServiceImpl implements CsvDataService {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private FieldRepository fieldRepository;
	
	@Autowired
	private CsvDataRepository csvDataRepository;
	
	@Autowired
	private UniAnomalyDtoRepository uniAnomalyDtoRepository;
	
	@Autowired
	private DataSource dataSource;

	@Override
	public String getCsvData(CheckRun checkRun) {
		
		// Assemble fields
		List<Field> idFields = fieldRepository.findByDatasetVersionAndIsIdentifying(checkRun.getDatasetVersion(), Boolean.TRUE);
		List<Field> fields = new ArrayList<>();
		fields.addAll(idFields);
		Study study = checkRun.getDatasetVersion().getDataset().getStudy();
		Field subjectField = study.getSubjectField();
		Field siteField = study.getSiteField();
		if (!fields.contains(subjectField)) {
			fields.add(subjectField);
		}
		if (!fields.contains(siteField)) {
			fields.add(siteField);
		}
		fields.add(checkRun.getField());
		
		// Assemble data records
		List<StringTokenizer> tokenizers = new ArrayList<>();
		List<String> fieldNames = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		int count = 0;
		for (Field field : fields) {
			count++;
			if (count > 1) {
				builder.append(",");
			}
			builder.append(field.getDisplayName());
			CsvData csvData = csvDataRepository.findByFieldAndDataset(field, checkRun.getDatasetVersion().getDataset());
			tokenizers.add(new StringTokenizer(csvData.getData(), ","));
			fieldNames.add(field.getFieldName());
		}
		builder.append(",anomaly_id\n");
		boolean moreRecords = true;
		List<Record> records = new ArrayList<>();
		while (moreRecords) {
			boolean hasNull = false;
			Record record = new Record();
			for (int i = 0; i < fieldNames.size(); i++) {
				String fieldName = fieldNames.get(i);
				StringTokenizer tok = tokenizers.get(i);
				if (!tok.hasMoreTokens()) {
					moreRecords = false;
					break;
				}
				String token = tok.nextToken();
				if (token.trim().length() == 0 || token.equals("null")) {
					hasNull = true;
				}
				record.fieldValues.put(fieldName, token);
				if (fieldName.equals(checkRun.getField().getFieldName())) {
					record.dataValue = token;
				}
			}
			if (moreRecords) {
				if (!hasNull) {
					records.add(record);
				}
				if (record.dataValue == null) {
					throw new RuntimeException();
				}
			}
		}
		
		addAnomalyIds(records, checkRun, idFields);
		fieldNames.add("anomaly_id");
		
		Collections.sort(records);
		for (Record record : records) {
			builder.append(record.toCsv(fieldNames) + "\n");
		}
		//logger.debug(builder.toString());
		return builder.toString();
	}
	
	private void addAnomalyIds(List<Record> records, CheckRun checkRun, List<Field> idFields) {
		List<UniAnomalyDto> dtos = uniAnomalyDtoRepository.findByCheckRunId(checkRun.getCheckRunId());
		Map<String, Long> anomalyIdIndex = new HashMap<>();
		for (UniAnomalyDto dto : dtos) {
			anomalyIdIndex.put(concatenateKeysAndValues(dto.getIdFieldNamesAndValues(), idFields), dto.getAnomalyId());
		}
		for (Record record : records) {
			String key = concatenateKeysAndValues(record.fieldValues, idFields);
			Long anomalyId = anomalyIdIndex.get(key);
			if (anomalyId == null) {
				anomalyId = 0L;
			}
			record.fieldValues.put("anomaly_id", anomalyId.toString());
		}
	}
	
	private String concatenateKeysAndValues(Map<String, String> map, List<Field> idFields) {
		StringBuilder builder = new StringBuilder();
		int count = 0;
		for (Field field : idFields) {
			count++;
			if (count > 1) {
				builder.append(",");
			}
			String fieldName = field.getFieldName();
			builder.append(fieldName + "=" + map.get(fieldName));
		}
		return builder.toString();
	}

	private static final class Record implements Comparable<Record> {
		
		private Logger logger = LoggerFactory.getLogger(this.getClass());
		
		private Map<String, String> fieldValues = new HashMap<>();
		
		private String dataValue;
		
		private String toCsv(List<String> fieldNames) {
			StringBuilder builder = new StringBuilder();
			int count = 0;
			for (String fieldName : fieldNames) {
				count++;
				if (count > 1) {
					builder.append(",");
				}
				//logger.debug(fieldName + ": " + fieldValues.get(fieldName));
				builder.append(fieldValues.get(fieldName));
			}
			return builder.toString();
		}
	
		@Override
		public int compareTo(Record o) {
			return new Double(this.dataValue).compareTo(new Double(o.dataValue));
		}
		
	}
}



