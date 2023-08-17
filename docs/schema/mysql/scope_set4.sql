-- ---------------------------------------------------------------------------------------------------------------------
-- app
-- ---------------------------------------------------------------------------------------------------------------------
CREATE TABLE `app_price_plans`
(
    `id`                       bigint       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `name`                     varchar(100) NOT NULL,
    `request_limit_per_second` bigint       NOT NULL DEFAULT '0',
    `request_limit_per_day`    bigint       NOT NULL DEFAULT '0',
    `request_limit_per_month`  bigint       NOT NULL DEFAULT '0',
    `description`              text                  DEFAULT NULL,
    `display_order`            tinyint      NOT NULL DEFAULT '0',
    `hidden`                   tinyint      NOT NULL DEFAULT '0',
    `allow_limit_over`         tinyint      NOT NULL DEFAULT '0',
    `activated_at`             datetime(6)           DEFAULT NULL,
    `deactivated_at`           datetime(6)           DEFAULT NULL,
    `created_at`               datetime(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created date',
    `updated_at`               datetime(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modified date',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `app_users`
(
    `id`                bigint      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `email_address`     varchar(200)         DEFAULT NULL,
    `app_price_plan_id` bigint      NOT NULL,
    `activated_at`      datetime(6)          DEFAULT NULL,
    `deactivated_at`    datetime(6)          DEFAULT NULL,
    `created_at`        datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created date',
    `updated_at`        datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modified date',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_emailaddress` (`email_address`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `app_user_keys`
(
    `id`             bigint       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `app_user_id`    bigint       NOT NULL,
    `access_key`     varchar(100) NOT NULL,
    `name`           varchar(100) NOT NULL,
    `description`    text                  DEFAULT NULL,
    `activated_at`   datetime(6)           DEFAULT NULL,
    `deactivated_at` datetime(6)           DEFAULT NULL,
    `created_at`     datetime(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created date',
    `updated_at`     datetime(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modified date',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_accesskey` (`access_key`),
    KEY `ix_appuserid_createdat` (`app_user_id`, `created_at` DESC),
    KEY `ix_appuserid_updatedat` (`app_user_id`, `updated_at` DESC),
    KEY `ix_appuserid_activatedat` (`app_user_id`, `activated_at` DESC),
    KEY `ix_appuserid_deactivatedat` (`app_user_id`, `deactivated_at` DESC)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;