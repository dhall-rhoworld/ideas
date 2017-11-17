/* MySQL root account: root@localhost/password */

create database rhover;
create user 'rhover'@'localhost' identified by 'rhover';
grant all privileges on rhover.* to 'rhover'@'localhost';

create table study (
	study_id BIGINT AUTO_INCREMENT NOT NULL,
	study_name VARCHAR(50) NOT NULL,
	form_field_name VARCHAR(50) NOT NULL,
	site_field_name VARCHAR(50) NOT NULL,
	subject_field_name VARCHAR(50) NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	modified_by VARCHAR(50),
	CONSTRAINT pk_study PRIMARY KEY (study_id)
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
	dataset_id BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT dataset_version PRIMARY KEY (dataset_version_id),
	CONSTRAINT fk_dataset_version_2_dataset FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id),
	CONSTRAINT u_dataset_version_name_dataset_id UNIQUE (dataset_version_name, dataset_id)
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

create table data_type (
	data_type_id BIGINT AUTO_INCREMENT NOT NULL,
	data_type_name VARCHAR(50) NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_data_type PRIMARY KEY (data_type_id)
);

create table field (
	field_id BIGINT AUTO_INCREMENT NOT NULL,
	field_name VARCHAR(200) NOT NULL,
	study_id BIGINT NOT NULL,
	data_type_id BIGINT NOT NULL,
	initial_dataset_version_id BIGINT NOT NULL,
	last_dataset_version_id BIGINT,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_field PRIMARY KEY (field_id),
	CONSTRAINT fk_field_2_dataset FOREIGN KEY (dataset_id) REFERENCES dataset (dataset_id),
	CONSTRAINT fk_field_2_dataset_version_1 FOREIGN KEY (initial_dataset_version_id)
		REFERENCES dataset_version(dataset_version_id),
	CONSTRAINT fk_field_2_dataset_version_2 FOREIGN KEY (last_dataset_version_id)
		REFERENCES dataset_version(dataset_version_id),
	CONSTRAINT fk_field_2_data_type FOREIGN KEY (data_type_id)
		REFERENCES data_type(data_type_id),
	CONSTRAINT u_field_name_dataset_id UNIQUE (field_name, dataset_id)
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


----------------------


/* TODO: Remove recruit_id */
create table anomaly (
	anomaly_id BIGINT AUTO_INCREMENT NOT NULL,
	anomaly_type CHAR NOT NULL,
	data_field_id BIGINT NOT NULL,
	field_value VARCHAR(100) NOT NULL,
	version_first_seen_in BIGINT NOT NULL,
	version_last_seen_in BIGINT NOT NULL,
	recruit_id VARCHAR(100) NOT NULL,
	subject_id BIGINT NOT NULL,
	event VARCHAR(100) NOT NULL,
	site_id BIGINT NOT NULL,
	has_been_viewed TINYINT DEFAULT 0 NOT NULL,
	is_an_issue TINYINT DEFAULT 1 NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT PK_ANOMALY PRIMARY KEY (anomaly_id),
	CONSTRAINT FK_ANOMALY_2_DATA_FIELD FOREIGN KEY (data_field_id) REFERENCES data_field(data_field_id),
	CONSTRAINT FK_ANOMALY_2_DATASET_VERSION_1 FOREIGN KEY (version_first_seen_in) REFERENCES dataset_version(dataset_version_id),
	CONSTRAINT FK_ANOMALY_2_DATASET_VERSION_2 FOREIGN KEY (version_last_seen_in) REFERENCES dataset_version(dataset_version_id),
	CONSTRAINT FK_ANOAMLY_2_SITE FOREIGN KEY (site_id) REFERENCES site(site_id),
	CONSTRAINT FK_ANOMALY_2_SUBJECT FOREIGN KEY (subject_id) REFERENCES subject(subject_id)
);

create table bivariate_check (
	bivariate_check_id BIGINT AUTO_INCREMENT PRIMARY KEY,
	dataset_id_1 BIGINT,
	dataset_id_2 BIGINT,
	data_field_1 VARCHAR(200),
	data_field_2 VARCHAR(200),
	file_path VARCHAR(250),
	is_het TINYINT DEFAULT 0,
	lambda DOUBLE,
	intercept DOUBLE,
	slope DOUBLE,
	residual_threshold DOUBLE,
	density_threshold DOUBLE,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	foreign key(dataset_id_1) references dataset (dataset_id),
	foreign key(dataset_id_2) references dataset (dataset_id)
);

create table bivariate_outlier (
	bivariate_outlier_id BIGINT AUTO_INCREMENT PRIMARY KEY,
	field_value_1 DOUBLE,
	field_value_2 DOUBLE,
	bivariate_check_id BIGINT,
	version_first_seen_in BIGINT,
	version_last_seen_in BIGINT,
	recruit_id VARCHAR(100),
	event VARCHAR(100),
	has_been_viewed TINYINT DEFAULT 0,
	is_an_issue TINYINT DEFAULT 1,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	foreign key(bivariate_check_id) references bivariate_check(bivariate_check_id),
	foreign key(version_first_seen_in) references dataset_version(dataset_version_id),
	foreign key(version_last_seen_in) references dataset_version(dataset_version_id)
);
