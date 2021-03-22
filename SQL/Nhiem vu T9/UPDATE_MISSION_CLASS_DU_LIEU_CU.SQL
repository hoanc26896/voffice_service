-- Cap nhat mission_class = 1

update Mission
set mission_class = 1
where mission_id in(Select m.Mission_id  from Mission m where m.text_id_approve is null and m.mission_class is null);


commit;


--- Cap nhat mission_class = 2


update Mission
set mission_class = 2
where mission_id in(Select m.Mission_id  from Mission m where m.text_id_approve is not null and m.mission_class is null);


commit;