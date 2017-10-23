package com.rho.rhover;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebMvcConfiguration extends WebMvcConfigurerAdapter {
	
	@Autowired
	private DataSource dataSource;
	
	@Bean
	public JdbcTemplate jdbcTemmplate() {
		return new JdbcTemplate(dataSource);
	}
}
