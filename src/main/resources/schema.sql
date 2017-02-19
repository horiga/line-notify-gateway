DROP TABLE IF EXISTS service;
CREATE TABLE IF NOT EXISTS `service` (
  `service_id` VARCHAR(100) NOT NULL DEFAULT '',
  `display_name` VARCHAR(100) NOT NULL DEFAULT '',
  `type` VARCHAR(10) NOT NULL DEFAULT 'direct' COMMENT 'direct, alert, webhook',
  `template_group_id` VARCHAR(30) NOT NULL DEFAULT '',
  `template_mapping_type` VARCHAR(30) NOT NULL DEFAULT 'http.header',
  `template_mapping_value` VARCHAR(30) NOT NULL DEFAULT 'X-GitHub-Event',
  `description` VARCHAR(300) NOT NULL DEFAULT '',
  PRIMARY KEY (`service_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS token;
CREATE TABLE IF NOT EXISTS `token` (
  `id` VARCHAR(33) NOT NULL DEFAULT '',
  `service_id` VARCHAR(100) NOT NULL DEFAULT '',
  `token` VARCHAR(40) NOT NULL DEFAULT '',
  `description` VARCHAR(300) NOT NULL DEFAULT '',
  `owner` VARCHAR(300) NOT NULL DEFAULT '' COMMENT 'LINE Notify access token owner mail or LINE ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_token_key` (`service_id`,`token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS template_group;
CREATE TABLE IF NOT EXISTS `template_group` (
  `group_id` VARCHAR(100) NOT NULL DEFAULT '',
  `display_name` VARCHAR(100) NOT NULL DEFAULT '',
  `description` VARCHAR(100) NOT NULL DEFAULT '',
  PRIMARY KEY (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS template;
CREATE TABLE IF NOT EXISTS `template` (
  `id` VARCHAR(33) NOT NULL DEFAULT '',
  `group_id` VARCHAR(100) NOT NULL DEFAULT '',
  `mapping_value` VARCHAR(300) NOT NULL DEFAULT 'push, issues, issue_comment...',
  `description` VARCHAR(40) NOT NULL DEFAULT '',
  `content` VARCHAR(1000) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_template_key` (`group_id`,`mapping_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- system alert direct message
INSERT INTO service(`service_id`, `display_name`, `type`, `template_group_id`, `template_mapping_type`, `template_mapping_value`, `description`) VALUES ('alert', 'Direct alert message', 'direct', 'simple_direct', 'none', 'none', 'Direct message for Your system alerts.');

-- service: github webhook
INSERT INTO service(`service_id`, `display_name`, `type`, `template_group_id`, `template_mapping_type`, `template_mapping_value`, `description`) VALUES ('github', 'GitHub Enterprise', 'payload', 'ghe_default', 'http.header', 'X-GitHub-Event', 'GitHub webhook service');

-- simple direct message
INSERT INTO template_group(`group_id`, `display_name`, `description`) VALUES('simple_direct', 'Direct message', 'A simple notify message');
INSERT INTO template(`id`, `group_id`, `mapping_value`, `description`, `content`) VALUES('10001', 'simple_direct', 'none', 'A simple direct message, for CLI', '{{{message}}}');

-- github enterprise
INSERT INTO template_group(`group_id`, `display_name`, `description`) VALUES('ghe_default', 'GitHub Enterprise Webhook', 'A template for GitHub Enterprise webhook events');
INSERT INTO template(`id`, `group_id`, `mapping_value`, `description`, `content`) VALUES('20001', 'ghe_default', 'create', 'create', '[{{repository.full_name}}] "{{sender.login}}" create {{ref_type}} "{{ref}}". {{repository.html_url}}');
INSERT INTO template(`id`, `group_id`, `mapping_value`, `description`, `content`) VALUES('20002', 'ghe_default', 'issues', 'issues', '[{{repository.full_name}}] "{{sender.login}}" {{action}} issue #{{issue.number}} {{issue.html_url}}');
INSERT INTO template(`id`, `group_id`, `mapping_value`, `description`, `content`) VALUES('20003', 'ghe_default', 'issue_comment', 'issue_comment', '[{{repository.full_name}}] "{{sender.login}}" {{action}} comment #{{issue.number}} {{comment.html_url}} {{{comment.body}}}');
INSERT INTO template(`id`, `group_id`, `mapping_value`, `description`, `content`) VALUES('20004', 'ghe_default', 'pull_request', 'pull_request', '[{{repository.full_name}}] "{{sender.login}}" {{action}} pull request into "{{pull_request.base.ref}}" from "{{pull_request.head.ref}} {{pull_request.html_url}}". {{{pull_request.title}}}');
INSERT INTO template(`id`, `group_id`, `mapping_value`, `description`, `content`) VALUES('20005', 'ghe_default', 'pull_request_review', 'pull_request_review', '[{{repository.full_name}}] "{{review.user.login}}" submitted pull_request review. {{review.html_url}} [submitted comment: #{{pull_request.number}} "{{{pull_request.title}}}"]\n{{{review.body}}}');
INSERT INTO template(`id`, `group_id`, `mapping_value`, `description`, `content`) VALUES('20006', 'ghe_default', 'pull_request_review_comment', 'pull_request_review_comment', '[{{repository.full_name}}] "{{sender.login}}" {{action}} comment #{{pull_request.number}} {{pull_request.html_url}} {{{comment.body}}}');
INSERT INTO template(`id`, `group_id`, `mapping_value`, `description`, `content`) VALUES('20007', 'ghe_default', 'release', 'release', '[{{repository.full_name}}] published release "{{release.tag_name}}" by "{{release.author.login}}". {{release.html_url}}\n{{{release.body}}}');

