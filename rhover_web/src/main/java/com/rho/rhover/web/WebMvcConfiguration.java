package com.rho.rhover.web;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@ComponentScan("com.rho.rhover")
@EnableJpaRepositories(basePackages = {"com.rho.rhover.common", "com.rho.rhover.web"})
@EntityScan({"com.rho.rhover.common", "com.rho.rhover.web"})
public class WebMvcConfiguration extends WebMvcConfigurerAdapter {
	
	@Autowired
	private DataSource dataSource;
	
	@Bean
	public JdbcTemplate jdbcTemmplate() {
		return new JdbcTemplate(dataSource);
	}
	
	@Override
    public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/").setViewName("home");
		registry.addViewController("/test_ui").setViewName("test_ui");
        registry.addViewController("/login").setViewName("login");
    }
}
