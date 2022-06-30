create table IF NOT EXISTS tag
(
  id serial not null
  constraint tag_pk
  primary key,

  name text not null
);

create unique index IF NOT EXISTS tag_name_uindex
on tag (name);