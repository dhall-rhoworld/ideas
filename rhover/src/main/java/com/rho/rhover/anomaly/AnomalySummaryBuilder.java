package com.rho.rhover.anomaly;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

/**
 * Builds summmaries of anomalies by querying the anomaly database.
 * @author dhall
 *
 */
@Service
public class AnomalySummaryBuilder {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	public AnomalySummaryBuilder() {
		
	}
	
	/**
	 * Get anomaly summaries across all studies.
	 * @return Zero or more anomaly summaries.
	 */
	public Iterable<AnomalySummary> getStudySummaries() {
		String sql =
				"select s.study_id, s.study_name, count(a.anomaly_id) num_anomalies, count(a.has_been_viewed = 0) num_unviewed " +
				"from study s " +
				"join dataset ds on ds.study_id = s.study_id " +
				"join data_field df on df.dataset_id = ds.dataset_id " +
				"join anomaly a on a.data_field_id = df.data_field_id " +
				"group by s.study_id, s.study_name";
		return jdbcTemplate.query(sql, new AnomalySummaryRowMapper());
	}
	
	public Iterable<AnomalySummary> getDatasetSummaries(Long studyId) {
		String sql =
				"select ds.dataset_id, ds.dataset_name, count(a.anomaly_id), count(a.has_been_viewed = 0) " +
				"from dataset ds " +
				"join data_field df on df.dataset_id = ds.dataset_id " +
				"join anomaly a on a.data_field_id = df.data_field_id " +
				"where ds.study_id = " + studyId + " " +
				"group by ds.dataset_id, ds.dataset_name";
		return jdbcTemplate.query(sql, new AnomalySummaryRowMapper());
	}
	
	public Iterable<AnomalySummary> getDataFieldSummaries(Long datasetId) {
		String sql =
				"select df.data_field_id, df.data_field_name, count(a.anomaly_id), count(a.has_been_viewed = 0) " +
				"from data_field df " +
				"join anomaly a on a.data_field_id = df.data_field_id " +
				"where df.dataset_id = " + datasetId + " " +
				"group by df.data_field_id, df.data_field_name";
		return jdbcTemplate.query(sql, new AnomalySummaryRowMapper());
	}
	
	private static class AnomalySummaryRowMapper implements RowMapper<AnomalySummary> {
		public AnomalySummary mapRow(ResultSet rs, int p) throws SQLException {
			return new AnomalySummary(rs.getLong(1), rs.getString(2), rs.getInt(3), rs.getInt(4));
		}
	}
}
