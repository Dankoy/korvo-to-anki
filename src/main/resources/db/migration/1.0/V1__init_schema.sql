drop table if exists state;

create table state
(
    id      integer primary key autoincrement,
    word    varchar(60) not null,
    created timestamp not null default current_timestamp
);