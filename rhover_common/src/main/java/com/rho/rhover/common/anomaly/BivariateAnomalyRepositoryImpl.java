package com.rho.rhover.common.anomaly;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class BivariateAnomalyRepositoryImpl extends AnomalyDtoRepositoryImpl implements BivariateAnomalyDtoRepository {

	@SuppressWarnings("unchecked")
	@Override
	public List<UniAnomalyDto> findByCheckRunId(Long checkRunId) {
		String sql =
				"select check_run_id, anomaly_id, field_instance_1_id, field_instance_2_id, field_name_1, "
				+ "field_name_2, anomalous_value_1, anomalous_value_2, subject_id, subject_name, "
				+ "site_id, site_name, phase_id, phase_name, record_id "
				+ "from bivariate_anomaly " 
				+ "where check_run_id = " + checkRunId;
		return (List<UniAnomalyDto>)queryAndMarkAsVeiwed(sql, new BivariateAnomalyDtoResultSetMapper());
	}

	@Override
	public List<UniAnomalyDto> findByCheckRunIdAndSiteId(Long checkRunId, Long siteId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UniAnomalyDto> findByCheckRunIdAndSubjectId(Long checkRunId, Long subjectId) {
		// TODO Auto-generated method stub
		return null;
	}

	private static final class BivariateAnomalyDtoResultSetMapper implements RowMapper<BivariateAnomalyDto> {

		@Override
		public BivariateAnomalyDto mapRow(ResultSet rs, int p) throws SQLException {
			BivariateAnomalyDto dto = new BivariateAnomalyDto();
			dto.setCheckRunId(rs.getLong(1));
			dto.setAnomalyId(rs.getLong(2));
			dto.setFieldInstance1Id(rs.getLong(3));
			dto.setFieldInstance2Id(rs.getLong(4));
			dto.setFieldName1(rs.getString(5));
			dto.setFieldName2(rs.getString(6));
			dto.setAnomalousValue1(rs.getString(7));
			dto.setAnomalousValue2(rs.getString(8));
			dto.setSubjectId(rs.getLong(9));
			dto.setSubjectName(rs.getString(10));
			dto.setSiteId(rs.getLong(11));
			dto.setSiteName(rs.getString(12));
			dto.setPhaseId(rs.getLong(13));
			dto.setPhaseName(rs.getString(14));
			dto.setRecordId(rs.getString(15));
			return dto;
		}
		
	}
}
