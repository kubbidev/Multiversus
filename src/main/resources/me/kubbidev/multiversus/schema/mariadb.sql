-- Multiversus MariaDB Schema

CREATE TABLE `{prefix}players` (
  `uuid`          VARCHAR(36) NOT NULL,
  `username`      VARCHAR(16) NOT NULL,
  PRIMARY KEY (`uuid`)
) DEFAULT CHARSET = utf8mb4;
CREATE INDEX `{prefix}players_username` ON `{prefix}players` (`username`);