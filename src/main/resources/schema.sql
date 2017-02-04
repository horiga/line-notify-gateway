CREATE TABLE IF NOT EXISTS `service` (
  `service` VARCHAR(100) NOT NULL DEFAULT '',
  `type` VARCHAR(10) NOT NULL DEFAULT 'direct' COMMENT 'direct, alert, webhook',
  `messageTemplateGroupId` VARCHAR(30) NOT NULL DEFAULT '',
  `description` VARCHAR(300) NOT NULL DEFAULT '',
  PRIMARY KEY (`service`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `token` (
  `id` VARCHAR(33) NOT NULL DEFAULT '',
  `service` VARCHAR(100) NOT NULL DEFAULT '',
  `token` VARCHAR(40) NOT NULL DEFAULT '',
  `description` VARCHAR(300) NOT NULL DEFAULT '',
  `owner` VARCHAR(300) NOT NULL DEFAULT '' COMMENT 'LINE Notify access token owner mail or LINE ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_token_key` (`service`,`token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `message_template_group` (
  `groupId` VARCHAR(100) NOT NULL DEFAULT '',
  `displayName` VARCHAR(100) NOT NULL DEFAULT '',
  `description` VARCHAR(100) NOT NULL DEFAULT '',
  PRIMARY KEY (`groupId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `message_template` (
  `id` VARCHAR(33) NOT NULL DEFAULT '',
  `groupId` VARCHAR(100) NOT NULL DEFAULT '',
  `displayName` VARCHAR(40) NOT NULL DEFAULT '',
  `description` VARCHAR(40) NOT NULL DEFAULT '',
  `eventKey` VARCHAR(300) NOT NULL DEFAULT '',
  `template` VARCHAR(1000) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


