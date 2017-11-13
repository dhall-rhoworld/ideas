/*
study_check_run
dataset_check_run
data_field_check_run
*/

create table study (
	study_id BIGINT AUTO_INCREMENT NOT NULL,
	study_name VARCHAR(50) NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	modified_by VARCHAR(50),
	CONSTRAINT pk_study PRIMARY KEY (study_id)
);

create table data_location (
	data_location_id BIGINT AUTO_INCREMENT NOT NULL,
	path VARCHAR(400) NOT NULL,
	study_id BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	modified_by VARCHAR(50),
	CONSTRAINT pk_data_location PRIMARY KEY (data_location_id),
	CONSTRAINT fk_data_location_2_study FOREIGN KEY (study_id) REFERENCES study(study_id)
);

create table site (
	site_id BIGINT AUTO_INCREMENT NOT NULL,
	site_name VARCHAR(200) NOT NULL,
	study_id BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_site PRIMARY KEY (site_id),
	CONSTRAINT fk_site_2_study FOREIGN KEY (study_id) REFERENCES study(study_id)
);

create table subject (
	subject_id BIGINT AUTO_INCREMENT NOT NULL,
	subject_name VARCHAR(50) NOT NULL,
	site_id BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_subject PRIMARY KEY (subject_id),
	CONSTRAINT fk_subject_2_site FOREIGN KEY (site_id) REFERENCES site(site_id)
);

create table event (
	event_id BIGINT AUTO_INCREMENT NOT NULL,
	event_name VARCHAR(50) NOT NULL,
	study_id BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_event PRIMARY KEY (event_id),
	CONSTRAINT fk_event_2_study FOREIGN KEY (study_id) REFERENCES study (study_id)
);

create table dataset (
	dataset_id BIGINT AUTO_INCREMENT NOT NULL,
	dataset_name VARCHAR(50) NOT NULL,
	file_path VARCHAR(400) NOT NULL,
	is_checked TINYINT NOT NULL DEFAULT 0,
	was_checkability_deduced TINYINT NOT NULL DEFAULT 0,
	was_checkability_confirmed TINYINT NOT NULL DEFAULT 0,
	study_id BIGINT NOT NULL,
	data_location_id BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	modified_by VARCHAR(50),
	CONSTRAINT pk_dataset PRIMARY KEY (dataset_id),
	CONSTRAINT fk_dataset_2_study FOREIGN KEY(study_id) REFERENCES study(study_id),
	CONSTRAINT fk_dataset_2_data_location FOREIGN KEY (data_location_id) REFERENCES data_location(data_location_id)
);

create table study_db_version (
	study_db_version_id BIGINT AUTO_INCREMENT NOT NULL,
	study_db_version_name VARCHAR(50) NOT NULL,
	study_id BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_study_db_version PRIMARY KEY (study_db_version_id),
	CONSTRAINT fk_study_db_version_2_study FOREIGN KEY (study_id) REFERENCES study(study_id)
);

create table dataset_version (
	dataset_version_id BIGINT AUTO_INCREMENT NOT NULL,
	dataset_version_name VARCHAR(50) NOT NULL,
	dataset_id BIGINT NOT NULL,
	study_db_version_id BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT dataset_version PRIMARY KEY (dataset_version_id),
	CONSTRAINT fk_dataset_version_2_dataset FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id),
	CONSTRAINT fk_dataset_version_2_study_db_version FOREIGN KEY (study_db_version_id) REFERENCES study_db_version(study_db_version_id)
);


create table field (
	field_id BIGINT AUTO_INCREMENT NOT NULL,
	field_name VARCHAR(200) NOT NULL,
	dataset_id BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_field PRIMARY KEY (field_id),
	CONSTRAINT fk_field_2_dataset FOREIGN KEY (dataset_id) REFERENCES dataset (dataset_id)
);

create table checks (
	check_id BIGINT AUTO_INCREMENT NOT NULL,
	check_code CHAR(2) NOT NULL,
	check_name VARCHAR(50) NOT NULL,
	description VARCHAR(200) NOT NULL,
	num_variables INT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	modified_by VARCHAR(50),
	CONSTRAINT pk_checks PRIMARY KEY (check_id)
);

create table check_param (
	check_param_id BIGINT AUTO_INCREMENT NOT NULL,
	param_name VARCHAR(50) NOT NULL,
	description VARCHAR(200) NOT NULL,
	check_id BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	modified_by VARCHAR(50),
	CONSTRAINT pk_check_param PRIMARY KEY (check_param_id),
	CONSTRAINT fk_check_param_2_checks
		FOREIGN KEY (check_id) REFERENCES checks(check_id)
);

create table scope (
	scope_code CHAR NOT NULL,
	scope_name VARCHAR(50) NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_scope PRIMARY KEY (scope_code)
);

create table field_set (
	field_set_id BIGINT AUTO_INCREMENT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_field_set PRIMARY KEY (field_set_id)
);

create table field_set_field (
	field_set_id BIGINT NOT NULL,
	field_id BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	modified_by VARCHAR(50),
	CONSTRAINT pk_field_set_field PRIMARY KEY (field_set_id, field_id),
	CONSTRAINT fk_field_set_field_2_field_set FOREIGN KEY (field_set_id) REFERENCES field_set(field_set_id),
	CONSTRAINT fk_field_set_field_2_field FOREIGN KEY (field_id) REFERENCES field(field_id)
);

create table field_set_check (
	field_set_check_id BIGINT AUTO_INCREMENT NOT NULL,
	check_id BIGINT NOT NULL,
	field_set_id BIGINT NOT NULL,
	current_data TEXT,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	modified_by VARCHAR(50),
	CONSTRAINT pk_field_set_checks PRIMARY KEY (field_set_check_id),
	CONSTRAINT fk_field_set_check_2_check FOREIGN KEY (check_id) REFERENCES checks(check_id),
	CONSTRAINT fk_field_set_check_2_field_set FOREIGN KEY (field_set_id) REFERENCES field_set(field_set_id)
);

create table param_value (
	param_value_id BIGINT AUTO_INCREMENT NOT NULL,
	scope_code CHAR NOT NULL,
	value VARCHAR(50) NOT NULL,
	study_id BIGINT,
	dataset_id BIGINT,
	field_set_id BIGINT,
	check_param_id BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	modified_by VARCHAR(50),
	CONSTRAINT pk_param_value PRIMARY KEY (param_value_id),
	CONSTRAINT fk_param_value_2_study FOREIGN KEY (study_id) REFERENCES study(study_id),
	CONSTRAINT fk_param_value_2_dataset FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id),
	CONSTRAINT fk_param_value_2_field_set FOREIGN KEY (field_set_id) REFERENCES field_set(field_set_id),
	CONSTRAINT fk_param_value_2_check_param FOREIGN KEY (check_param_id) REFERENCES check_param(check_param_id)
);

create table checker_run (
	checker_run_id BIGINT AUTO_INCREMENT NOT NULL,
	study_db_version_id BIGINT NOT NULL,
	start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	end_time TIMESTAMP,
	CONSTRAINT pk_checker_run PRIMARY KEY (checker_run_id),
	CONSTRAINT fk_checker_run_2_study_db_version FOREIGN KEY (study_db_version_id) REFERENCES study_db_version(study_db_version_id)
);

create table checker_message (
	checker_message_id BIGINT AUTO_INCREMENT NOT NULL,
	message_type CHAR(1) NOT NULL,
	message VARCHAR(500) NOT NULL,
	checker_run_id BIGINT NOT NULL,
	message_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_checker_message PRIMARY KEY (checker_message_id),
	CONSTRAINT fk_checker_message_2_checker_run
		FOREIGN KEY (checker_run_id) REFERENCES checker_run(checker_run_id)
);

create table data_property (
	data_property_id BIGINT AUTO_INCREMENT NOT NULL,
	data_property_name VARCHAR(50) NOT NULL,
	data_property_value VARCHAR(50) NOT NULL,
	field_set_check_id BIGINT NOT NULL,
	study_db_version_id BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_data_property PRIMARY KEY (data_property_id),
	CONSTRAINT fk_data_property_2_field_set_check FOREIGN KEY (field_set_check_id)
		REFERENCES field_set_check(field_set_check_id),
	CONSTRAINT fk_data_property_2_study_db_version FOREIGN KEY (study_db_version_id)
		REFERENCES study_db_version(study_db_version_id)
);

create table parameter_value_set (
	parameter_value_set_id BIGINT AUTO_INCREMENT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	field_set_check_id BIGINT NOT NULL,
	CONSTRAINT pk_parameter_value_set PRIMARY KEY (parameter_value_set_id),
	CONSTRAINT fk_parameter_value_set_2_field_set_check FOREIGN KEY (field_set_check_id)
		REFERENCES field_set_check(field_set_check_id)
);

create table param_value_used (
	param_value_used_id BIGINT AUTO_INCREMENT NOT NULL,
	param_id BIGINT NOT NULL,
	param_value VARCHAR(50) NOT NULL,
	
)
