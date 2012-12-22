# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table feeditemmedia (
  id                        bigint auto_increment not null,
  feeditem_id               bigint,
  scraper_id                bigint,
  url                       varchar(255),
  type                      varchar(255),
  `primary`                 integer,
  created                   datetime,
  flags                     integer,
  hash                      varchar(255),
  abs_path                  varchar(255),
  width                     bigint,
  height                    bigint,
  constraint pk_feeditemmedia primary key (id))
;




# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table feeditemmedia;

SET FOREIGN_KEY_CHECKS=1;

