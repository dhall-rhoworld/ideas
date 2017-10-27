package com.rho.rhover.anomaly;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

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
				"select s.study_id, s.study_name,\r\n" + 
				"(\r\n" + 
				"	select count(*)\r\n" + 
				"	from anomaly a\r\n" + 
				"	join data_field df on df.data_field_id = a.data_field_id\r\n" + 
				"	join dataset ds on ds.dataset_id = df.dataset_id\r\n" + 
				"	where ds.study_id = s.study_id\r\n" + 
				"   and a.is_an_issue = 1\r\n" +
				"),\r\n" + 
				"(\r\n" + 
				"	select count(*)\r\n" + 
				"	from anomaly a\r\n" + 
				"	join data_field df on df.data_field_id = a.data_field_id\r\n" + 
				"	join dataset ds on ds.dataset_id = df.dataset_id\r\n" + 
				"	where ds.study_id = s.study_id\r\n" + 
				"	and a.has_been_viewed = 0\r\n" + 
				"   and a.is_an_issue = 1\r\n" +
				")\r\n" + 
				"from study s";
		return removeEmpties(jdbcTemplate.query(sql, new AnomalySummaryRowMapper()));
	}
	
	public Iterable<AnomalySummary> getDatasetSummaries(Long studyId) {
		String sql =
				"select ds.dataset_id, ds.dataset_name,\r\n" + 
				"(\r\n" + 
				"	select count(*)\r\n" + 
				"	from anomaly a\r\n" + 
				"	join data_field df on df.data_field_id = a.data_field_id\r\n" + 
				"	where df.dataset_id = ds.dataset_id\r\n" + 
				"   and a.is_an_issue = 1\r\n" +
				"),\r\n" + 
				"(\r\n" + 
				"	select count(*)\r\n" + 
				"	from anomaly a\r\n" + 
				"	join data_field df on df.data_field_id = a.data_field_id\r\n" + 
				"	where df.dataset_id = ds.dataset_id\r\n" + 
				"   and a.is_an_issue = 1\r\n" +
				"	and a.has_been_viewed = 0\r\n" + 
				")\r\n" + 
				"from dataset ds \r\n" + 
				"where ds.study_id = " + studyId;
		return removeEmpties(jdbcTemplate.query(sql, new AnomalySummaryRowMapper()));
	}
	
	public Iterable<AnomalySummary> getDataFieldSummaries(Long datasetId) {
		String sql =
				"select df.data_field_id, df.data_field_name,\r\n" + 
				"(\r\n" + 
				"	select count(*)\r\n" + 
				"	from anomaly a\r\n" + 
				"	where a.data_field_id = df.data_field_id\r\n" + 
				"   and a.is_an_issue = 1\r\n" +
				"),\r\n" + 
				"(\r\n" + 
				"	select count(*)\r\n" + 
				"	from anomaly a\r\n" + 
				"	where a.data_field_id = df.data_field_id\r\n" + 
				"   and a.is_an_issue = 1\r\n" +
				"	and a.has_been_viewed = 0\r\n" + 
				")\r\n" + 
				"from data_field df \r\n" + 
				"where df.dataset_id = " + datasetId;
		return removeEmpties(jdbcTemplate.query(sql, new AnomalySummaryRowMapper()));
	}
	
	private Iterable<AnomalySummary> removeEmpties(Iterable<AnomalySummary> summaries) {
		Iterator<AnomalySummary> it = summaries.iterator();
		while (it.hasNext()) {
			AnomalySummary summary = it.next();
			if (summary.getNumAnomalies() == 0) {
				it.remove();
			}
		}
		return summaries;
	}
	
	private static class AnomalySummaryRowMapper implements RowMapper<AnomalySummary> {
		public AnomalySummary mapRow(ResultSet rs, int p) throws SQLException {
			return new AnomalySummary(rs.getLong(1), rs.getString(2), rs.getInt(3), rs.getInt(4));
		}
	}
}
