insert into study(study_name, form_field_name, site_field_name, subject_field_name, query_file_path)
values ('CTOT-08', 'DATASTR', 'SITE', 'ID', 'S:/RhoFED/CTOT-SACCC/CTOT/CTOT-08-Abecassis/Stats/Data/Clinical/query.sas7bdat');

insert into data_location(folder_path, study_id)
values('S:/RhoFED/CTOT-SACCC/CTOT/CTOT-08-Abecassis/Stats/Data/Clinical',
(select study_id from study where study_name = 'CTOT-08'));

insert into study(study_name, form_field_name, site_field_name, subject_field_name)
values ('PROSE', 'form_name', 'Site', 'RecruitID');

insert into data_location(folder_path, study_id)
values('S:/RhoFED/ICAC2/PROSE/Statistics/Data/Complete',
(select study_id from study where study_name = 'PROSE'));

