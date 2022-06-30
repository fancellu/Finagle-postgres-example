create table IF NOT EXISTS video
(
id serial not null
  constraint video_pk
    primary key,
name text not null,
source text not null,
link text not null,
description text not null
);

create unique index IF NOT EXISTS video_name_uindex
on video (name);