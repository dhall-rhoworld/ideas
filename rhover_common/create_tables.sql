
create table study (
	study_id BIGINT AUTO_INCREMENT NOT NULL,
	study_name VARCHAR(50) NOT NULL,
	form_field_name VARCHAR(50) NOT NULL,
	site_field_name VARCHAR(50) NOT NULL,
	subject_field_name VARCHAR(50) NOT NULL,
	query_file_path VARCHAR(400),
	is_initialized TINYINT NOT NULL DEFAULT 0,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	modified_by VARCHAR(50),
	CONSTRAINT pk_study PRIMARY KEY (study_id),
	CONSTRAINT u_study_name UNIQUE (study_name)
);

create table data_location (
	data_location_id BIGINT AUTO_INCREMENT NOT NULL,
	folder_path VARCHAR(400) NOT NULL,
	study_id BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	modified_by VARCHAR(50),
	CONSTRAINT pk_data_location PRIMARY KEY (data_location_id),
	CONSTRAINT fk_data_location_2_study FOREIGN KEY (study_id) REFERENCES study(study_id),
	CONSTRAINT u_data_location_folder_path_study_id UNIQUE (folder_path, study_id)
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
	CONSTRAINT fk_dataset_2_data_location FOREIGN KEY (data_location_id) REFERENCES data_location(data_location_id),
	CONSTRAINT u_dataset_name_study_id UNIQUE (dataset_name, study_id)
);

create table dataset_version (
	dataset_version_id BIGINT AUTO_INCREMENT NOT NULL,
	dataset_version_name VARCHAR(50) NOT NULL,
	is_current TINYINT NOT NULL,
	num_records INT NOT NULL,
	dataset_id BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_dataset_version PRIMARY KEY (dataset_version_id),
	CONSTRAINT fk_dataset_version_2_dataset FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id),
	CONSTRAINT u_dataset_version_name_dataset_id UNIQUE (dataset_version_name, dataset_id)
);

create table data_stream (
	data_stream_id BIGINT AUTO_INCREMENT NOT NULL,
	data_stream_name VARCHAR(50) NOT NULL,
	study_id BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_data_stream PRIMARY KEY (data_stream_id),
	CONSTRAINT fk_data_stream_2_study FOREIGN KEY (study_id) REFERENCES study(study_id),
	CONSTRAINT u_data_stream_name_study_id UNIQUE (data_stream_name, study_id)
);

create table dataset_version_stream (
	dataset_version_id BIGINT NOT NULL,
	data_stream_id BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_data_version__stream PRIMARY KEY (dataset_version_id, data_stream_id),
	CONSTRAINT fk_dataset_version_stream_2_dataset_version
		FOREIGN KEY (dataset_version_id) REFERENCES dataset_version(dataset_version_id),
	CONSTRAINT fk_dataset_version_stream_2_data_stream
		FOREIGN KEY (data_stream_id) REFERENCES data_stream(data_stream_id)
);

create table site (
	site_id BIGINT AUTO_INCREMENT NOT NULL,
	site_name VARCHAR(200) NOT NULL,
	study_id BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_site PRIMARY KEY (site_id),
	CONSTRAINT fk_site_2_study FOREIGN KEY (study_id) REFERENCES study(study_id),
	CONSTRAINT u_site_name_study_id UNIQUE (site_name, study_id)
);

create table subject (
	subject_id BIGINT AUTO_INCREMENT NOT NULL,
	subject_name VARCHAR(50) NOT NULL,
	site_id BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_subject PRIMARY KEY (subject_id),
	CONSTRAINT fk_subject_2_site FOREIGN KEY (site_id) REFERENCES site(site_id),
	CONSTRAINT u_subject_name_site_id UNIQUE (subject_name, site_id)
);

create table field (
	field_id BIGINT AUTO_INCREMENT NOT NULL,
	field_name VARCHAR(200) NOT NULL,
	field_label VARCHAR(400) NOT NULL,
	is_identifying TINYINT NOT NULL DEFAULT 0,
	study_id BIGINT NOT NULL,
	data_type VARCHAR(50) NOT NULL,
	is_skipped TINYINT NOT NULL DEFAULT 0,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_field PRIMARY KEY (field_id),
	CONSTRAINT fk_field_2_study FOREIGN KEY (study_id) REFERENCES study (study_id),
	CONSTRAINT u_field_name_study_id UNIQUE (field_name, study_id)
);

create table csv_data (
	csv_data_id BIGINT AUTO_INCREMENT NOT NULL,
	field_id BIGINT NOT NULL,
	data LONGTEXT NOT NULL,
	CONSTRAINT pk_csv_data PRIMARY KEY (csv_data_id),
	CONSTRAINT fk_csv_data_2_field FOREIGN KEY (field_id) REFERENCES field(field_id) 
);

create table dataset_version_field (
	dataset_version_id BIGINT NOT NULL,
	field_id BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_dataset_version_field PRIMARY KEY (dataset_version_id, field_id),
	CONSTRAINT data_set_version_field_2_dataset_version
		FOREIGN KEY (dataset_version_id) REFERENCES dataset_version (dataset_version_id),
	CONSTRAINT data_set_version_field_2_field
		FOREIGN KEY (field_id) REFERENCES field (field_id)
);

create table study_db_version (
	study_db_version_id BIGINT AUTO_INCREMENT NOT NULL,
	study_db_version_name VARCHAR(50) NOT NULL,
	study_id BIGINT NOT NULL,
	is_current TINYINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_study_db_version PRIMARY KEY (study_db_version_id),
	CONSTRAINT fk_study_db_version_2_study FOREIGN KEY (study_id) REFERENCES study(study_id),
	CONSTRAINT u_study_db_version_name_study_id UNIQUE (study_db_version_name, study_id)
);

create table study_db_version_config (
	study_db_version_id BIGINT NOT NULL,
	dataset_version_id BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_study_db_version_config PRIMARY KEY(study_db_version_id, dataset_version_id),
	CONSTRAINT fk_study_db_version_config_2_study_db_version FOREIGN KEY (study_db_version_id) REFERENCES study_db_version(study_db_version_id),
	CONSTRAINT fk_study_db_version_config_2_dataset_version FOREIGN KEY (dataset_version_id) REFERENCES dataset_version(dataset_version_id)
);

create table loader_issue (
	loader_issue_id BIGINT AUTO_INCREMENT NOT NULL,
	message VARCHAR(500),
	stack_trace TEXT,
	issue_level VARCHAR(50) NOT NULL,
	dataset_version_id BIGINT,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_loader_issue PRIMARY KEY (loader_issue_id),
	CONSTRAINT fk_issue_2_dataset_version FOREIGN KEY (dataset_version_id)
		REFERENCES dataset_version(dataset_version_id)
);

create table checks (
	check_id BIGINT AUTO_INCREMENT NOT NULL,
	check_name VARCHAR(50) NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_checks PRIMARY KEY (check_id),
	CONSTRAINT u_check_name UNIQUE (check_name)
);

insert into checks(check_name)
values('UNIVARIATE_OUTLIER');

create table check_param (
	check_param_id BIGINT AUTO_INCREMENT NOT NULL,
	param_name VARCHAR(50) NOT NULL,
	param_value VARCHAR(50) NOT NULL,
	param_scope VARCHAR(50) NOT NULL,
	study_id BIGINT,
	dataset_id BIGINT,
	field_id BIGINT,
	check_id BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	modified_by VARCHAR(50),
	CONSTRAINT pk_check_param PRIMARY KEY (check_param_id),
	CONSTRAINT fk_check_param_2_study FOREIGN KEY (study_id) REFERENCES study(study_id),
	CONSTRAINT fk_check_param_2_dataset FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id),
	CONSTRAINT fk_check_param_2_field FOREIGN KEY (field_id) REFERENCES field(field_id),
	CONSTRAINT fk_check_param_2_checks FOREIGN KEY (check_id) REFERENCES checks(check_id)
);

insert into check_param (param_name, param_value, param_scope, check_id)
values('data_types', 'continuous', 'GLOBAL', (select check_id from checks where check_name = 'UNIVARIATE_OUTLIER'));

insert into check_param (param_name, param_value, param_scope, check_id)
values('filter_non_key', 'off', 'GLOBAL', (select check_id from checks where check_name = 'UNIVARIATE_OUTLIER'));

insert into check_param (param_name, param_value, param_scope, check_id)
values('filter_identifying', 'on', 'GLOBAL', (select check_id from checks where check_name = 'UNIVARIATE_OUTLIER'));

insert into check_param (param_name, param_value, param_scope, check_id)
values('sd', '2', 'GLOBAL', (select check_id from checks where check_name = 'UNIVARIATE_OUTLIER'));
