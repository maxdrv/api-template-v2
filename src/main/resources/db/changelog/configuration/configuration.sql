--liquibase formatted sql

--changeset maxdrv:create_configuration
create table if not exists configuration
(
    id             bigserial   primary key,
    created_at     timestamptz not null,
    updated_at     timestamptz not null,
    key            text        not null,
    value          text
);

--changeset maxdrv:uniq for key
CREATE UNIQUE INDEX IF NOT EXISTS configuration_key_unique_idx ON configuration(key);