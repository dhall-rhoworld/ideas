package com.rho.rhover.web.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.rho.rhover.web.dto.JqueryUiAutocompleteDto;

@Service
public class AutocompleteHelperServiceImpl implements AutocompleteHelperService {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private DataSource dataSource;

	@Override
	public List<JqueryUiAutocompleteDto> findMatchingFieldInstances(String fragment, Long studyId) {
		String sql =
				"select f.field_name, f.field_label, fi.field_instance_id, ds.dataset_name\r\n" + 
				"from field f\r\n" + 
				"join field_instance fi on fi.field_id = f.field_id\r\n" + 
				"join dataset ds on ds.dataset_id = fi.dataset_id\r\n" + 
				"where f.study_id = " + studyId + "\r\n" + 
				"and (lower(f.field_name) like lower('%" + fragment + "%')\r\n" + 
				"or lower(f.field_label) like lower('%" + fragment + "%'))";
		//logger.debug(sql);
		JdbcTemplate template = new JdbcTemplate(dataSource);
		return template.query(sql,  new FieldInstanceRowMapper());
	}

	private static final class FieldInstanceRowMapper implements RowMapper<JqueryUiAutocompleteDto> {

		@Override
		public JqueryUiAutocompleteDto mapRow(ResultSet rs, int p) throws SQLException {
			JqueryUiAutocompleteDto dto = new JqueryUiAutocompleteDto();
			String fieldName = rs.getString(1);
			String fieldLabel = rs.getString(2);
			String datasetName = rs.getString(4);
			if (fieldLabel != null && fieldLabel.trim().length() > 0) {
				dto.setLabel(fieldLabel + " (" + fieldName + "a) [" + datasetName + "]");
			}
			else {
				dto.setLabel(fieldName + " (" + fieldName + "a) [" + datasetName + "]");
			}
			dto.setValue(dto.getLabel());
			return dto;
		}
		
	}
}
