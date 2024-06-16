-- Multiversus PostgreSQL Schema

CREATE TABLE "{prefix}players" (
  "uuid"          VARCHAR(36) PRIMARY KEY NOT NULL,
  "username"      VARCHAR(16)             NOT NULL
);
CREATE INDEX "{prefix}players_username" ON "{prefix}players" ("username");