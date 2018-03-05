package com.rho.rhover.web.service;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.rho.rhover.common.study.Study;

@Component
public class QueryFilterHelperImpl implements QueryFilterHelper {

	@Autowired
	private DataSource dataSource;

	@Override
	public String getFilterOptionsAsJSON(Study study) {
		return
				"{" +
				"statuses: " + getQueryStatusFilterOptionsAsJSON(study) + "," +
				"datasets: " + getDatasetFilterOptionsAsJSON(study) + "," +
				"sites: " + getSiteFilterOptionsAsJSON(study) + "," +
				"phases: " + getPhaseFilterOptionsAsJSON(study) + "," +
				"subjects: " + getSubjectFilterOptionsAsJSON(study) + "," +
				"fields: " + getFieldFilterOptionsAsJSON(study) +
				"}";
	}
	
	private String getQueryStatusFilterOptionsAsJSON(Study study) {
		String sql =
				"select qs.query_status_id, qs.query_status_name\r\n" + 
				"from query_status qs\r\n" + 
				"join query_candidate qc on qc.query_status_id = qs.query_status_id\r\n" + 
				"join anomaly a on a.anomaly_id = qc.anomaly_id\r\n" + 
				"join field f on f.field_id = a.field_id\r\n" + 
				"where f.study_id = " + study.getStudyId() + "\r\n" + 
				"group by qs.query_status_id, qs.query_status_name";
		return getAsJSON(sql);
	}
	
	private String getDatasetFilterOptionsAsJSON(Study study) {
		String sql =
				"select ds.dataset_id, ds.dataset_name\r\n" + 
				"from query_candidate qc\r\n" + 
				"join anomaly a on a.anomaly_id = qc.anomaly_id\r\n" + 
				"join field_instance fi on fi.field_instance_id = a.field_instance_id\r\n" + 
				"join dataset ds on ds.dataset_id = fi.dataset_id\r\n" + 
				"where ds.study_id = " + study.getStudyId() + "\r\n" + 
				"group by ds.dataset_id, ds.dataset_name";
		return getAsJSON(sql);
	}
	
	private String getSiteFilterOptionsAsJSON(Study study) {
		String sql =
				"select s.site_id, s.site_name\r\n" + 
				"from query_candidate qc\r\n" + 
				"join anomaly a on a.anomaly_id = qc.anomaly_id\r\n" + 
				"join site s on s.site_id = a.site_id\r\n" + 
				"where s.study_id = " + study.getStudyId() + "\r\n" + 
				"group by s.site_id, s.site_name\r\n" + 
				";\r\n" + 
				"";
		return getAsJSON(sql);
	}
	
	private String getPhaseFilterOptionsAsJSON(Study study) {
		String sql =
				"select p.phase_id, p.phase_name\r\n" + 
				"from query_candidate qc\r\n" + 
				"join anomaly a on a.anomaly_id = qc.anomaly_id\r\n" + 
				"join phase p on p.phase_id = a.phase_id\r\n" + 
				"where p.study_id = " + study.getStudyId() + "\r\n" + 
				"group by p.phase_id, p.phase_name";
		return getAsJSON(sql);
	}
	
	private String getSubjectFilterOptionsAsJSON(Study study) {
		String sql =
				"select s.subject_id, s.subject_name\r\n" + 
				"from query_candidate qc\r\n" + 
				"join anomaly a on a.anomaly_id = qc.anomaly_id\r\n" + 
				"join subject s on s.subject_id = a.subject_id\r\n" + 
				"join site si on si.site_id = s.site_id\r\n" + 
				"where si.study_id = " + study.getStudyId() + "\r\n" + 
				"group by s.subject_id, s.subject_name";
		return getAsJSON(sql);
	}
	
	private String getFieldFilterOptionsAsJSON(Study study) {
		String sql =
				"select f.field_id, f.field_label\r\n" + 
				"from query_candidate qc\r\n" + 
				"join anomaly a on a.anomaly_id = qc.anomaly_id\r\n" + 
				"join field f on f.field_id = a.field_id\r\n" + 
				"where f.study_id = " + study.getStudyId() + "\r\n" + 
				"group by f.field_id, f.field_label";
		return getAsJSON(sql);
	}
	
	private String getAsJSON(String sql) {
		JdbcTemplate template = new JdbcTemplate(dataSource);
		return toJSONArray(template.query(sql, new QueryOptionRowMapper()));
	}
	
	private String toJSONArray(Iterable<IdAndName> options) {
		StringBuilder builder = new StringBuilder("[");
		int count = 0;
		for (IdAndName option : options) {
			count++;
			if (count > 1) {
				builder.append(",");
			}
			builder.append(option.toJSON());
		}
		builder.append("]");
		return builder.toString();
	}
	
	private static class IdAndName {
		String id;
		String name;

		private String toJSON() {
			return "{id: '" + id + "', name: '" + name + "'}";
		}
	}

	private static class QueryOptionRowMapper implements RowMapper<IdAndName> {

		@Override
		public IdAndName mapRow(ResultSet rs, int p) throws SQLException {
			IdAndName idAndName = new IdAndName();
			idAndName.id = rs.getString(1);
			idAndName.name = rs.getString(2);
			return idAndName;
		}
		
	}
}
