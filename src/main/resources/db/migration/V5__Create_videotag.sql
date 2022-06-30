create table IF NOT EXISTS videotag
(
    id serial not null
        constraint videotag_pk
            primary key,
    videoId integer not null
        constraint videotag_video_id_fk
            references dbname.video on delete cascade,
    tagId integer not null
        constraint videotag_tag_id_fk
            references dbname.tag on delete cascade
);

create index IF NOT EXISTS videotag_tagId_index
    on dbname.videotag (tagId);

create index IF NOT EXISTS videotag_videoId_index
    on dbname.videotag (videoId);