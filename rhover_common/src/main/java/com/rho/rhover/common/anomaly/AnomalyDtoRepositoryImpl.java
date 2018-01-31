package com.rho.rhover.common.anomaly;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class AnomalyDtoRepositoryImpl {
	
	private static final int BATCH_SIZE = 100;
	
	@Autowired
	protected DataSource dataSource;

	protected List<? extends AnomalyDto> queryAndMarkAsVeiwed(String sql, RowMapper<? extends AnomalyDto> rowMapper) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		List<? extends AnomalyDto> dtos = jdbcTemplate.query(sql, rowMapper);
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
}
