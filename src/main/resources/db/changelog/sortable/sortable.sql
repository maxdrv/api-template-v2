--liquibase formatted sql


--changeset maxdrv:create_stage_table
create table stage(
  id                BIGSERIAL PRIMARY KEY,
  system_name       TEXT,
  status            TEXT
);

--changeset maxdrv:insert_stages
insert into stage(system_name, status) values ('A', 'CREATED'), ('B', 'CREATED'), ('C', 'ARRIVED'), ('D', 'IN_PROGRESS'), ('E', 'IN_PROGRESS'), ('F', 'IN_PROGRESS'), ('G', 'CANCELLED'), ('H', 'SHIPPED');

--changeset maxdrv:create_sortable_table
CREATE TABLE sortable (
  id                BIGSERIAL PRIMARY KEY,
  created_at        TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at        TIMESTAMP WITH TIME ZONE DEFAULT now(),
  status            TEXT,
  type              TEXT,
  stage_id          bigint REFERENCES stage(id),
  barcode           TEXT,
  archive_id        bigint
);

--changeset maxdrv:archive:create_archive_table
create table archive(
  id                BIGSERIAL PRIMARY KEY,
  created_at        TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at        TIMESTAMP WITH TIME ZONE DEFAULT now(),
  marked            bigint,
  deleted           bigint
)
