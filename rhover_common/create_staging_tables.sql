CREATE TABLE stg_study_db_version (
	study_db_version_id BIGINT PRIMARY KEY,
	study_db_version_name VARCHAR(50),
	study_id BIGINT,
	is_current TINYINT
);

CREATE TABLE stg_dataset (
	dataset_id BIGINT PRIMARY KEY,
	dataset_name VARCHAR(50),
	file_path VARCHAR(400),
	is_checked TINYINT,
	study_id BIGINT
);

CREATE TABLE stg_dataset_version (
	dataset_version_id BIGINT PRIMARY KEY,
	dataset_version_name VARCHAR(50),
	is_current TINYINT,
	num_records INT,
	dataset_id BIGINT
);

CREATE TABLE stg_site (
	site_id BIGINT PRIMARY KEY,
	site_name VARCHAR(200),
	study_id BIGINT
);

CREATE TABLE stg_subject (
	subject_id BIGINT PRIMARY KEY,
	subject_name VARCHAR(50),
	site_id BIGINT
);

CREATE TABLE stg_phase (
	phase_id BIGINT PRIMARY KEY,
	phase_name VARCHAR(200),
	study_id BIGINT
);

CREATE TABLE stg_data_stream (
	data_stream_id BIGINT PRIMARY KEY,
	data_stream_name VARCHAR(50),
	study_id BIGINT
);

CREATE TABLE stg_dataset_version_stream (
	dataset_version_id BIGINT,
	data_stream_id BIGINT
);

CREATE TABLE stg_observation (
	observation_id BIGINT PRIMARY KEY,
	dataset_id BIGINT,
	first_dataset_version_id BIGINT,
	site_id BIGINT,
	subject_id BIGINT,
	phase_id BIGINT,
	record_id VARCHAR(50)
);

CREATE TABLE stg_field (
	field_id BIGINT PRIMARY KEY,
	field_name VARCHAR(200),
	field_label VARCHAR(400),
	study_id BIGINT,
	data_type VARCHAR(50),
	is_skipped TINYINT
);

CREATE TABLE stg_field_instance (
	field_instance_id BIGINT PRIMARY KEY,
	field_id BIGINT,
	dataset_id BIGINT,
	first_dataset_version_id BIGINT
);

CREATE TABLE stg_dataset_version_field (
	dataset_version_id BIGINT,
	field_id BIGINT
);

CREATE TABLE stg_study_db_version_config (
	study_db_version_id BIGINT,
	dataset_version_id BIGINT
);

CREATE TABLE stg_dataset_modification (
	dataset_modification_id BIGINT PRIMARY KEY,
	study_db_version_id BIGINT,
	dataset_id BIGINT,
	is_new TINYINT,
	is_modified TINYINT
);

CREATE TABLE stg_datum (
	datum_id BIGINT PRIMARY KEY,
	field_id BIGINT,
	first_dataset_version_id BIGINT,
	observation_id BIGINT
);

CREATE TABLE stg_datum_version (
	datum_version_id BIGINT PRIMARY KEY,
	value VARCHAR(50),
	first_dataset_version_id BIGINT,
	is_current TINYINT,
	datum_id BIGINT
);

CREATE TABLE stg_datum_change (
	datum_change_id BIGINT PRIMARY KEY,
	old_datum_version_id BIGINT,
	new_datum_version_id BIGINT,
	dataset_version_id BIGINT
);
