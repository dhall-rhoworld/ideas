insert into data_check (data_check_code, data_check_name, description, num_variables)
values ('UU', 'Unsupervised Univariate',
	'Checks individual numeric variables for extreme values', 1);

insert into global_parameters_version(data_check_id)
values((select data_check_id from data_check where data_check_code = 'UU'));
