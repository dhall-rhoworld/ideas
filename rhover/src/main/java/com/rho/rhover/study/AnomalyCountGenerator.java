package com.rho.rhover.study;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class AnomalyCountGenerator {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	public AnomalyCountGenerator() {
		
	}
	
	public Iterable<AnomalyCount> getStudyCounts() {
		String sql =
				"select s.study_id, s.study_name, count(a.anomaly_id) num_anomalies, count(a.has_been_viewed = 0) num_unviewed " +
				"from study s " +
				"join dataset ds on ds.study_id = s.study_id " +
				"join data_field df on df.dataset_id = ds.dataset_id " +
				"join anomaly a on a.data_field_id = df.data_field_id " +
				"group by s.study_id, s.study_name";
		return jdbcTemplate.query(sql, new RowMapper<AnomalyCount>() {
			public AnomalyCount mapRow(ResultSet rs, int p) throws SQLException {
				return new AnomalyCount(rs.getLong(1), rs.getString(2), rs.getInt(3), rs.getInt(4));
			}
		});
	}
}
