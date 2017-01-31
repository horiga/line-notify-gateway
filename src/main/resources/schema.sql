CREATE TABLE IF NOT EXISTS `service` (
  `service` varchar(33) NOT NULL DEFAULT '',
  `description` varchar(300) NOT NULL DEFAULT '',
  PRIMARY KEY (`service`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `token` (
  `id` varchar(33) NOT NULL DEFAULT '',
  `service` varchar(10) NOT NULL DEFAULT '',
  `token` varchar(40) NOT NULL DEFAULT '',
  `description` varchar(300) NOT NULL DEFAULT '',
  `owner` varchar(300) NOT NULL DEFAULT '' COMMENT 'LINE Notify access token owner mail or LINE ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_token_key` (`service`,`token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;