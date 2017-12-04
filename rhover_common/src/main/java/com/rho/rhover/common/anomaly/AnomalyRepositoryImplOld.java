package com.rho.rhover.common.anomaly;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.rho.rhover.common.study.Site;
import com.rho.rhover.common.study.Subject;

@Service
public class AnomalyRepositoryImplOld implements AnomalyRepositoryOld {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public Iterable<AnomalyOld> getCurrentAnomalies(Long dataFieldId) {
		
		// Get most recent dataset version
		String sql = "select max(version_last_seen_in) from anomaly where data_field_id = " + dataFieldId;
		Long version = jdbcTemplate.queryForObject(sql, Long.class);
		
		// Get anomalies
		sql =
				"select a.anomaly_id, a.recruit_id, a.event, a.field_value, dsv.dataset_version_name, s.site_name\r\n" + 
				"from anomaly a\r\n" + 
				"join dataset_version dsv on a.version_first_seen_in = dsv.dataset_version_id\r\n" + 
				"join site s on s.site_id = a.site_id\r\n" +
				"where a.data_field_id = " + dataFieldId + "\r\n" + 
				"and a.version_last_seen_in = " + version + "\r\n" +
				"and a.is_an_issue = 1\r\n" +
				"order by a.event, a.recruit_id";
		return jdbcTemplate.query(sql, new RowMapper<AnomalyOld>() {
			public AnomalyOld mapRow(ResultSet rs, int p) throws SQLException {
				return new AnomalyOld(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6));
			}
		});
	}
	
	@Override
	public Iterable<AnomalyOld> getCurrentAnomalies(Long dataFieldId, Site site) {
		
		// Get most recent dataset version
		String sql = "select max(version_last_seen_in) from anomaly where data_field_id = " + dataFieldId;
		Long version = jdbcTemplate.queryForObject(sql, Long.class);
		
		// Get anomalies
		sql =
				"select a.anomaly_id, a.recruit_id, a.event, a.field_value, dsv.dataset_version_name, s.site_name\r\n" + 
				"from anomaly a\r\n" + 
				"join dataset_version dsv on a.version_first_seen_in = dsv.dataset_version_id\r\n" + 
				"join site s on s.site_id = a.site_id\r\n" +
				"where a.data_field_id = " + dataFieldId + "\r\n" + 
				"and a.version_last_seen_in = " + version + "\r\n" +
				"and a.is_an_issue = 1\r\n" +
				"and a.site_id = " + site.getSiteId() + "\r\n" +
				"order by a.event, a.recruit_id";
		return jdbcTemplate.query(sql, new RowMapper<AnomalyOld>() {
			public AnomalyOld mapRow(ResultSet rs, int p) throws SQLException {
				return new AnomalyOld(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6));
			}
		});
	}
	

	@Override
	public Iterable<AnomalyOld> getCurrentAnomalies(Long dataFieldId, Subject subject) {
		
		// Get most recent dataset version
		String sql = "select max(version_last_seen_in) from anomaly where data_field_id = " + dataFieldId;
		Long version = jdbcTemplate.queryForObject(sql, Long.class);
		
		// Get anomalies
		sql =
				"select a.anomaly_id, a.recruit_id, a.event, a.field_value, dsv.dataset_version_name, s.site_name\r\n" + 
				"from anomaly a\r\n" + 
				"join dataset_version dsv on a.version_first_seen_in = dsv.dataset_version_id\r\n" + 
				"join site s on s.site_id = a.site_id\r\n" +
				"where a.data_field_id = " + dataFieldId + "\r\n" + 
				"and a.version_last_seen_in = " + version + "\r\n" +
				"and a.is_an_issue = 1\r\n" +
				"and a.subject_id = " + subject.getSubjectId() + "\r\n" +
				"order by a.event, a.recruit_id";
		return jdbcTemplate.query(sql, new RowMapper<AnomalyOld>() {
			public AnomalyOld mapRow(ResultSet rs, int p) throws SQLException {
				return new AnomalyOld(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6));
			}
		});
	}

	@Override
	public int setIsAnIssue(Iterable<Long> anomalyIds, boolean isAnIssue) {
		int isAnIssueValue = 0;
		if (isAnIssue) {
			isAnIssueValue = 1;
		}
		StringBuilder builder = new StringBuilder("update anomaly set is_an_issue = "
				+ isAnIssueValue + " where anomaly_id in (");
		int count = 0;
		for (Long anomalyId : anomalyIds) {
			count++;
			if (count > 1) {
				builder.append(",");
			}
			builder.append(anomalyId);
		}
		builder.append(")");
		String sql = builder.toString();
		//logger.debug(sql);
		//return 1;
		return jdbcTemplate.update(sql);
	}

	@Override
	public int setIsAnIssue(Long dataFieldId, String[] recruitIds, String[] events, boolean isAnIssue) {
		int isAnIssueValue = 0;
		if (isAnIssue) {
			isAnIssueValue = 1;
		}
		for (int i = 0; i < recruitIds.length; i++) {
			String sql =
				"update anomaly " +
				"set is_an_issue = " + isAnIssueValue + " " +
				"where data_field_id = " + dataFieldId + " " +
				"and recruit_id = '" + recruitIds[i] + "' " +
				"and event = '" + events[i] + "'";
			jdbcTemplate.update(sql);
		}
		return recruitIds.length;
	}
}
