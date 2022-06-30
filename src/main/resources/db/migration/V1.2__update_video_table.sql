truncate table video;
alter table video
    add column noOfViews integer not null DEFAULT 0,
    add column noOfUpvotes integer not null DEFAULT 0,
    add column noOfDownvotes integer not null DEFAULT 0,
    add column updloadTimestamp timestamptz not null DEFAULT NOW();