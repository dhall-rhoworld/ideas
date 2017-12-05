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
	
	private static final int BATCH_SIZE = 100;
	
	@Autowired
	private DataSource dataSource;

	@Override
	public List<UniAnomalyDto> findByCheckRunId(Long checkRunId) {
		String sql = 
				"select check_run_id, anomaly_id, field_id, field_name, anomalous_value, " +
				"id_field_names, id_field_values, subject_id, site_id " +
				"from uni_anomaly_dto " +
				"where check_run_id = " + checkRunId;
		return queryAndMarkAsVeiwed(sql);
	}
	
	@Override
	public List<UniAnomalyDto> findByCheckRunIdAndSiteId(Long checkRunId, Long siteId) {
		String sql = 
				"select check_run_id, anomaly_id, field_id, field_name, anomalous_value, " +
				"id_field_names, id_field_values, subject_id, site_id " +
				"from uni_anomaly_dto " +
				"where check_run_id = " + checkRunId + " " +
				"and site_id = " + siteId;
		return queryAndMarkAsVeiwed(sql);
	}
	
	@Override
	public List<UniAnomalyDto> findByCheckRunIdAndSubjectId(Long checkRunId, Long subjectId) {
		String sql = 
				"select check_run_id, anomaly_id, field_id, field_name, anomalous_value, " +
				"id_field_names, id_field_values, subject_id, site_id " +
				"from uni_anomaly_dto " +
				"where check_run_id = " + checkRunId + " " +
				"and subject_id = " + subjectId;
		return queryAndMarkAsVeiwed(sql);
	}
	
	private List<UniAnomalyDto> queryAndMarkAsVeiwed(String sql) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		List<UniAnomalyDto> dtos = jdbcTemplate.query(sql, new UniAnomalyDtoResultSetMapper());
		int p = 0;
		while (p < dtos.size()) {
			int q = p + BATCH_SIZE;
			if (q > dtos.size()) {
				q = dtos.size();
			}
			StringBuilder builder = new StringBuilder("update anomaly set has_been_viewed = 1 where anomaly_id in (");
			for (int i = p; i < q; i++) {
				if (i > p) {
					builder.append(", ");
				}
				builder.append(dtos.get(i).getAnomalyId());
			}
			builder.append(")");
			jdbcTemplate.update(builder.toString());
			p = q;
		}
		return dtos;
	}

	private static final class UniAnomalyDtoResultSetMapper implements RowMapper<UniAnomalyDto> {

		@Override
		public UniAnomalyDto mapRow(ResultSet rs, int num) throws SQLException {
			UniAnomalyDto dto = new UniAnomalyDto(rs.getLong(1), rs.getLong(2), rs.getLong(3), rs.getString(4), rs.getString(5),
					rs.getLong(8), rs.getLong(9));
			String[] idFieldNames = rs.getString(6).split(",");
			String[] idFieldValues = rs.getString(7).split(",");
			for (int i = 0; i < idFieldNames.length; i++) {
				dto.addIdFieldNameAndValue(idFieldNames[i], idFieldValues[i]);
			}
			return dto;
		}
		
	}

}
