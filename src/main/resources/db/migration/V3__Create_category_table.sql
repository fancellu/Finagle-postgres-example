create table IF NOT EXISTS category
(
  id serial not null
  constraint category_pk
  primary key,

  name text not null
);

create unique index IF NOT EXISTS category_name_uindex
on category (name);