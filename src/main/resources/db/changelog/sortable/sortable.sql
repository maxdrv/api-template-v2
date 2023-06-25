--liquibase formatted sql


--changeset maxdrv:create_stage_table
create table stage(
  id                BIGSERIAL PRIMARY KEY,
  system_name       TEXT,
  status            TEXT
);

--changeset maxdrv:insert_stages
insert into stage(system_name, status) values ('A', 'CREATED'), ('B', 'CREATED'), ('C', 'ARRIVED'), ('D', 'IN_PROGRESS'), ('E', 'IN_PROGRESS'), ('F', 'IN_PROGRESS'), ('G', 'CANCELLED'), ('H', 'SHIPPED');

--changeset maxdrv:create_sc_table
create table sorting_center(
  id              BIGSERIAL PRIMARY KEY,
  name            TEXT
);

--changeset maxdrv:insert_sc
insert into sorting_center(name) values ('A'), ('B'), ('C'), ('D'), ('E'), ('F'), ('G'), ('H')

--changeset maxdrv:archive:create_archive_table
create table archive(
  id                BIGSERIAL PRIMARY KEY,
  created_at        TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at        TIMESTAMP WITH TIME ZONE DEFAULT now(),
  marked            bigint,
  deleted           bigint
)

--changeset maxdrv:create_sortable_table
CREATE TABLE sortable (
  id                BIGSERIAL PRIMARY KEY,
  created_at        TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at        TIMESTAMP WITH TIME ZONE DEFAULT now(),
  sc_id             bigint REFERENCES sorting_center(id),
  status            TEXT,
  type              TEXT,
  stage_id          bigint REFERENCES stage(id),
  barcode           TEXT,
  archive_id        bigint REFERENCES archive(id)
);

--changeset maxdrv:sortable_archive_id_idx runInTransaction:false
CREATE INDEX CONCURRENTLY IF NOT EXISTS sortable_archive_id_idx
    ON sortable (archive_id);

--changeset maxdrv:sortable_sc_id_status_idx runInTransaction:false
CREATE INDEX CONCURRENTLY IF NOT EXISTS sortable_sc_id_status_idx
    ON sortable (sc_id, status);

--changeset maxdrv:sortable_sc_id_stage_id_idx runInTransaction:false
CREATE INDEX CONCURRENTLY IF NOT EXISTS sortable_sc_id_stage_id_idx
    ON sortable (sc_id, stage_id);

--changeset maxdrv:sortable_stage_id_idx runInTransaction:false
CREATE INDEX CONCURRENTLY IF NOT EXISTS sortable_stage_id_idx
    ON sortable (stage_id);

--changeset maxdrv:sortable_barcode_idx runInTransaction:false
create index concurrently if not exists sortable_barcode_idx
    on sortable (barcode);