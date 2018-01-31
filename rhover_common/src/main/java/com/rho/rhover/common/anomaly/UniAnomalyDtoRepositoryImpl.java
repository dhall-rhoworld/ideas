package com.rho.rhover.common.anomaly;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class UniAnomalyDtoRepositoryImpl extends AnomalyDtoRepositoryImpl implements UniAnomalyDtoRepository {

	@SuppressWarnings("unchecked")
	@Override
	public List<UniAnomalyDto> findByCheckRunId(Long checkRunId) {
		String sql = 
				"select check_run_id, anomaly_id, field_id, field_name, anomalous_value, " +
				"phase_id, phase_name, subject_id, subject_name, site_id, site_name, record_id " +
				"from uni_anomaly_dto " +
				"where check_run_id = " + checkRunId;
		return (List<UniAnomalyDto>)queryAndMarkAsVeiwed(sql, new UniAnomalyDtoResultSetMapper());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<UniAnomalyDto> findByCheckRunIdAndSiteId(Long checkRunId, Long siteId) {
		String sql = 
				"select check_run_id, anomaly_id, field_id, field_name, anomalous_value, " +
				"phase_id, phase_name, subject_id, subject_name, site_id, site_name, record_id " +
				"from uni_anomaly_dto " +
				"where check_run_id = " + checkRunId + " " +
				"and site_id = " + siteId;
		return (List<UniAnomalyDto>)queryAndMarkAsVeiwed(sql, new UniAnomalyDtoResultSetMapper());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<UniAnomalyDto> findByCheckRunIdAndSubjectId(Long checkRunId, Long subjectId) {
		String sql = 
				"select check_run_id, anomaly_id, field_id, field_name, anomalous_value, " +
				"phase_id, phase_name, subject_id, subject_name, site_id, site_name, record_id " +
				"from uni_anomaly_dto " +
				"where check_run_id = " + checkRunId + " " +
				"and subject_id = " + subjectId;
		return (List<UniAnomalyDto>)queryAndMarkAsVeiwed(sql, new UniAnomalyDtoResultSetMapper());
	}

	private static final class UniAnomalyDtoResultSetMapper implements RowMapper<UniAnomalyDto> {

		@Override
		public UniAnomalyDto mapRow(ResultSet rs, int num) throws SQLException {
			UniAnomalyDto dto = new UniAnomalyDto(
					rs.getLong(1),
					rs.getLong(2),
					rs.getLong(3),
					rs.getString(4),
					rs.getString(5),
					rs.getLong(6),
					rs.getString(7),
					rs.getLong(8),
					rs.getString(9),
					rs.getLong(10),
					rs.getString(11),
					rs.getString(12));
			return dto;
		}
		
	}

}
