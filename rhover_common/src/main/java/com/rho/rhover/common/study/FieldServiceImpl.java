package com.rho.rhover.common.study;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class FieldServiceImpl implements FieldService {
	
	private static final double MIN_PERCENT = 0.75;
	
	@Autowired
	private FieldRepository fieldRepository;
	
	@Autowired
	private DataSource dataSource;

	@Override
	public List<Field> findPotentiallyIdentiableFields(Study study) {
		
		// Get number of datasets in current database version
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		String sql = "select count(*)\r\n" + 
				"from study_db_version_config sdvc\r\n" + 
				"join study_db_version sdv on sdv.study_db_version_id = sdvc.study_db_version_id\r\n" + 
				"where sdv.study_id = " + study.getStudyId() + "\r\n" + 
				"and sdv.is_current = 1;\r\n" + 
				"";
		int numDatasets = jdbcTemplate.queryForObject(sql, Integer.class);
		
		// Calculate mininum number of datasets to be potentially identifiable
		int minNumDatasets = (int)(numDatasets * MIN_PERCENT);
		
		// Get number of datasets that each field occurs in
		sql = "select dvf.field_id as field_id, count(*) as total\r\n" + 
				"from dataset_version_field dvf\r\n" + 
				"join dataset_version dv on dv.dataset_version_id = dvf.dataset_version_id\r\n" + 
				"join study_db_version_config sdvc on sdvc.dataset_version_id = dv.dataset_version_id\r\n" + 
				"join study_db_version sdv on sdv.study_db_version_id = sdvc.study_db_version_id\r\n" + 
				"join dataset ds on ds.dataset_id = dv.dataset_id\r\n" + 
				"where sdv.study_id = " + study.getStudyId() + "\r\n" +
				"and sdv.is_current = 1\r\n" + 
				"group by dvf.field_id";
		List<DatasetCountTuple> tuples = jdbcTemplate.query(sql, new RowMapper<DatasetCountTuple>() {
			public DatasetCountTuple mapRow(ResultSet rs, int p) throws SQLException {
				DatasetCountTuple tuple = new DatasetCountTuple();
				tuple.fieldId = rs.getLong(1);
				tuple.numDatasets = rs.getInt(2);
				return tuple;
			}
		});
		List<Long> ids = new ArrayList<>();
		for (DatasetCountTuple tuple : tuples) {
			if (tuple.numDatasets >= minNumDatasets) {
				ids.add(tuple.fieldId);
			}
		}
		return fieldRepository.findAll(ids);
	}
	
	@Override
	@Transactional
	public void setIdentifiableFields(Study study, Iterable<Long> fieldIds) {
		Iterable<Field> fields = fieldRepository.findByStudy(study);
		for (Field field : fields) {
			field.setIsIdentifying(Boolean.FALSE);
			fieldRepository.save(field);
		}
		Iterable<Field> idFields = fieldRepository.findAll(fieldIds);
		for (Field field : idFields) {
			field.setIsIdentifying(Boolean.TRUE);
			fieldRepository.save(field);
		}
	}

	private static final class DatasetCountTuple {
		private Long fieldId;
		private int numDatasets;
	}
}
