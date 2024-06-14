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
    `id`                bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `email_address`     varchar(200) DEFAULT NULL,
    `app_price_plan_id` bigint NOT NULL,
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
    `description`    text DEFAULT NULL,
    `activated_at`   datetime(6)           DEFAULT NULL,
    `deactivated_at` datetime(6)           DEFAULT NULL,
    `created_at`     datetime(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created date',
    `updated_at`     datetime(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modified date',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_accesskey` (`access_key`),
    KEY              `ix_appuserid_createdat` (`app_user_id`, `created_at` DESC),
    KEY              `ix_appuserid_updatedat` (`app_user_id`, `updated_at` DESC),
    KEY              `ix_appuserid_activatedat` (`app_user_id`, `activated_at` DESC),
    KEY              `ix_appuserid_deactivatedat` (`app_user_id`, `deactivated_at` DESC)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `token_info`
(
    `id`               bigint NOT NULL AUTO_INCREMENT,
    `cmc_id`           int          DEFAULT NULL,
    `name`             varchar(700) DEFAULT NULL,
    `symbol`           varchar(50)  DEFAULT NULL,
    `contract_address` varchar(42)  DEFAULT NULL,
    `is_active`        int          DEFAULT '1',
    `created_at`       datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at`       datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_contractaddress` (`contract_address`,`symbol`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `token_time_series`
(
    `id`                  bigint NOT NULL AUTO_INCREMENT,
    `token_info_id`       bigint NOT NULL,
    `symbol`              varchar(50)  DEFAULT NULL,
    `price`               varchar(255) DEFAULT NULL,
    `kaia_price`          varchar(255) DEFAULT NULL,
    `change_rate`         varchar(255) DEFAULT NULL,
    `volume`              varchar(255) DEFAULT NULL,
    `market_cap`          varchar(255) DEFAULT NULL,
    `on_chain_market_cap` varchar(255) DEFAULT NULL,
    `timestamp`           int          DEFAULT NULL,
    `created_at`          datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at`          datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_tokeninfoid` (`token_info_id`,`symbol`,`timestamp`) USING BTREE,
    KEY                   `ix_tokeninfoid` (`token_info_id`,`symbol`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `governance_councils_info`
(
    `id`             bigint                                                        NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `square_id`      bigint                                                        NOT NULL COMMENT 'id of Klaytn square',
    `square_link`    varchar(200)                                                  NOT NULL COMMENT 'link of Klaytn square',
    `name`           varchar(200)                                                  NOT NULL COMMENT 'name of Klaytn square',
    `thumbnail`      varchar(200)                                                  NOT NULL COMMENT 'gc icon url',
    `website`        varchar(200)                                                  NOT NULL COMMENT 'gc website',
    `summary`        text,
    `description`    text,
    `apy`            varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `total_staking`  varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    `is_foundation`  tinyint                                                       DEFAULT '0',
    `joined_at`      datetime(6) DEFAULT NULL COMMENT 'gc joined date',
    `activated_at`   datetime(6) DEFAULT NULL COMMENT 'gc activated date',
    `deactivated_at` datetime(6) DEFAULT NULL COMMENT 'gc deactivated date',
    `created_at`     datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created date',
    `updated_at`     datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modified date',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `governance_council_contracts`
(
    `id`           bigint                                                       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `square_id`    bigint                                                       NOT NULL COMMENT 'id of klaytn square',
    `address`      varchar(42) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'contract address',
    `address_type` tinyint                                                      NOT NULL COMMENT 'node(0), staking(1), reward(2)',
    `version`      tinyint DEFAULT NULL COMMENT 'staking version',
    `created_at`   datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created date',
    `updated_at`   datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modified date',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ix_address` (`address`),
    KEY            `ix_squareid` (`square_id`)
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `governance_council_communities`
(
    `id`           bigint       NOT NULL AUTO_INCREMENT,
    `community_id` bigint DEFAULT NULL,
    `name`         varchar(200) NOT NULL,
    `links`        varchar(200) NOT NULL,
    `thumbnail`    varchar(200) NOT NULL,
    `created_at`   datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created date',
    `updated_at`   datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modified date',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `governance_council_categories`
(
    `id`            bigint       NOT NULL AUTO_INCREMENT,
    `category_id`   bigint       NOT NULL,
    `category_name` varchar(200) NOT NULL,
    `square_id`     bigint       NOT NULL,
    `created_at`    datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created date',
    `updated_at`    datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modified date',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;