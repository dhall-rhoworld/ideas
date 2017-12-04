package com.rho.rhover.web.dto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class UniAnomalyDtoRepositoryImpl implements UniAnomalyDtoRepository {
	
	@Autowired
	private DataSource dataSource;

	@Override
	public List<UniAnomalyDto> findByCheckRunId(Long checkRunId) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		String sql = 
				"select check_run_id, anomaly_id, field_id, field_name, anomalous_value, num_id_fields, id_field_names, id_field_values " +
				"from uni_anomaly_dto " +
				"where check_run_id = " + checkRunId + " " +
				"and anomalous_value <> 'null'";
		return jdbcTemplate.query(sql, new UniAnomalyDtoResultSetMapper());
	}

	private static final class UniAnomalyDtoResultSetMapper implements RowMapper<UniAnomalyDto> {

		@Override
		public UniAnomalyDto mapRow(ResultSet rs, int num) throws SQLException {
			UniAnomalyDto dto = new UniAnomalyDto(rs.getLong(1), rs.getLong(2), rs.getLong(3), rs.getString(4), rs.getString(5));
			int numIdFields = rs.getInt(6);
			String[] idFieldNames = rs.getString(7).split(",");
			String[] idFieldValues = rs.getString(8).split(",");
			for (int i = 0; i < numIdFields; i++) {
				dto.addIdFieldNameAndValue(idFieldNames[i], idFieldValues[i]);
			}
			return dto;
		}
		
	}
}
