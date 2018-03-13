package com.rho.rhover.web.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.rho.rhover.web.reporting.DataLoadOverview;

@Component
public class ReportingServiceImpl implements ReportingService {
	
	@Autowired
	private DataSource dataSource;

	@Override
	public List<DataLoadOverview> getDataLoadOverviews() {
		JdbcTemplate template = new JdbcTemplate(dataSource);
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
				"select count(*)\r\n" + 
				"	from dataset_modification dm\r\n" + 
				"	where dm.study_db_version_id = sdv.study_db_version_id\r\n" + 
				"	and dm.is_modified = 1\r\n" + 
				") modified_datasets\r\n" + 
				"from study_db_version sdv\r\n" + 
				"join study s on s.study_id = sdv.study_id";
		return template.query(sql, new RowMapper<DataLoadOverview>() {
			public DataLoadOverview mapRow(ResultSet rs, int p) throws SQLException {
				DataLoadOverview overview = new DataLoadOverview();
				overview.setLoadStarted(rs.getTimestamp(1));
				overview.setLoadStopped(rs.getTimestamp(2));
				overview.setStudyName(rs.getString(3));
				overview.setTotalDatasets(rs.getInt(4));
				overview.setNumNewDatasets(rs.getInt(5));
				overview.setNumModifiedDatasets(rs.getInt(6));
				return overview;
			}
		});
	}

}
