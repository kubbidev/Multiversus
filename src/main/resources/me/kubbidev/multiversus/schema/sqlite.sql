-- Multiversus SQLite Schema

CREATE TABLE `{prefix}players` (
  `uuid`          VARCHAR(36) NOT NULL,
  `username`      VARCHAR(16) NOT NULL,
  PRIMARY KEY (`uuid`)
);
CREATE INDEX `{prefix}players_username` ON `{prefix}players` (`username`);