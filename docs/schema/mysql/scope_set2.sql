-- ---------------------------------------------------------------------------------------------------------------------
-- sharding by block_number (physical:1, logic:10)
-- ---------------------------------------------------------------------------------------------------------------------

CREATE TABLE `internal_transactions`
(
    `id`                bigint      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `internal_tx_id`    varchar(100)         DEFAULT NULL COMMENT 'blocknumber_transactionindex_callid 조합의 unique key',
    `block_number`      bigint      NOT NULL,
    `call_id`           int         NOT NULL,
    `error`             text,
    `from`              varchar(42) NOT NULL,
    `to`                varchar(42)          DEFAULT NULL,
    `gas`               bigint      NOT NULL,
    `gas_used`          bigint               DEFAULT NULL,
    `transaction_index` int         NOT NULL,
    `input`             text        NOT NULL,
    `output`            text,
    `parent_call_id`    int                  DEFAULT NULL,
    `reverted`          text                 DEFAULT NULL,
    `time`              varchar(255)         DEFAULT NULL,
    `type`              varchar(50) NOT NULL,
    `value`             varchar(50)          DEFAULT NULL,
    `created_at`        datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created date',
    `updated_at`        datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modified date',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_internaltxid` (`internal_tx_id`),
    KEY `ix_blocknumber_transactionindex_callid` (`block_number` DESC, `transaction_index` DESC, `call_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------------------------------------------------
-- sharding by account_address (physical:1, logic:10)
-- ---------------------------------------------------------------------------------------------------------------------

CREATE TABLE `internal_transaction_index`
(
    `id`                bigint       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `internal_tx_id`    varchar(100) NOT NULL,
    `account_address`   varchar(42)  NOT NULL,
    `block_number`      bigint       NOT NULL,
    `transaction_index` int          NOT NULL,
    `call_id`           int          NOT NULL,
    `created_at`        datetime(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created date',
    `updated_at`        datetime(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modified date',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_itxid_accountaddress_bn` (`internal_tx_id`, `account_address`, `block_number`),
    KEY `ix_accountaddress_bn_txidx_callid_itxid` (`account_address`, `block_number` DESC,
                                                   `transaction_index` DESC, `call_id` ASC,
                                                   `internal_tx_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

