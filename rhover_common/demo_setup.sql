update anomaly set has_been_viewed = 1;

select field_id, field_label from field where field_label like '%Temperature%';

update anomaly set has_been_viewed = 0 where field_id = 352;

select field_id, field_label from field where field_label like '%Systolic%';

update anomaly set has_been_viewed = 0 where field_id = 356;
