package com.rho.rhover.web.dto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.rho.rhover.common.study.Site;
import com.rho.rhover.common.study.Study;
import com.rho.rhover.common.study.Subject;

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
	public List<AnomalySummary> getStudySummaries() {
		String sql =
				"select s.study_id, s.study_name,\r\n" + 
				"(\r\n" + 
				"	select count(*)\r\n" + 
				"	from anomaly a\r\n" + 
				"	join anomaly_check_run acr on acr.anomaly_id = a.anomaly_id\r\n" + 
				"	join check_run cr on cr.check_run_id = acr.check_run_id\r\n" + 
				"	join dataset_version dv on dv.dataset_version_id = cr.dataset_version_id\r\n" + 
				"	join dataset ds on ds.dataset_id = dv.dataset_id\r\n" + 
				"	where ds.study_id = s.study_id\r\n" + 
				"	and cr.is_latest = 1\r\n" + 
				"	and a.is_an_issue = 1\r\n" + 
				") total,\r\n" + 
				"(\r\n" + 
				"	select count(*)\r\n" + 
				"	from anomaly a\r\n" + 
				"	join anomaly_check_run acr on acr.anomaly_id = a.anomaly_id\r\n" + 
				"	join check_run cr on cr.check_run_id = acr.check_run_id\r\n" + 
				"	join dataset_version dv on dv.dataset_version_id = cr.dataset_version_id\r\n" + 
				"	join dataset ds on ds.dataset_id = dv.dataset_id\r\n" + 
				"	where ds.study_id = s.study_id\r\n" + 
				"	and cr.is_latest = 1\r\n" + 
				"	and a.is_an_issue = 1\r\n" + 
				"	and a.has_been_viewed = 0\r\n" + 
				") unviewed\r\n" + 
				"from study s";
		return removeEmpties(jdbcTemplate.query(sql, new AnomalySummaryRowMapper()));
	}
	
	public List<AnomalySummary> getDatasetSummaries(Study study) {
		String sql =
				"select ds.dataset_id, ds.dataset_name,\r\n" + 
				"(\r\n" + 
				"	select count(*)\r\n" + 
				"	from anomaly a\r\n" + 
				"	join anomaly_check_run acr on acr.anomaly_id = a.anomaly_id\r\n" + 
				"	join check_run cr on cr.check_run_id = acr.check_run_id\r\n" + 
				"	join dataset_version dv on dv.dataset_version_id = cr.dataset_version_id\r\n" + 
				"	where dv.dataset_id = ds.dataset_id\r\n" + 
				"	and cr.is_latest = 1\r\n" + 
				"	and a.is_an_issue = 1\r\n" + 
				") total,\r\n" + 
				"(\r\n" + 
				"	select count(*)\r\n" + 
				"	from anomaly a\r\n" + 
				"	join anomaly_check_run acr on acr.anomaly_id = a.anomaly_id\r\n" + 
				"	join check_run cr on cr.check_run_id = acr.check_run_id\r\n" + 
				"	join dataset_version dv on dv.dataset_version_id = cr.dataset_version_id\r\n" + 
				"	where dv.dataset_id = ds.dataset_id\r\n" + 
				"	and cr.is_latest = 1\r\n" + 
				"	and a.is_an_issue = 1\r\n" + 
				"	and a.has_been_viewed = 0\r\n" + 
				") unviewed\r\n" + 
				"from dataset ds\r\n" + 
				"where ds.study_id = " + study.getStudyId();
		return removeEmpties(jdbcTemplate.query(sql, new AnomalySummaryRowMapper()));
	}
	
	public List<AnomalySummary> getDatasetSummaries(Site site) {
		String sql = 
				"select ds.dataset_id, ds.dataset_name,\r\n" + 
				"(\r\n" + 
				"	select count(*)\r\n" + 
				"	from anomaly a\r\n" + 
				"	join anomaly_check_run acr on acr.anomaly_id = a.anomaly_id\r\n" + 
				"	join check_run cr on cr.check_run_id = acr.check_run_id\r\n" + 
				"	join dataset_version dv on dv.dataset_version_id = cr.dataset_version_id\r\n" +
				"	where dv.dataset_id = ds.dataset_id\r\n" + 
				"	and cr.is_latest = 1\r\n" + 
				"	and a.is_an_issue = 1\r\n" + 
				"	and a.site_id = " + site.getSiteId() + "\r\n" + 
				") total,\r\n" + 
				"(\r\n" + 
				"	select count(*)\r\n" + 
				"	from anomaly a\r\n" + 
				"	join anomaly_check_run acr on acr.anomaly_id = a.anomaly_id\r\n" + 
				"	join check_run cr on cr.check_run_id = acr.check_run_id\r\n" + 
				"	join dataset_version dv on dv.dataset_version_id = cr.dataset_version_id\r\n" +
				"	where dv.dataset_id = ds.dataset_id\r\n" + 
				"	and cr.is_latest = 1\r\n" + 
				"	and a.is_an_issue = 1\r\n" + 
				"	and a.site_id = " + site.getSiteId() + "\r\n" + 
				"	and a.has_been_viewed = 0\r\n" + 
				") unviewed\r\n" + 
				"from dataset ds\r\n" + 
				"where ds.study_id = " + site.getStudy().getStudyId();
		return removeEmpties(jdbcTemplate.query(sql, new AnomalySummaryRowMapper()));
	}
	
	public List<AnomalySummary> getDatasetSummaries(Subject subject) {
		String sql = 
				"select ds.dataset_id, ds.dataset_name,\r\n" + 
				"(\r\n" + 
				"	select count(*)\r\n" + 
				"	from anomaly a\r\n" + 
				"	join anomaly_check_run acr on acr.anomaly_id = a.anomaly_id\r\n" + 
				"	join check_run cr on cr.check_run_id = acr.check_run_id\r\n" + 
				"	join dataset_version dv on dv.dataset_version_id = cr.dataset_version_id\r\n" +
				"	where dv.dataset_id = ds.dataset_id\r\n" + 
				"	and cr.is_latest = 1\r\n" + 
				"	and a.is_an_issue = 1\r\n" + 
				"	and a.subject_id = " + subject.getSubjectId() + "\r\n" + 
				") total,\r\n" + 
				"(\r\n" + 
				"	select count(*)\r\n" + 
				"	from anomaly a\r\n" + 
				"	join anomaly_check_run acr on acr.anomaly_id = a.anomaly_id\r\n" + 
				"	join check_run cr on cr.check_run_id = acr.check_run_id\r\n" + 
				"	join dataset_version dv on dv.dataset_version_id = cr.dataset_version_id\r\n" +
				"	where dv.dataset_id = ds.dataset_id\r\n" + 
				"	and cr.is_latest = 1\r\n" + 
				"	and a.is_an_issue = 1\r\n" + 
				"	and a.subject_id = " + subject.getSubjectId() + "\r\n" + 
				"	and a.has_been_viewed = 0\r\n" + 
				") unviewed\r\n" + 
				"from dataset ds\r\n" + 
				"where ds.study_id = " + subject.getSite().getStudy().getStudyId();
		return removeEmpties(jdbcTemplate.query(sql, new AnomalySummaryRowMapper()));
	}
	
	public List<AnomalySummary> getSiteSummaries(Long studyId) {
		String sql =
				"select s.site_id, s.site_name,\r\n" + 
				"(\r\n" + 
				"	select count(*)\r\n" + 
				"	from anomaly a\r\n" + 
				"	join anomaly_check_run acr on acr.anomaly_id = a.anomaly_id\r\n" + 
				"	join check_run cr on cr.check_run_id = acr.check_run_id\r\n" +
				"	where a.site_id = s.site_id\r\n" + 
				"	and cr.is_latest = 1\r\n" + 
				"	and a.is_an_issue = 1\r\n" + 
				") total,\r\n" + 
				"(\r\n" + 
				"select count(*)\r\n" + 
				"	from anomaly a\r\n" + 
				"	join anomaly_check_run acr on acr.anomaly_id = a.anomaly_id\r\n" + 
				"	join check_run cr on cr.check_run_id = acr.check_run_id\r\n" + 
				"	where a.site_id = s.site_id\r\n" + 
				"	and cr.is_latest = 1\r\n" + 
				"	and a.is_an_issue = 1\r\n" + 
				"	and a.has_been_viewed = 0\r\n" + 
				") unviewed\r\n" + 
				"from site s\r\n" + 
				"where s.study_id = " + studyId;
		return removeEmpties(jdbcTemplate.query(sql, new AnomalySummaryRowMapper()));
	}
	
	public List<AnomalySummary> getSubjectSummaries(Long studyId, int limit, int offset) {
		String sql = "select s.subject_id, s.subject_name,\r\n" + 
				"(\r\n" + 
				"	select count(*)\r\n" + 
				"	from anomaly a\r\n" + 
				"	join anomaly_check_run acr on acr.anomaly_id = a.anomaly_id\r\n" + 
				"	join check_run cr on cr.check_run_id = acr.check_run_id\r\n" +
				"	where a.subject_id = s.subject_id\r\n" + 
				"	and cr.is_latest = 1\r\n" + 
				"	and a.is_an_issue = 1\r\n" + 
				") total,\r\n" + 
				"(\r\n" + 
				"	select count(*)\r\n" + 
				"	from anomaly a\r\n" + 
				"	join anomaly_check_run acr on acr.anomaly_id = a.anomaly_id\r\n" + 
				"	join check_run cr on cr.check_run_id = acr.check_run_id\r\n" +
				"	where a.subject_id = s.subject_id\r\n" + 
				"	and cr.is_latest = 1\r\n" + 
				"	and a.is_an_issue = 1\r\n" + 
				"	and a.has_been_viewed = 0\r\n" + 
				"),\r\n" + 
				"si.site_name\r\n" + 
				"from subject s\r\n" + 
				"join site si on si.site_id = s.site_id\r\n" + 
				"where si.study_id = " + studyId + "\r\n" + 
				"order by total desc\r\n" + 
				"limit " + limit + " offset " + offset;
		return removeEmpties(jdbcTemplate.query(sql, new AnomalySummaryRowMapperWithAttribute()));
	}
	
	public List<AnomalySummary> getDataFieldSummaries(Long datasetId) {
		String sql =
				"select f.field_id, f.field_name,\r\n" + 
				"(\r\n" + 
				"	select count(*)\r\n" + 
				"	from anomaly a\r\n" + 
				"	join anomaly_check_run acr on acr.anomaly_id = a.anomaly_id\r\n" + 
				"	join check_run cr on cr.check_run_id = acr.check_run_id\r\n" + 
				"	where a.field_id = f.field_id\r\n" + 
				"	and a.is_an_issue = 1\r\n" + 
				"	and cr.is_latest = 1\r\n" + 
				") total,\r\n" + 
				"(\r\n" + 
				"	select count(*)\r\n" + 
				"	from anomaly a\r\n" + 
				"	join anomaly_check_run acr on acr.anomaly_id = a.anomaly_id\r\n" + 
				"	join check_run cr on cr.check_run_id = acr.check_run_id\r\n" + 
				"	where a.field_id = f.field_id\r\n" + 
				"	and a.is_an_issue = 1\r\n" + 
				"	and cr.is_latest = 1\r\n" + 
				") unviewed\r\n" + 
				"from field f\r\n" + 
				"join dataset_version_field dvf on dvf.field_id = f.field_id\r\n" + 
				"join dataset_version dv on dv.dataset_version_id = dvf.dataset_version_id\r\n" + 
				"where dv.dataset_id = " + datasetId;
		return removeEmpties(jdbcTemplate.query(sql, new AnomalySummaryRowMapper()));
	}
	
	public List<AnomalySummary> getDataFieldSummaries(Long datasetId, Site site) {
		String sql =
				"select f.field_id, f.field_name,\r\n" + 
				"(\r\n" + 
				"	select count(*)\r\n" + 
				"	from anomaly a\r\n" + 
				"	join anomaly_check_run acr on acr.anomaly_id = a.anomaly_id\r\n" + 
				"	join check_run cr on cr.check_run_id = acr.check_run_id\r\n" + 
				"	where a.field_id = f.field_id\r\n" + 
				"	and a.site_id = " + site.getSiteId() + "\r\n" + 
				"	and a.is_an_issue = 1\r\n" + 
				"	and cr.is_latest = 1\r\n" + 
				") total,\r\n" + 
				"(\r\n" + 
				"	select count(*)\r\n" + 
				"	from anomaly a\r\n" + 
				"	join anomaly_check_run acr on acr.anomaly_id = a.anomaly_id\r\n" + 
				"	join check_run cr on cr.check_run_id = acr.check_run_id\r\n" + 
				"	where a.field_id = f.field_id\r\n" + 
				"	and a.site_id = " + site.getSiteId() + "\r\n" + 
				"	and a.is_an_issue = 1\r\n" + 
				"	and cr.is_latest = 1\r\n" + 
				") unviewed\r\n" + 
				"from field f\r\n" + 
				"join dataset_version_field dvf on dvf.field_id = f.field_id\r\n" + 
				"join dataset_version dv on dv.dataset_version_id = dvf.dataset_version_id\r\n" + 
				"where dv.dataset_id = " + datasetId;
		return removeEmpties(jdbcTemplate.query(sql, new AnomalySummaryRowMapper()));
	}
	
	public List<AnomalySummary> getDataFieldSummaries(Long datasetId, Subject subject) {
		String sql =
				"select f.field_id, f.field_name,\r\n" + 
				"(\r\n" + 
				"	select count(*)\r\n" + 
				"	from anomaly a\r\n" + 
				"	join anomaly_check_run acr on acr.anomaly_id = a.anomaly_id\r\n" + 
				"	join check_run cr on cr.check_run_id = acr.check_run_id\r\n" + 
				"	where a.field_id = f.field_id\r\n" + 
				"	and a.subject_id = " + subject.getSubjectId() + "\r\n" + 
				"	and a.is_an_issue = 1\r\n" + 
				"	and cr.is_latest = 1\r\n" + 
				") total,\r\n" + 
				"(\r\n" + 
				"	select count(*)\r\n" + 
				"	from anomaly a\r\n" + 
				"	join anomaly_check_run acr on acr.anomaly_id = a.anomaly_id\r\n" + 
				"	join check_run cr on cr.check_run_id = acr.check_run_id\r\n" + 
				"	where a.field_id = f.field_id\r\n" + 
				"	and a.subject_id = " + subject.getSubjectId() + "\r\n" + 
				"	and a.is_an_issue = 1\r\n" + 
				"	and cr.is_latest = 1\r\n" + 
				") unviewed\r\n" + 
				"from field f\r\n" + 
				"join dataset_version_field dvf on dvf.field_id = f.field_id\r\n" + 
				"join dataset_version dv on dv.dataset_version_id = dvf.dataset_version_id\r\n" + 
				"where dv.dataset_id = " + datasetId;
		return removeEmpties(jdbcTemplate.query(sql, new AnomalySummaryRowMapper()));
	}
	
	public int numSubjectsWithAnomalies(Long studyId) {
		String sql = "select count(distinct(a.subject_id))\r\n" + 
				"from anomaly a\r\n" + 
				"join site s on s.site_id = a.site_id\r\n" + 
				"where s.study_id = " + studyId;
		return jdbcTemplate.queryForObject(sql, Integer.class);
	}
	
	private List<AnomalySummary> removeEmpties(List<AnomalySummary> summaries) {
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
	
	private static class AnomalySummaryRowMapperWithAttribute implements RowMapper<AnomalySummary> {
		public AnomalySummary mapRow(ResultSet rs, int p) throws SQLException {
			return new AnomalySummary(rs.getLong(1), rs.getString(2), rs.getInt(3), rs.getInt(4), rs.getString(5));
		}
	}
}
