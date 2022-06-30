create table IF NOT EXISTS videocategory
(
    id serial not null
        constraint videocategory_pk
            primary key,
    videoId integer not null
        constraint videocategory_video_id_fk
            references dbname.video on delete cascade,
    categoryId integer not null
        constraint videocategory_category_id_fk
            references dbname.category on delete cascade
);

create index IF NOT EXISTS videocategory_categoryId_index
    on dbname.videocategory (categoryId);

create index IF NOT EXISTS videocategory_videoId_index
    on dbname.videocategory (videoId);