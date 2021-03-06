CREATE TABLE logging_event 
  (
    timestmp         BIGINT NOT NULL,
    formatted_message  TEXT NOT NULL,
    logger_name       VARCHAR(254) NOT NULL,
    level_string      VARCHAR(254) NOT NULL,
    thread_name       VARCHAR(254),
    reference_flag    SMALLINT,
    arg0              VARCHAR(254),
    arg1              VARCHAR(254),
    arg2              VARCHAR(254),
    arg3              VARCHAR(254),
    caller_filename   VARCHAR(254) NOT NULL,
    caller_class      VARCHAR(254) NOT NULL,
    caller_method     VARCHAR(254) NOT NULL,
    caller_line       CHAR(4) NOT NULL,
    event_id          BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY
  );

CREATE TABLE logging_event_property
  (
    event_id	      BIGINT NOT NULL,
    mapped_key        VARCHAR(254) NOT NULL,
    mapped_value      TEXT,
    PRIMARY KEY(event_id, mapped_key),
    FOREIGN KEY (event_id) REFERENCES logging_event(event_id)
  );

CREATE TABLE logging_event_exception
  (
    event_id         BIGINT NOT NULL,
    i                SMALLINT NOT NULL,
    trace_line       VARCHAR(254) NOT NULL,
    PRIMARY KEY(event_id, i),
    FOREIGN KEY (event_id) REFERENCES logging_event(event_id)
  );
  
CREATE TABLE user_session (
	user_session_id BIGINT AUTO_INCREMENT NOT NULL,
	user_name VARCHAR(50) NOT NULL,
	session_started TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	web_session_id VARCHAR(100),
	CONSTRAINT pk_user_session PRIMARY KEY (user_session_id)
);

insert into user_session(user_name, web_session_id) values ('system', '0');
  
CREATE TABLE study (
	study_id BIGINT AUTO_INCREMENT NOT NULL,
	study_name VARCHAR(50) NOT NULL,
	form_field_name VARCHAR(50) NOT NULL,
	site_field_name VARCHAR(50) NOT NULL,
	subject_field_name VARCHAR(50) NOT NULL,
	phase_field_name VARCHAR(50) NOT NULL,
	record_id_field_name VARCHAR(50) NOT NULL,
	form_field_id BIGINT,
	site_field_id BIGINT,
	subject_field_id BIGINT,
	phase_field_id BIGINT,
	record_id_field_id BIGINT,
	query_file_path VARCHAR(400),
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	modified_by VARCHAR(50),
	user_session_id BIGINT NOT NULL,
	CONSTRAINT pk_study PRIMARY KEY (study_id),
	CONSTRAINT u_study_name UNIQUE (study_name),
	CONSTRAINT fk_study_2_user_session FOREIGN KEY (user_session_id) REFERENCES user_session(user_session_id)
);

CREATE TABLE data_location (
	data_location_id BIGINT AUTO_INCREMENT NOT NULL,
	folder_path VARCHAR(400) NOT NULL,
	include_sas TINYINT NOT NULL DEFAULT 1,
	include_csv TINYINT NOT NULL DEFAULT 1,
	study_id BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	user_session_id BIGINT NOT NULL,
	CONSTRAINT pk_data_location PRIMARY KEY (data_location_id),
	CONSTRAINT fk_data_location_2_study FOREIGN KEY (study_id) REFERENCES study(study_id),
	CONSTRAINT u_data_location_folder_path_study_id UNIQUE (folder_path, study_id),
	CONSTRAINT fk_data_location_2_user_session FOREIGN KEY (user_session_id) REFERENCES user_session(user_session_id)
);

CREATE TABLE dataset (
	dataset_id BIGINT NOT NULL,
	dataset_name VARCHAR(50) NOT NULL,
	file_path VARCHAR(400) NOT NULL,
	is_checked TINYINT NOT NULL DEFAULT 0,
	is_critical TINYINT NOT NULL DEFAULT 0,
	was_checkability_deduced TINYINT NOT NULL DEFAULT 0,
	was_checkability_confirmed TINYINT NOT NULL DEFAULT 0,
	study_id BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	modified_by VARCHAR(50),
	CONSTRAINT pk_dataset PRIMARY KEY (dataset_id),
	CONSTRAINT fk_dataset_2_study FOREIGN KEY(study_id) REFERENCES study(study_id),
	CONSTRAINT u_dataset_name_study_id UNIQUE (dataset_name, study_id)
);

CREATE TABLE dataset_version (
	dataset_version_id BIGINT NOT NULL,
	dataset_version_name VARCHAR(50) NOT NULL,
	is_current TINYINT NOT NULL,
	num_records INT NOT NULL,
	missing_an_id_field TINYINT NOT NULL DEFAULT 0,
	multiple_recs_per_encounter TINYINT NOT NULL DEFAULT 0,
	dataset_id BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_dataset_version PRIMARY KEY (dataset_version_id),
	CONSTRAINT fk_dataset_version_2_dataset FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id),
	CONSTRAINT u_dataset_version_name_dataset_id UNIQUE (dataset_version_name, dataset_id)
);

CREATE TABLE data_stream (
	data_stream_id BIGINT NOT NULL,
	data_stream_name VARCHAR(50) NOT NULL,
	study_id BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_data_stream PRIMARY KEY (data_stream_id),
	CONSTRAINT fk_data_stream_2_study FOREIGN KEY (study_id) REFERENCES study(study_id),
	CONSTRAINT u_data_stream_name_study_id UNIQUE (data_stream_name, study_id)
);

CREATE TABLE dataset_version_stream (
	dataset_version_id BIGINT NOT NULL,
	data_stream_id BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_data_version__stream PRIMARY KEY (dataset_version_id, data_stream_id),
	CONSTRAINT fk_dataset_version_stream_2_dataset_version
		FOREIGN KEY (dataset_version_id) REFERENCES dataset_version(dataset_version_id),
	CONSTRAINT fk_dataset_version_stream_2_data_stream
		FOREIGN KEY (data_stream_id) REFERENCES data_stream(data_stream_id)
);

CREATE TABLE site (
	site_id BIGINT NOT NULL,
	site_name VARCHAR(200) NOT NULL,
	study_id BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_site PRIMARY KEY (site_id),
	CONSTRAINT fk_site_2_study FOREIGN KEY (study_id) REFERENCES study(study_id),
	CONSTRAINT u_site_name_study_id UNIQUE (site_name, study_id)
);

CREATE TABLE subject (
	subject_id BIGINT NOT NULL,
	subject_name VARCHAR(50) NOT NULL,
	site_id BIGINT,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_subject PRIMARY KEY (subject_id),
	CONSTRAINT fk_subject_2_site FOREIGN KEY (site_id) REFERENCES site(site_id),
	CONSTRAINT u_subject_name_site_id UNIQUE (subject_name, site_id)
);

CREATE TABLE phase (
	phase_id BIGINT NOT NULL,
	phase_name VARCHAR(200) NOT NULL,
	study_id BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_phase PRIMARY KEY (phase_id),
	CONSTRAINT fk_phase_study FOREIGN KEY (study_id) REFERENCES study(study_id)
);

CREATE TABLE dataset_version_phase (
	dataset_version_id BIGINT NOT NULL,
	phase_id BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_dataset_version_phase PRIMARY KEY (dataset_version_id, phase_id),
	CONSTRAINT fk_dataset_version_phase_2_dataset_version FOREIGN KEY (dataset_version_id)
		REFERENCES dataset_version(dataset_version_id),
	CONSTRAINT fk_dataset_version_phase_2_phase FOREIGN KEY (phase_id)
		REFERENCES phase(phase_id)
);

CREATE TABLE field (
	field_id BIGINT NOT NULL,
	field_name VARCHAR(200) NOT NULL,
	field_label VARCHAR(400) NOT NULL,
	study_id BIGINT NOT NULL,
	data_type VARCHAR(50) NOT NULL,
	is_skipped TINYINT NOT NULL DEFAULT 0,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_field PRIMARY KEY (field_id),
	CONSTRAINT fk_field_2_study FOREIGN KEY (study_id) REFERENCES study (study_id),
	CONSTRAINT u_field_name_study_id UNIQUE (field_name, study_id)
);

alter table study
add CONSTRAINT fk_study_2_field_form
FOREIGN KEY (form_field_id) REFERENCES field(field_id);

alter table study
add CONSTRAINT fk_study_2_field_site
FOREIGN KEY (site_field_id) REFERENCES field(field_id);

alter table study
add CONSTRAINT fk_study_2_field_subject
FOREIGN KEY (subject_field_id) REFERENCES field(field_id);

alter table study
add CONSTRAINT fk_study_2_field_phase
FOREIGN KEY (phase_field_id) REFERENCES field(field_id);

alter table study
add CONSTRAINT fk_study_2_field_record
FOREIGN KEY (record_id_field_id) REFERENCES field(field_id);

CREATE TABLE csv_data (
	csv_data_id BIGINT AUTO_INCREMENT NOT NULL,
	field_id BIGINT NOT NULL,
	dataset_id BIGINT NOT NULL,
	data LONGTEXT NOT NULL,
	CONSTRAINT pk_csv_data PRIMARY KEY (csv_data_id),
	CONSTRAINT fk_csv_data_2_field FOREIGN KEY (field_id) REFERENCES field(field_id),
	CONSTRAINT fk_csv_data_2_dataset FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id)
);

CREATE TABLE dataset_version_field (
	dataset_version_id BIGINT NOT NULL,
	field_id BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_dataset_version_field PRIMARY KEY (dataset_version_id, field_id),
	CONSTRAINT data_set_version_field_2_dataset_version
		FOREIGN KEY (dataset_version_id) REFERENCES dataset_version (dataset_version_id),
	CONSTRAINT data_set_version_field_2_field
		FOREIGN KEY (field_id) REFERENCES field (field_id)
);

CREATE TABLE study_db_version (
	study_db_version_id BIGINT NOT NULL,
	study_db_version_name VARCHAR(50) NOT NULL,
	study_id BIGINT NOT NULL,
	load_started TIMESTAMP,
	load_stopped TIMESTAMP,
	is_current TINYINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_study_db_version PRIMARY KEY (study_db_version_id),
	CONSTRAINT fk_study_db_version_2_study FOREIGN KEY (study_id) REFERENCES study(study_id),
	CONSTRAINT u_study_db_version_name_study_id UNIQUE (study_db_version_name, study_id)
);

CREATE TABLE study_db_version_config (
	study_db_version_id BIGINT NOT NULL,
	dataset_version_id BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_study_db_version_config PRIMARY KEY(study_db_version_id, dataset_version_id),
	CONSTRAINT fk_study_db_version_config_2_study_db_version FOREIGN KEY (study_db_version_id) REFERENCES study_db_version(study_db_version_id),
	CONSTRAINT fk_study_db_version_config_2_dataset_version FOREIGN KEY (dataset_version_id) REFERENCES dataset_version(dataset_version_id)
);

CREATE TABLE dataset_modification (
	dataset_modification_id BIGINT NOT NULL,
	study_db_version_id BIGINT NOT NULL,
	dataset_id BIGINT NOT NULL,
	is_new TINYINT NOT NULL DEFAULT 0,
	is_modified TINYINT NOT NULL DEFAULT 0,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_dataset_modification PRIMARY KEY (dataset_modification_id),
	CONSTRAINT fk_dataset_modification_2_study_db_version
		FOREIGN KEY (study_db_version_id) REFERENCES study_db_version(study_db_version_id),
	CONSTRAINT fk_dataset_modification_2_dataset
		FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id)
);

CREATE TABLE loader_issue (
	loader_issue_id BIGINT AUTO_INCREMENT NOT NULL,
	message VARCHAR(500),
	stack_trace TEXT,
	issue_level VARCHAR(50) NOT NULL,
	study_db_version_id BIGINT,
	dataset_version_id BIGINT,
	study_id BIGINT,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_loader_issue PRIMARY KEY (loader_issue_id),
	CONSTRAINT fk_issue_2_study_db_version FOREIGN KEY (study_db_version_id)
		REFERENCES study_db_version(study_db_version_id),
	CONSTRAINT fk_issue_2_dataset_version FOREIGN KEY (dataset_version_id)
		REFERENCES dataset_version(dataset_version_id),
	CONSTRAINT fk_issue_2_study FOREIGN KEY (study_id)
		REFERENCES study(study_id)
);

CREATE TABLE field_instance (
	field_instance_id BIGINT NOT NULL,
	field_id BIGINT NOT NULL,
	dataset_id BIGINT NOT NULL,
	first_dataset_version_id BIGINT NOT NULL,
	is_potential_splitter TINYINT NOT NULL DEFAULT 0,
	is_potential_splittee TINYINT NOT NULL DEFAULT 0,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	modified_by VARCHAR(25) NOT NULL DEFAULT 'system',
	CONSTRAINT pk_field_instance PRIMARY KEY (field_instance_id),
	CONSTRAINT fk_field_instance_2_field FOREIGN KEY (field_id) REFERENCES field(field_id),
	CONSTRAINT fk_field_instance_2_dataset FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id),
	CONSTRAINT fk_field_instance_2_dataset_version FOREIGN KEY (first_dataset_version_id)
		REFERENCES dataset_version (dataset_version_id),
	CONSTRAINT u_field_instance UNIQUE (field_id, dataset_id)
);

CREATE TABLE merge_field (
	merge_field_id BIGINT AUTO_INCREMENT NOT NULL,
	field_instance_id_1 BIGINT NOT NULL,
	field_instance_id_2 BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	modified_by VARCHAR(25) NOT NULL DEFAULT 'system',
	CONSTRAINT pk_merge_field PRIMARY KEY (merge_field_id),
	CONSTRAINT fk_merge_field_2_field_instance_1 FOREIGN KEY (field_instance_id_1)
		REFERENCES field_instance(field_instance_id),
	CONSTRAINT fk_merge_field_2_field_instance_2 FOREIGN KEY (field_instance_id_2)
		REFERENCES field_instance(field_instance_id),
	CONSTRAINT u_merge_field UNIQUE (field_instance_id_1, field_instance_id_2)
);

CREATE TABLE correlation (
	correlation_id BIGINT AUTO_INCREMENT NOT NULL,
	field_instance_id_1 BIGINT NOT NULL,
	field_instance_id_2 BIGINT NOT NULL,
	study_id BIGINT NOT NULL,
	coefficient DOUBLE NOT NULL,
	CONSTRAINT pk_correlation PRIMARY KEY (correlation_id),
	CONSTRAINT fk_correlation_2_field_instance_1 FOREIGN KEY (field_instance_id_1)
		REFERENCES field_instance(field_instance_id),
	CONSTRAINT fk_correlation_2_field_instance_2 FOREIGN KEY (field_instance_id_2)
		REFERENCES field_instance(field_instance_id),
	CONSTRAINT fk_correlation_2_study FOREIGN KEY (study_id)
		REFERENCES study(study_id)
);

CREATE TABLE checks (
	check_id BIGINT AUTO_INCREMENT NOT NULL,
	check_name VARCHAR(50) NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_checks PRIMARY KEY (check_id),
	CONSTRAINT u_check_name UNIQUE (check_name)
);

insert into checks(check_name)
values('UNIVARIATE_OUTLIER');

insert into checks(check_name)
values('BIVARIATE_OUTLIER');

CREATE TABLE bivariate_check (
	bivariate_check_id BIGINT AUTO_INCREMENT NOT NULL,
	x_field_instance_id BIGINT NOT NULL,
	y_field_instance_id BIGINT NOT NULL,
	check_id BIGINT NOT NULL,
	study_id BIGINT NOT NULL,
	CONSTRAINT pk_bivariate_check PRIMARY KEY (bivariate_check_id),
	CONSTRAINT fk_bivariate_check_2_field_instance_1 FOREIGN KEY (x_field_instance_id)
		REFERENCES field_instance (field_instance_id),
	CONSTRAINT fk_bivariate_check_2_field_instance_2 FOREIGN KEY (y_field_instance_id)
		REFERENCES field_instance (field_instance_id),
	CONSTRAINT fk_bivariate_check_2_check FOREIGN KEY (check_id)
		REFERENCES checks(check_id),
	CONSTRAINT fk_bivariate_check_2_study FOREIGN KEY (study_id)
		REFERENCES study(study_id)
);

CREATE TABLE check_param (
	check_param_id BIGINT AUTO_INCREMENT NOT NULL,
	param_name VARCHAR(50) NOT NULL,
	param_value VARCHAR(50) NOT NULL,
	param_scope VARCHAR(50) NOT NULL,
	study_id BIGINT,
	dataset_id BIGINT,
	field_id BIGINT,
	bivariate_check_id BIGINT,
	check_id BIGINT NOT NULL,
	is_current TINYINT NOT NULL DEFAULT 1,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	user_session_id BIGINT NOT NULL,
	CONSTRAINT pk_check_param PRIMARY KEY (check_param_id),
	CONSTRAINT fk_check_param_2_study FOREIGN KEY (study_id) REFERENCES study(study_id),
	CONSTRAINT fk_check_param_2_dataset FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id),
	CONSTRAINT fk_check_param_2_field FOREIGN KEY (field_id) REFERENCES field(field_id),
	CONSTRAINT fk_check_param_2_bivariate_check FOREIGN KEY (bivariate_check_id) REFERENCES bivariate_check(bivariate_check_id),
	CONSTRAINT fk_check_param_2_checks FOREIGN KEY (check_id) REFERENCES checks(check_id),
	CONSTRAINT fk_check_param_2_user_session FOREIGN KEY (user_session_id) REFERENCES user_session(user_session_id)
);

insert into check_param (param_name, param_value, param_scope, check_id, user_session_id)
values('data_types', 'continuous', 'GLOBAL', (select check_id from checks where check_name = 'UNIVARIATE_OUTLIER'),
(select user_session_id from user_session where web_session_id = '0'));

insert into check_param (param_name, param_value, param_scope, check_id, user_session_id)
values('sd', '2', 'GLOBAL', (select check_id from checks where check_name = 'UNIVARIATE_OUTLIER'),
(select user_session_id from user_session where web_session_id = '0'));

insert into check_param (param_name, param_value, param_scope, check_id, user_session_id)
values('min-univariate', '25', 'GLOBAL', (select check_id from checks where check_name = 'UNIVARIATE_OUTLIER'),
(select user_session_id from user_session where web_session_id = '0'));

insert into check_param (param_name, param_value, param_scope, check_id, user_session_id)
values('sd-residual', '2', 'GLOBAL', (select check_id from checks where check_name = 'BIVARIATE_OUTLIER'),
(select user_session_id from user_session where web_session_id = '0'));

insert into check_param (param_name, param_value, param_scope, check_id, user_session_id)
values('num-nearest-neighbors', '5', 'GLOBAL', (select check_id from checks where check_name = 'BIVARIATE_OUTLIER'),
(select user_session_id from user_session where web_session_id = '0'));

insert into check_param (param_name, param_value, param_scope, check_id, user_session_id)
values('sd-density', '6', 'GLOBAL', (select check_id from checks where check_name = 'BIVARIATE_OUTLIER'),
(select user_session_id from user_session where web_session_id = '0'));

insert into check_param (param_name, param_value, param_scope, check_id, user_session_id)
values('sd-density', '6', 'GLOBAL', (select check_id from checks where check_name = 'BIVARIATE_OUTLIER'),
(select user_session_id from user_session where web_session_id = '0'));

insert into check_param (param_name, param_value, param_scope, check_id, user_session_id)
values('min-bivariate', '25', 'GLOBAL', (select check_id from checks where check_name = 'BIVARIATE_OUTLIER'),
(select user_session_id from user_session where web_session_id = '0'));

CREATE TABLE check_param_change (
	check_param_change_id BIGINT AUTO_INCREMENT NOT NULL,
	old_check_param_id BIGINT NOT NULL,
	new_check_param_id BIGINT NOT NULL,
	user_session_id BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_check_param_change PRIMARY KEY (check_param_change_id),
	CONSTRAINT fk_check_param_change_2_check_param_old FOREIGN KEY (old_check_param_id)
		REFERENCES check_param(check_param_id),
	CONSTRAINT fk_check_param_change_2_check_param_new FOREIGN KEY (new_check_param_id)
		REFERENCES check_param(check_param_id),
	CONSTRAINT fk_check_param_change_2_user_session FOREIGN KEY (user_session_id)
		REFERENCES user_session(user_session_id)
);

CREATE TABLE check_run (
	check_run_id BIGINT AUTO_INCREMENT NOT NULL,
	dataset_version_id BIGINT NOT NULL,
	dataset_version_2_id BIGINT,
	check_id BIGINT NOT NULL,
	field_id BIGINT,
	bivariate_check_id BIGINT,
	is_latest TINYINT NOT NULL DEFAULT 0,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_check_run PRIMARY KEY (check_run_id),
	CONSTRAINT fk_check_run_2_dataset_version FOREIGN KEY (dataset_version_id)
		REFERENCES dataset_version(dataset_version_id),
	CONSTRAINT fk_check_run_2_check FOREIGN KEY (check_id)
		REFERENCES checks (check_id),
	CONSTRAINT fk_check_runb_2_field FOREIGN KEY (field_id)
		REFERENCES field(field_id),
	CONSTRAINT fk_check_run_2_bivariate_check FOREIGN KEY (bivariate_check_id)
		REFERENCES bivariate_check(bivariate_check_id)
);

CREATE TABLE param_used (
	param_used_id BIGINT AUTO_INCREMENT NOT NULL,
	param_name VARCHAR(50) NOT NULL,
	param_value VARCHAR(50) NOT NULL,
	check_run_id BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_param_used PRIMARY KEY (param_used_id),
	CONSTRAINT fk_param_used_2_check_run FOREIGN KEY (check_run_id)
		REFERENCES check_run (check_run_id)
);

CREATE TABLE observation (
	observation_id BIGINT NOT NULL,
	dataset_id BIGINT NOT NULL,
	first_dataset_version_id BIGINT NOT NULL,
	site_id BIGINT NOT NULL,
	subject_id BIGINT NOT NULL,
	phase_id BIGINT NOT NULL,
	record_id VARCHAR(50) NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_observation PRIMARY KEY (observation_id),
	CONSTRAINT fk_observation_2_dataset FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id),
	CONSTRAINT fk_observation_2_dataset_version FOREIGN KEY (first_dataset_version_id)
		REFERENCES dataset_version(dataset_version_id),
	CONSTRAINT fk_observation_2_site FOREIGN KEY (site_id) REFERENCES site(site_id),
	CONSTRAINT fk_observation_2_subject FOREIGN KEY (subject_id) REFERENCES subject(subject_id),
	CONSTRAINT fk_observation_2_phase FOREIGN KEY (phase_id) REFERENCES phase(phase_id),
	CONSTRAINT u_observation UNIQUE (dataset_id, subject_id, phase_id, record_id)
);

CREATE TABLE datum (
	datum_id BIGINT NOT NULL,
	field_id BIGINT NOT NULL,
	first_dataset_version_id BIGINT NOT NULL,
	observation_id BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_datum PRIMARY KEY (datum_id),
	CONSTRAINT fk_datum_2_field FOREIGN KEY (field_id) REFERENCES field(field_id),
	CONSTRAINT fk_datum_2_dataset_version FOREIGN KEY (first_dataset_version_id)
		REFERENCES dataset_version(dataset_version_id),
	CONSTRAINT fk_datum_2_observation FOREIGN KEY (observation_id) REFERENCES observation(observation_id),
	CONSTRAINT u_field_observation UNIQUE (field_id, observation_id)
);

CREATE TABLE datum_version (
	datum_version_id BIGINT NOT NULL,
	value VARCHAR(50),
	first_dataset_version_id BIGINT NOT NULL,
	last_dataset_version_id BIGINT,
	is_current TINYINT NOT NULL DEFAULT 0,
	datum_id BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_datum_version PRIMARY KEY (datum_version_id),
	CONSTRAINT fk_datum_version_2_datum FOREIGN KEY (datum_id) REFERENCES datum (datum_id),
	CONSTRAINT fk_datum_version_2_dataset_version_first FOREIGN KEY (first_dataset_version_id)
		REFERENCES dataset_version(dataset_version_id),
	CONSTRAINT fk_datum_version_2_dataset_version_last FOREIGN KEY (last_dataset_version_id)
		REFERENCES dataset_version(dataset_version_id)
);

CREATE TABLE datum_change (
	datum_change_id BIGINT NOT NULL,
	old_datum_version_id BIGINT NOT NULL,
	new_datum_version_id BIGINT NOT NULL,
	dataset_version_id BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_datum_change PRIMARY KEY (datum_change_id),
	CONSTRAINT fk_datum_change_2_datum_version_old FOREIGN KEY (old_datum_version_id)
		REFERENCES datum_version(datum_version_id),
	CONSTRAINT fk_datum_change_2_datum_version_new FOREIGN KEY (new_datum_version_id)
		REFERENCES datum_version(datum_version_id),
	CONSTRAINT fk_datum_change_2_dataset_version FOREIGN KEY (dataset_version_id)
		REFERENCES dataset_version(dataset_version_id)
);

CREATE TABLE datum_dataset_version (
	datum_version_id BIGINT NOT NULL,
	dataset_version_id BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_datum_dataset_version PRIMARY KEY (datum_version_id, dataset_version_id),
	CONSTRAINT fk_datum_dataset_version_2_datum_version FOREIGN KEY (datum_version_id)
		REFERENCES datum_version(datum_version_id),
	CONSTRAINT fk_datum_dataset_version_2_dataset_version FOREIGN KEY (dataset_version_id)
		REFERENCES dataset_version(dataset_version_id)
);

CREATE TABLE anomaly_resolution (
	anomaly_resolution_id BIGINT NOT NULL,
	anomaly_resolution_code VARCHAR(20) NOT NULL,
	anomaly_resolution_description VARCHAR(100) NOT NULL,
	CONSTRAINT pk_anomaly_resolution PRIMARY KEY (anomaly_resolution_id)
);

INSERT INTO anomaly_resolution values (1, 'DATA_REMOVED', 'Data removed from clinical database');
INSERT INTO anomaly_resolution values (2, 'DATA_CHANGED', 'Anomalous data value changed');
INSERT INTO anomaly_resolution values (3, 'CHECK_PARAMS_CHANGED', 'Check parameters changed');
INSERT INTO anomaly_resolution values (4, 'USER_LABEL', 'User labeled data a non-issue');
INSERT INTO anomaly_resolution values (5, 'DISTRIBUTION_CHANGED', 'Statistical distribution of dataset changed');

CREATE TABLE anomaly (
	anomaly_id BIGINT AUTO_INCREMENT NOT NULL,
	subject_id BIGINT NOT NULL,
	site_id BIGINT NOT NULL,
	check_id BIGINT NOT NULL,
	field_id BIGINT NOT NULL,
	field_2_id BIGINT,
	field_instance_id BIGINT,
	field_instance_2_id BIGINT,
	phase_id BIGINT NOT NULL,
	record_id VARCHAR(50) NOT NULL,
	has_been_viewed TINYINT NOT NULL DEFAULT 0,
	is_an_issue TINYINT NOT NULL DEFAULT 1,
	anomaly_resolution_id BIGINT,
	resolution_time TIMESTAMP,
	resolution_user_session_id BIGINT,
	resolution_check_run_id BIGINT,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_anomaly PRIMARY KEY (anomaly_id),
	CONSTRAINT fk_anomaly_2_subject FOREIGN KEY (subject_id) REFERENCES subject(subject_id),
	CONSTRAINT fk_anomaly_2_site FOREIGN KEY (site_id) REFERENCES site(site_id),
	CONSTRAINT fk_anomaly_2_checks FOREIGN KEY (check_id) REFERENCES checks(check_id),
	CONSTRAINT fk_anomaly_2_field FOREIGN KEY (field_id) REFERENCES field(field_id),
	CONSTRAINT fk_anomaly_2_phase FOREIGN KEY (phase_id) REFERENCES phase(phase_id),
	CONSTRAINT fk_anomaly_2_field_2 FOREIGN KEY (field_2_id) REFERENCES field(field_id),
	CONSTRAINT fk_anomaly_2_field_instance FOREIGN KEY (field_instance_id) REFERENCES field_instance(field_instance_id),
	CONSTRAINT fk_anomaly_2_field_instance_2 FOREIGN KEY (field_instance_2_id) REFERENCES field_instance(field_instance_id),
	CONSTRAINT fk_anomaly_2_anomaly_resolution FOREIGN KEY (anomaly_resolution_id) REFERENCES anomaly_resolution(anomaly_resolution_id),
	CONSTRAINT fk_anomaly_2_check_run FOREIGN KEY (resolution_check_run_id) REFERENCES check_run(check_run_id),
	CONSTRAINT fk_anomaly_2_user_session FOREIGN KEY (resolution_user_session_id) REFERENCES user_session(user_session_id)
);

CREATE TABLE anomaly_datum_version (
	anomaly_id BIGINT NOT NULL,
	datum_version_id BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_anomaly_datum_version PRIMARY KEY (anomaly_id, datum_version_id),
	CONSTRAINT fk_anomaly_datum_version_2_anomaly FOREIGN KEY (anomaly_id) REFERENCES anomaly(anomaly_id),
	CONSTRAINT fk_anomaly_datum_version_2_datum_version FOREIGN KEY (datum_version_id) REFERENCES datum_version(datum_version_id)
);

CREATE TABLE anomaly_datum_version_2 (
	anomaly_id BIGINT NOT NULL,
	datum_version_id BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_anomaly_datum_version_2 PRIMARY KEY (anomaly_id, datum_version_id),
	CONSTRAINT fk_anomaly_datum_version_2_2_anomaly FOREIGN KEY (anomaly_id) REFERENCES anomaly(anomaly_id),
	CONSTRAINT fk_anomaly_datum_version_2_2_datum_version FOREIGN KEY (datum_version_id) REFERENCES datum_version(datum_version_id)
);

CREATE TABLE anomaly_check_run (
	anomaly_id BIGINT NOT NULL,
	check_run_id BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_anomaly_check_run PRIMARY KEY (anomaly_id, check_run_id),
	CONSTRAINT fk_anomaly_check_run_2_anomaly FOREIGN KEY (anomaly_id) REFERENCES anomaly(anomaly_id),
	CONSTRAINT fk_anomaly_check_run_2_check_run FOREIGN KEY (check_run_id) REFERENCES check_run(check_run_id)
);

CREATE TABLE data_property (
	data_property_id BIGINT AUTO_INCREMENT NOT NULL,
	data_property_name VARCHAR(50),
	data_property_value VARCHAR(50),
	check_run_id BIGINT NOT NULL,
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_data_property PRIMARY KEY (data_property_id),
	CONSTRAINT fk_data_property_2_check_run FOREIGN KEY (check_run_id) REFERENCES check_run (check_run_id),
	CONSTRAINT u_data_property UNIQUE (data_property_name, check_run_id)
);

create or replace view bivariate_anomaly as
select acr.check_run_id as check_run_id,
a.anomaly_id as anomaly_id,
a.field_instance_id as field_instance_1_id,
a.field_instance_2_id as field_instance_2_id,
f1.field_name as field_name_1,
f2.field_name as field_name_2,
dv1.value as anomalous_value_1,
dv2.value as anomalous_value_2,
s.subject_id as subject_id,
s.subject_name as subject_name,
si.site_id as site_id,
si.site_name as site_name,
p.phase_id as phase_id,
p.phase_name as phase_name,
a.record_id as record_id
from anomaly a
join subject s on s.subject_id = a.subject_id
join phase p on p.phase_id = a.phase_id
join site si on si.site_id = a.site_id
join field_instance fi1 on fi1.field_instance_id = a.field_instance_id
join field_instance fi2 on fi2.field_instance_id = a.field_instance_2_id
join field f1 on f1.field_id = fi1.field_id
join field f2 on f2.field_id = fi2.field_id
join anomaly_check_run acr on acr.anomaly_id = a.anomaly_id
join anomaly_datum_version adv1 on adv1.anomaly_id = a.anomaly_id
join anomaly_datum_version adv2 on adv2.anomaly_id = a.anomaly_id
join datum_version dv1 on dv1.datum_version_id = adv1.datum_version_id
join datum_version dv2 on dv2.datum_version_id = adv2.datum_version_id;

CREATE TABLE query_status (
	query_status_id BIGINT NOT NULL,
	query_status_name VARCHAR(50) NOT NULL,
	query_status_label VARCHAR(50) NOT NULL,
	CONSTRAINT pk_query_status PRIMARY KEY (query_status_id)
);

INSERT INTO query_status(query_status_id, query_status_name, query_status_label)
VALUES (1, "NOT-OPENED", "Query Not Opened");

INSERT INTO query_status(query_status_id, query_status_name, query_status_label)
VALUES (2, "OPENED", "Query Opened");

INSERT INTO query_status(query_status_id, query_status_name, query_status_label)
VALUES (3, "CLOSED", "Query Closed");

CREATE TABLE query_candidate (
	query_candidate_id BIGINT AUTO_INCREMENT NOT NULL,
	anomaly_id BIGINT NOT NULL,
	created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	created_by VARCHAR(50),
	query_status_id BIGINT NOT NULL DEFAULT 1,
	modified_by VARCHAR(50),
	last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT pk_query_candidate PRIMARY KEY (query_candidate_id),
	CONSTRAINT fk_query_candidate_2_anomaly FOREIGN KEY (anomaly_id) REFERENCES anomaly(anomaly_id)
);

CREATE TABLE event (
	event_id BIGINT AUTO_INCREMENT NOT NULL,
	event_type VARCHAR(50) NOT NULL,
	user_session_id BIGINT,
	study_id BIGINT,
	data_location_id BIGINT,
	study_db_version_id BIGINT,
	check_param_id BIGINT,
	check_param_change_id BIGINT,
	dataset_id BIGINT,
	field_id BIGINT,
	created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT pk_event PRIMARY KEY (event_id),
	CONSTRAINT fk_event_2_user_session FOREIGN KEY (user_session_id)
		REFERENCES user_session(user_session_id),
	CONSTRAINT fk_event_2_study FOREIGN KEY (study_id)
		REFERENCES study(study_id),
	CONSTRAINT fk_event_2_data_location FOREIGN KEY (data_location_id)
		REFERENCES data_location(data_location_id),
	CONSTRAINT fk_event_2_study_db_version FOREIGN KEY (study_db_version_id)
		REFERENCES study_db_version(study_db_version_id),
	CONSTRAINT fk_event_2_check_param FOREIGN KEY (check_param_id)
		REFERENCES check_param (check_param_id),
	CONSTRAINT fk_event_2_check_param_change FOREIGN KEY (check_param_change_id)
		REFERENCES check_param_change (check_param_change_id),
	CONSTRAINT fk_event_2_dataset FOREIGN KEY (dataset_id)
		REFERENCES dataset(dataset_id),
	CONSTRAINT fk_event_2_field FOREIGN KEY (field_id)
		REFERENCES field(field_id)
);

create or replace view uni_anomaly_dto as
select acr.check_run_id as check_run_id,
a.anomaly_id as anomaly_id,
f.field_id as field_id,
f.field_name as field_name,
dv.value as anomalous_value,
s.subject_id as subject_id,
s.subject_name as subject_name,
si.site_id as site_id,
si.site_name as site_name,
p.phase_id as phase_id,
p.phase_name as phase_name,
a.record_id as record_id,
a.is_an_issue as is_an_issue,
qc.query_candidate_id as query_candidate_id
from anomaly a
join subject s on s.subject_id = a.subject_id
join phase p on p.phase_id = a.phase_id
join site si on si.site_id = a.site_id
join field f on f.field_id = a.field_id 
join anomaly_check_run acr on acr.anomaly_id = a.anomaly_id
join anomaly_datum_version adv on adv.anomaly_id = a.anomaly_id
join datum_version dv on dv.datum_version_id = adv.datum_version_id
left join query_candidate qc on qc.anomaly_id = a.anomaly_id;

/*
CREATE TABLE data_load_job (
	data_load_job_id BIGINT AUTO_INCREMENT NOT NULL,
	study_id BIGINT NOT NULL,
	study_db_version_id BIGINT,
	status VARCHAR(50),
	created_on DATETIME DEFAULT CURRENT_DATETIME,
	created_by VARCHAR(50),
	started_on DATETIME,
	ended_on DATETIME,
	CONSTRAINT pk_data_load_job PRIMARY KEY (data_load_job_id),
	CONSTRAINT fk_data_load_job_2_study FOREIGN KEY (study_id)
		REFERENCES study(study_id),
	CONSTRAINT fk_data_load_job_2_study_db_version FOREIGN KEY (study_db_version_id)
		REFERENCES study_db_version (study_db_version_id)
);
*/
