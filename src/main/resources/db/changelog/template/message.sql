--liquibase formatted sql

--changeset maxdrv:create_message_table
CREATE TABLE message (
  id                BIGSERIAL PRIMARY KEY,
  created_at        TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at        TIMESTAMP WITH TIME ZONE DEFAULT now(),
  payload           TEXT,
  source            TEXT,
  dist              TEXT
)
