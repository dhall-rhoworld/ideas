package com.rho.rhover.anomaly;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class AnomalyRepositoryImpl implements AnomalyRepository {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public Iterable<Anomaly> getCurrentAnomalies(Long dataFieldId) {
		
		// Get most recent dataset version
		String sql = "select max(version_last_seen_in) from anomaly where data_field_id = " + dataFieldId;
		Long version = jdbcTemplate.queryForObject(sql, Long.class);
		
		// Get anomalies
		sql =
				"select a.recruit_id, a.event, a.field_value, dsv.dataset_version_name\r\n" + 
				"from anomaly a\r\n" + 
				"join dataset_version dsv on a.version_first_seen_in = dsv.dataset_version_id\r\n" + 
				"where a.data_field_id = " + dataFieldId + "\r\n" + 
				"and a.version_last_seen_in = " + version + "\r\n" +
				"order by a.event, a.recruit_id";
		return jdbcTemplate.query(sql, new RowMapper<Anomaly>() {
			public Anomaly mapRow(ResultSet rs, int p) throws SQLException {
				return new Anomaly(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4));
			}
		});
	}
}
