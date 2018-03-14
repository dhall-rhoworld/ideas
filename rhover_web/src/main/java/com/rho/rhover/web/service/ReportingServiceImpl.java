package com.rho.rhover.web.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.rho.rhover.common.study.StudyDbVersion;
import com.rho.rhover.web.reporting.DatasetLoadOverview;
import com.rho.rhover.web.reporting.FailedLoadOverview;
import com.rho.rhover.web.reporting.StudyEventOverview;
import com.rho.rhover.web.reporting.StudyLoadOverview;

@Component
public class ReportingServiceImpl implements ReportingService {
	
	@Autowired
	private DataSource dataSource;

	@Override
	public List<StudyEventOverview> getStudyEventOverviews() {
		JdbcTemplate template = new JdbcTemplate(dataSource);
		List<StudyEventOverview> overviews = new ArrayList<StudyEventOverview>();
		String sql =
				"select sdv.load_started, sdv.load_stopped, s.study_name,\r\n" + 
				"(\r\n" + 
				"	select count(*)\r\n" + 
				"	from study_db_version_config sdvc\r\n" + 
				"	where sdvc.study_db_version_id = sdv.study_db_version_id\r\n" + 
				") total_datasets,\r\n" + 
				"(\r\n" + 
				"	select count(*)\r\n" + 
				"	from dataset_modification dm\r\n" + 
				"	where dm.study_db_version_id = sdv.study_db_version_id\r\n" + 
				"	and dm.is_new = 1\r\n" + 
				") new_datasets,\r\n" + 
				"(\r\n" + 
				"	select count(*)\r\n" + 
				"	from dataset_modification dm\r\n" + 
				"	where dm.study_db_version_id = sdv.study_db_version_id\r\n" + 
				"	and dm.is_modified = 1\r\n" + 
				") modified_datasets, sdv.study_db_version_id,\r\n" + 
				"(\r\n" + 
				"	select count(*)\r\n" + 
				"	from loader_issue li\r\n" + 
				"	where li.study_db_version_id = sdv.study_db_version_id\r\n" + 
				") num_issues\r\n" + 
				"from study_db_version sdv\r\n" + 
				"join study s on s.study_id = sdv.study_id\r\n" + 
				"order by sdv.study_db_version_id desc";
		overviews.addAll(template.query(sql, new RowMapper<StudyEventOverview>() {
			public StudyLoadOverview mapRow(ResultSet rs, int p) throws SQLException {
				StudyLoadOverview overview = new StudyLoadOverview();
				overview.setEventStarted(rs.getTimestamp(1));
				overview.setEventStopped(rs.getTimestamp(2));
				overview.setStudyName(rs.getString(3));
				overview.setTotalDatasets(rs.getInt(4));
				overview.setNumNewDatasets(rs.getInt(5));
				overview.setNumModifiedDatasets(rs.getInt(6));
				overview.setStudyDbVersionId(rs.getLong(7));
				overview.setNumIssues(rs.getInt(8));
				return overview;
			}
		}));
		
		sql =
				"select s.study_name, li.last_modified, li.message\r\n" + 
				"from loader_issue li\r\n" + 
				"join study s on s.study_id = li.study_id\r\n" + 
				"where li.issue_level = 'STUDY'";
		overviews.addAll(template.query(sql, new RowMapper<StudyEventOverview>() {
			public StudyEventOverview mapRow(ResultSet rs, int p) throws SQLException {
				FailedLoadOverview overview = new FailedLoadOverview();
				overview.setErrorMessage(rs.getString(3));
				overview.setEventStarted(rs.getTimestamp(2));
				overview.setEventStopped(rs.getTimestamp(2));
				overview.setNumIssues(0);
				overview.setStudyName(rs.getString(1));
				return overview;
			}
		}));
		
		Collections.sort(overviews, new Comparator<StudyEventOverview>() {
			public int compare(StudyEventOverview o1, StudyEventOverview o2) {
				return -(o1.getEventStarted().compareTo(o2.getEventStarted()));
			}
		});
		return overviews;
	}

	@Override
	public List<DatasetLoadOverview> getAllDatasetLoadOverviews(StudyDbVersion studyDbVersion) {
		JdbcTemplate template = new JdbcTemplate(dataSource);
		String sql =
				"select ds.dataset_name,\r\n" + 
				"(\r\n" + 
				"	select count(*)\r\n" + 
				"	from field_instance fi\r\n" + 
				"	where fi.first_dataset_version_id = dv.dataset_version_id\r\n" + 
				") new_fields,\r\n" + 
				"(\r\n" + 
				"	select count(*)\r\n" + 
				"	from observation o\r\n" + 
				"	where o.first_dataset_version_id = dv.dataset_version_id\r\n" + 
				") new_records,\r\n" + 
				
				// TODO: Modify subquery to utilize datum_change table
				"(\r\n" + 
				"	select count(*)\r\n" + 
				"	from datum_version dtv\r\n" + 
				"	join datum d on d.datum_id = dtv.datum_id\r\n" + 
				"	where dtv.first_dataset_version_id = dv.dataset_version_id\r\n" + 
				"	and d.first_dataset_version_id <> dv.dataset_version_id\r\n" + 
				") modified_values, dv.dataset_version_id\r\n" + 
				"from study_db_version_config sdvc\r\n" + 
				"join dataset_version dv on dv.dataset_version_id = sdvc.dataset_version_id\r\n" + 
				"join dataset ds on ds.dataset_id = dv.dataset_id\r\n" + 
				"where sdvc.study_db_version_id = " + studyDbVersion.getStudyDbVersionId();
		return template.query(sql, new DatasetLoadRowMapper());
	}

	@Override
	public List<DatasetLoadOverview> getNewDatasetLoadOverviews(StudyDbVersion studyDbVersion) {
		JdbcTemplate template = new JdbcTemplate(dataSource);
		String sql =
				"select ds.dataset_name,\r\n" + 
				"(\r\n" + 
				"	select count(*)\r\n" + 
				"	from field_instance fi\r\n" + 
				"	where fi.first_dataset_version_id = dv.dataset_version_id\r\n" + 
				") new_fields,\r\n" + 
				"(\r\n" + 
				"	select count(*)\r\n" + 
				"	from observation o\r\n" + 
				"	where o.first_dataset_version_id = dv.dataset_version_id\r\n" + 
				") new_records,\r\n" + 
				
				// TODO: Modify subquery to utilize datum_change table
				"(\r\n" + 
				"	select count(*)\r\n" + 
				"	from datum_version dtv\r\n" + 
				"	join datum d on d.datum_id = dtv.datum_id\r\n" + 
				"	where dtv.first_dataset_version_id = dv.dataset_version_id\r\n" + 
				"	and d.first_dataset_version_id <> dv.dataset_version_id\r\n" + 
				") modified_values, dv.dataset_version_id\r\n" + 
				"from study_db_version_config sdvc\r\n" + 
				"join dataset_version dv on dv.dataset_version_id = sdvc.dataset_version_id\r\n" + 
				"join dataset ds on ds.dataset_id = dv.dataset_id\r\n" + 
				"where sdvc.study_db_version_id = " + studyDbVersion.getStudyDbVersionId() + "\r\n" + 
				"and ds.dataset_id in\r\n" + 
				"(\r\n" + 
				"	select dm.dataset_id\r\n" + 
				"	from dataset_modification dm\r\n" + 
				"	where dm.study_db_version_id = " + studyDbVersion.getStudyDbVersionId() + "\r\n" + 
				"	and dm.is_new = 1\r\n" + 
				")";
		return template.query(sql, new DatasetLoadRowMapper());
	}

	@Override
	public List<DatasetLoadOverview> getModifiedDatasetLoadOverviews(StudyDbVersion studyDbVersion) {
		JdbcTemplate template = new JdbcTemplate(dataSource);
		String sql =
				"select ds.dataset_name,\r\n" + 
				"(\r\n" + 
				"	select count(*)\r\n" + 
				"	from field_instance fi\r\n" + 
				"	where fi.first_dataset_version_id = dv.dataset_version_id\r\n" + 
				") new_fields,\r\n" + 
				"(\r\n" + 
				"	select count(*)\r\n" + 
				"	from observation o\r\n" + 
				"	where o.first_dataset_version_id = dv.dataset_version_id\r\n" + 
				") new_records,\r\n" + 
				
                // TODO: Modify subquery to utilize datum_change table
				"(\r\n" + 
				"	select count(*)\r\n" + 
				"	from datum_version dtv\r\n" + 
				"	join datum d on d.datum_id = dtv.datum_id\r\n" + 
				"	where dtv.first_dataset_version_id = dv.dataset_version_id\r\n" + 
				"	and d.first_dataset_version_id <> dv.dataset_version_id\r\n" + 
				") modified_values, dv.dataset_version_id\r\n" + 
				"from study_db_version_config sdvc\r\n" + 
				"join dataset_version dv on dv.dataset_version_id = sdvc.dataset_version_id\r\n" + 
				"join dataset ds on ds.dataset_id = dv.dataset_id\r\n" + 
				"where sdvc.study_db_version_id = " + studyDbVersion.getStudyDbVersionId() + "\r\n" + 
				"and ds.dataset_id in\r\n" + 
				"(\r\n" + 
				"	select dm.dataset_id\r\n" + 
				"	from dataset_modification dm\r\n" + 
				"	where dm.study_db_version_id = " + studyDbVersion.getStudyDbVersionId() + "\r\n" + 
				"	and dm.is_modified = 1\r\n" + 
				")";
		return template.query(sql, new DatasetLoadRowMapper());
	}

	private static class DatasetLoadRowMapper implements RowMapper<DatasetLoadOverview> {
		public DatasetLoadOverview mapRow(ResultSet rs, int p) throws SQLException {
			DatasetLoadOverview overview = new DatasetLoadOverview();
			overview.setDatasetName(rs.getString(1));
			overview.setNumNewFields(rs.getInt(2));
			overview.setNumNewRecords(rs.getInt(3));
			overview.setNumModifiedDataValues(rs.getInt(4));
			overview.setDatasetVersionId(rs.getLong(5));
			return overview;
		}
	}
}
