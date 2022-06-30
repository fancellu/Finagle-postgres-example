truncate table video;
alter table video add column eventId integer;
alter table video add column hidden boolean not null;