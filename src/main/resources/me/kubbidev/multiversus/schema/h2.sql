-- Multiversus H2 Schema.

CREATE TABLE `{prefix}players` (
  `uuid`          VARCHAR(36) NOT NULL,
  `username`      VARCHAR(16) NOT NULL,
  PRIMARY KEY (`uuid`)
);
CREATE INDEX ON `{prefix}players` (`username`);