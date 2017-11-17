insert into study(study_name, form_field_name, site_field_name, subject_field_name)
values ('CTOT-08', 'DATASTR', 'SITE', 'ID');

insert into data_location(folder_path, study_id)
values('S:/RhoFED/CTOT-SACCC/CTOT/CTOT-08-Abecassis/Stats/Data/Clinical',
(select study_id from study where study_name = 'CTOT-08'));
