CREATE TABLE `accounts`
(
    `id`                                bigint      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `address`                           varchar(42) NOT NULL COMMENT 'account address',
    `account_type`                      tinyint     NOT NULL COMMENT 'EOA(0), SCA(1)',
    `balance`                           decimal(36, 18) DEFAULT '0' COMMENT 'account balance',
    `total_transaction_count`           bigint          DEFAULT NULL COMMENT 'account nonce(sent tx count)',
    `contract_type`                     tinyint     NOT NULL COMMENT 'ERC20(0),KIP7(1),KIP17(2),KIP37(3),ERC721(4),ERC1155(5),CONSENSUS_NODE(126),CUSTOM(127)',
    `contract_creator_address`          varchar(42)     DEFAULT NULL COMMENT 'account creator address ',
    `contract_creator_transaction_hash` varchar(66)     DEFAULT NULL COMMENT 'account created transactionHash',
    `contract_deployer_address`         varchar(42)     DEFAULT NULL COMMENT 'account deployer address',
    `kns_domain`                        varchar(255)    DEFAULT NULL COMMENT 'Klaytn Name Service domain',
    `address_label`                     varchar(500)    DEFAULT NULL COMMENT 'address label',
    `tags`                              text            DEFAULT NULL COMMENT 'account tags(json string)',
    `created_at`                        datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created date',
    `updated_at`                        datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modified date',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_address` (`address`),
    UNIQUE KEY `ux_knsdomain` (`kns_domain`),
    KEY                                 `ix_accounttype` (`account_type`),
    KEY                                 `ix_contracttype` (`contract_type`),
    KEY                                 `ix_contractdeployeraddress_createdat_address` (`contract_deployer_address`, `created_at` DESC, `address`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- For kip7, spender_address can approach the amount for contract_address. If amount is 0, revoke
CREATE TABLE `account_token_approves`
(
    `id`               bigint      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `block_number`     bigint      NOT NULL COMMENT 'approved blockNumber',
    `transaction_hash` varchar(66) NOT NULL COMMENT 'approved transactionHash',
    `account_address`  varchar(42) NOT NULL COMMENT 'account address',
    `spender_address`  varchar(42) NOT NULL COMMENT 'spender address',
    `contract_type`    tinyint     NOT NULL COMMENT 'ERC20(0),KIP7(1),KIP17(2)',
    `contract_address` varchar(42) NOT NULL COMMENT 'token contract address',
    `approved_amount`  varchar(66) NOT NULL COMMENT 'approved amount',
    `timestamp`        int         NOT NULL DEFAULT 0 COMMENT '',
    `created_at`       datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created date',
    `updated_at`       datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modified date',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_accountaddress_contractaddress_spenderaddress`
        (`account_address`, `contract_address`, `spender_address`),
    KEY                `ix_accountaddress_contractaddress_blocknumber` (`account_address`, `contract_address`, `block_number` DESC),
    KEY                `ix_accountaddress_spenderaddress_blocknumber` (`account_address`, `spender_address`, `block_number` DESC),
    KEY                `ix_accountaddress_blocknumber` (`account_address`, `block_number` DESC),
    KEY                `ix_spenderaddress_blocknumber` (`spender_address`, `block_number` DESC)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- In the case of kip17, it can be approached to spender by token_id, or can be approached to all token_id by token_all
-- In the case of kip37, it is not a token_id, and only all token_id can be used to set whether to apply or not
CREATE TABLE `account_nft_approves`
(
    `id`                bigint      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `block_number`      bigint      NOT NULL COMMENT 'approved blockNumber',
    `transaction_hash`  varchar(66) NOT NULL COMMENT 'approved transactionHash',
    `account_address`   varchar(42) NOT NULL COMMENT 'account address',
    `spender_address`   varchar(42) NOT NULL COMMENT 'spender address',
    `contract_type`     tinyint     NOT NULL COMMENT 'KIP17(2),KIP37(3),ERC721(4),ERC1155(5)',
    `contract_address`  varchar(42) NOT NULL COMMENT 'nft contract address',
    `approved_all`      tinyint     NOT NULL DEFAULT '0' COMMENT 'If approved_token_id is null, then it is true.',
    `approved_token_id` text NULL     DEFAULT NULL COMMENT 'approved tokenId',
    `timestamp`         int         NOT NULL DEFAULT 0 COMMENT 'approved timestamp',
    `created_at`        datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created date',
    `updated_at`        datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modified date',
    PRIMARY KEY (`id`),
    KEY                 `ix_accountaddress_contracttype_approvedall_blocknumber` (`account_address`, `contract_type`, `approved_all`, `block_number` DESC),
    KEY                 `ix_accountaddress_contractaddress_approvedall_blocknumber` (`account_address`, `contract_address`, `approved_all`, `block_number` DESC),
    KEY                 `ix_accountaddress_spenderaddress_approvedall_blocknumber` (`account_address`, `spender_address`, `approved_all`, `block_number` DESC),
    KEY                 `ix_accountaddress_approvedall_blocknumber` (`account_address`, `approved_all`, `block_number` DESC),
    KEY                 `ix_spenderaddress_approvedall_blocknumber` (`spender_address`, `approved_all`, `block_number` DESC)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `account_transfer_contracts`
(
    `id`               bigint      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `account_address`  varchar(42) NOT NULL COMMENT 'account address',
    `contract_address` varchar(42) NOT NULL COMMENT 'transfer contract',
    `transfer_type`    tinyint     NOT NULL DEFAULT 0 COMMENT 'TOKEN(0),NFT(1)',
    `created_at`       datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created date',
    `updated_at`       datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modified date',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_accountaddress_contractaddress` (`account_address`, `contract_address`),
    KEY                `ix_accountaddress_transfertype_updated_at` (`account_address`, `transfer_type`, `updated_at` DESC),
    KEY                `ix_accountaddress_updatedat` (`account_address`, `updated_at` DESC)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `blocks`
(
    `id`                bigint     NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `block_score`       int          DEFAULT NULL,
    `committee`         text,
    `extra_data`        text,
    `gas_used`          int          DEFAULT NULL,
    `governance_data`   varchar(255) DEFAULT NULL,
    `hash`              varchar(66)  DEFAULT NULL,
    `logs_bloom`        text,
    `number`            bigint       DEFAULT NULL,
    `parent_hash`       varchar(66)  DEFAULT NULL,
    `proposer`          varchar(42)  DEFAULT NULL,
    `receipts_root`     varchar(66)  DEFAULT NULL,
    `reward`            varchar(255) DEFAULT NULL,
    `size`              bigint       DEFAULT NULL,
    `state_root`        varchar(66)  DEFAULT NULL,
    `timestamp`         int          DEFAULT NULL,
    `timestamp_fos`     int          DEFAULT NULL,
    `date`              varchar(6) NOT NULL COMMENT 'created date(yyyyMM)',
    `total_block_score` int          DEFAULT NULL,
    `transaction_count` bigint       DEFAULT NULL,
    `transactions_root` varchar(66)  DEFAULT NULL,
    `vote_data`         varchar(255) DEFAULT NULL,
    `base_fee_per_gas`  varchar(255) DEFAULT NULL COMMENT 'since klaytn 1.8.0',
    `created_at`        datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created date',
    `updated_at`        datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modified date',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ix_hash` (`hash`),
    UNIQUE KEY `ix_number` (`number` DESC),
    KEY                 `ix_timestamp_blocknumber` (`timestamp` DESC, `number` DESC),
    KEY                 `ix_proposer_date_blocknumber` (`proposer`, `date`, `number` DESC),
    KEY                 `ix_proposer_blocknumber_timestamp` (`proposer`, `number` DESC, `timestamp`),
    KEY                 `ix_proposer_date_blocknumber_others` (`proposer`, `date`, `number` DESC, `timestamp`, `transaction_count`,
                                               `gas_used`, `size`),
    KEY                 `ix_proposer_date_blocknumber_others2` (`proposer`, `date`, `number` DESC, `timestamp`, `transaction_count`,
                                                `gas_used`, `size`, `base_fee_per_gas`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `transactions`
(
    `id`                       bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `block_hash`               varchar(66),
    `block_number`             bigint,
    `code_format`              varchar(255) DEFAULT NULL,
    `contract_address`         varchar(42)  DEFAULT NULL,
    `fee_payer`                varchar(42)  DEFAULT NULL,
    `fee_payer_signatures`     text,
    `fee_ratio`                varchar(255) DEFAULT NULL,
    `from`                     varchar(42)  DEFAULT NULL,
    `to`                       varchar(42)  DEFAULT NULL,
    `gas`                      bigint       DEFAULT NULL,
    `gas_price`                varchar(255) DEFAULT NULL,
    `gas_used`                 bigint       DEFAULT NULL,
    `human_readable`           tinyint      DEFAULT NULL,
    `input`                    text,
    `key`                      text,
    `logs_bloom`               text,
    `nft_transfer_count`       int          DEFAULT NULL,
    `nonce`                    bigint       DEFAULT NULL,
    `sender_tx_hash`           varchar(66)  DEFAULT NULL,
    `signatures`               text,
    `status`                   tinyint      DEFAULT NULL,
    `timestamp`                int          DEFAULT NULL,
    `token_transfer_count`     int          DEFAULT NULL,
    `transaction_hash`         varchar(66)  DEFAULT NULL,
    `transaction_index`        int          DEFAULT NULL,
    `tx_error`                 int          DEFAULT NULL,
    `type`                     varchar(255) DEFAULT NULL,
    `type_int`                 int          DEFAULT NULL,
    `value`                    varchar(255) DEFAULT NULL,
    `access_list`              text         DEFAULT NULL COMMENT 'since klaytn 1.8.0',
    `chain_id`                 varchar(255) DEFAULT NULL COMMENT 'since klaytn 1.8.0',
    `max_fee_per_gas`          varchar(255) DEFAULT NULL COMMENT 'since klaytn 1.8.0',
    `max_priority_fee_per_gas` varchar(255) DEFAULT NULL COMMENT 'since klaytn 1.8.0',
    `effective_gas_price`      varchar(255) DEFAULT NULL COMMENT 'since klaytn 1.9.0',
    `created_at`               datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created date',
    `updated_at`               datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modified date',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_transactionhash` (`transaction_hash`),
    KEY                        `ix_blocknumber_type_transactionindex` (`block_number` DESC, `type`, `transaction_index` DESC,
                                                `transaction_hash`),
    KEY                        `ix_blocknumber_transactionindex` (`block_number` DESC, `transaction_index` DESC, `transaction_hash`),
    KEY                        `ix_type_blocknumber_transactionindex` (`type`, `block_number` DESC, `transaction_index` DESC,
                                                `transaction_hash`),
    KEY                        `ix_from_type_blocknumber_transactionindex` (`from`, `type`, `block_number` DESC, `transaction_index` DESC,
                                                     `transaction_hash`),
    KEY                        `ix_from_blocknumber_transactionindex` (`from`, `block_number` DESC, `transaction_index` DESC,
                                                `transaction_hash`),
    KEY                        `ix_to_type_blocknumber_transactionindex` (`to`, `type`, `block_number` DESC, `transaction_index` DESC,
                                                   `transaction_hash`),
    KEY                        `ix_to_blocknumber_transactionindex` (`to`, `block_number` DESC, `transaction_index` DESC, `transaction_hash`),
    KEY                        `ix_feepayer_type_blocknumber_transactionindex` (`fee_payer`, `type`, `block_number` DESC, `transaction_index`
                                                         DESC, `transaction_hash`),
    KEY                        `ix_feepayer_blocknumber_transactionindex` (`fee_payer`, `block_number` DESC, `transaction_index` DESC,
                                                    `transaction_hash`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `contracts`
(
    `id`                     bigint       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `contract_address`       varchar(42)           DEFAULT NULL,
    `contract_type`          tinyint      NOT NULL COMMENT 'ERC20(0),KIP7(1),KIP17(2),KIP37(3),ERC721(4),ERC1155(5),CONSENSUS_NODE(126),CUSTOM(127)',
    `name`                   varchar(700)          DEFAULT NULL,
    `symbol`                 varchar(50)           DEFAULT NULL,
    `icon`                   text,
    `official_site`          varchar(200)          DEFAULT NULL,
    `official_email_address` varchar(200)          DEFAULT NULL,
    `decimal`                int          NOT NULL DEFAULT '0',
    `holder_count`           bigint       NOT NULL DEFAULT '0',
    `total_supply`           varchar(512) NOT NULL DEFAULT '0',
    `total_transfer`         bigint       NOT NULL DEFAULT '0',
    `verified`               tinyint      NOT NULL DEFAULT '0',
    `tx_error`               tinyint      NOT NULL DEFAULT '0',
    `total_supply_order`     varchar(130) NOT NULL DEFAULT '0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000',
    `implementation_address` varchar(42)           DEFAULT NULL COMMENT 'if contract is proxy, implementation address exists.',
    `burn_amount`            varchar(67)           DEFAULT NULL COMMENT 'token burn amount',
    `total_burn`             bigint       NOT NULL DEFAULT '0' COMMENT 'token total burn',
    `created_at`             datetime(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created date',
    `updated_at`             datetime(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modified date',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_contractaddress` (`contract_address`),
    KEY                      `ix_contractaddress_updatedat` (`contract_address`, `updated_at` DESC),
    KEY                      `ix_contracttype_name_updatedat` (`contract_type`, `name`, `updated_at` DESC),
    KEY                      `ix_contracttype_symbol_updatedad` (`contract_type`, `symbol`, `updated_at` DESC),
    KEY                      `ix_contracttype_updatedat` (`contract_type`, `updated_at` DESC),
    KEY                      `ix_contracttype_verified_updatedat` (`contract_type`, `verified`, `updated_at` DESC),
    KEY                      `ix_implementationaddress` (`implementation_address`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `contract_codes`
(
    `id`                       bigint  NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `abi_encoded_value`        text COMMENT 'contract encoded abi',
    `compiler_type`            varchar(50)      DEFAULT NULL COMMENT 'solidity',
    `compiler_version`         varchar(50)      DEFAULT NULL COMMENT 'solidity version',
    `optimization_flag`        tinyint NOT NULL COMMENT 'compiler option',
    `optimization_runs_count`  bigint  NOT NULL DEFAULT '0' COMMENT 'compiler option',
    `optimization_evm_version` varchar(50)      DEFAULT NULL COMMENT 'compiler option',
    `license_type`             varchar(100)     DEFAULT NULL COMMENT 'license type',
    `contract_abi`             longtext COMMENT 'contract abi',
    `contract_address`         varchar(42)      DEFAULT NULL COMMENT 'contract address',
    `contract_creation_code`   text COMMENT 'contract creation code(binary)',
    `contract_name`            varchar(50)      DEFAULT NULL COMMENT 'contract name',
    `contract_source_code`     longtext COMMENT 'contract source(solidity)',
    `created_at`               datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created date',
    `updated_at`               datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modified date',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ix_contractaddress` (`contract_address`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `contract_submission_requests`
(
    `id`                         bigint       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `contract_address`           varchar(42)  NOT NULL COMMENT 'contract address',
    `contract_creator_signature` text         NOT NULL COMMENT 'contract owner signature',
    `contract_source_code`       longtext     NOT NULL COMMENT 'contract source',
    `constructor_arguments`      text         DEFAULT NULL COMMENT 'contract encoded abi',
    `compiler_version`           varchar(50)  NOT NULL COMMENT 'solidity',
    `license_type`               varchar(100) NOT NULL COMMENT 'license type',
    `optimization`               tinyint      NOT NULL COMMENT 'compiler option',
    `optimization_runs`          bigint       DEFAULT NULL COMMENT 'compiler option',
    `evm_version`                varchar(50)  DEFAULT NULL COMMENT 'compiler option',
    `token_name`                 varchar(100) DEFAULT NULL COMMENT 'token name',
    `token_symbol`               varchar(50)  DEFAULT NULL COMMENT 'token symbol',
    `token_icon`                 text         DEFAULT NULL COMMENT 'token icon',
    `official_web_site`          varchar(200) DEFAULT NULL COMMENT 'official webSite',
    `official_email_address`     varchar(200) DEFAULT NULL COMMENT 'official email',
    `contract_creation_code`     text         NOT NULL COMMENT 'contract creation code',
    `result`                     tinyint      NOT NULL COMMENT 'submission result. SUCCESS(1), FAIL(0)',
    `result_message`             text         DEFAULT NULL COMMENT 'submission fail message',
    `created_at`                 datetime(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created date',
    `updated_at`                 datetime(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modified date',
    `wallet_type`                tinyint      DEFAULT '0' COMMENT 'KAIKAS(0), METAMASK(1), KLIP(2), KAS(3)',
    `libraries`                  text NULL     DEFAULT NULL COMMENT 'compiler option',
    PRIMARY KEY (`id`),
    KEY                          `ix_contractaddress` (`contract_address`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `gas_prices`
(
    `id`               bigint       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `min_block_number` bigint       NOT NULL COMMENT 'apply range(start)',
    `max_block_number` bigint       NOT NULL COMMENT 'apply range(end)',
    `gas_price`        varchar(255) NOT NULL COMMENT 'gas price',
    `created_at`       datetime(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created date',
    `updated_at`       datetime(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modified date',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `account_tags`
(
    `id`         bigint      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `tag`        varchar(50) NOT NULL COMMENT 'managed tag',
    `tag_order`  tinyint     NOT NULL COMMENT 'tag order',
    `display`    tinyint     NOT NULL default 0 COMMENT 'tag display.',
    `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created date',
    `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modified date',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_tag` (`tag`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Table used by spark jobs
CREATE TABLE `klaytn_name_service`
(
    `id`               bigint       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `name`             varchar(512) NOT NULL COMMENT 'kns',
    `resolved_address` varchar(42)  NOT NULL COMMENT 'kns resolved address',
    `resolver_address` varchar(42)  NOT NULL COMMENT 'kns resolver address',
    `name_hash`        varchar(66)  NOT NULL COMMENT 'kns nameHash',
    `token_id`         varchar(256) NOT NULL COMMENT 'tokenId',
    `created_at`       datetime(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created date',
    `updated_at`       datetime(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modified date',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_name` (`name`),
    KEY                `ix_namehash` (`name_hash`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `users`
(
    `id`         bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_name`  varchar(100) DEFAULT NULL,
    `user_type`  int    NOT NULL COMMENT 'NONE(0),ADMIN(255)',
    `access_key` varchar(40)  DEFAULT NULL,
    `secret_key` varchar(40)  DEFAULT NULL,
    `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created date',
    `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modified date',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_user_name` (`user_name`),
    UNIQUE KEY `ux_access_key` (`access_key`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `function_signatures`
(
    `id`              bigint      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `4byte_id`        bigint  DEFAULT NULL COMMENT 'id of 4byte',
    `bytes_signature` varchar(10) NOT NULL COMMENT 'signature bytes',
    `text_signature`  text NULL COMMENT 'signature',
    `primary`         tinyint DEFAULT NULL COMMENT 'apply priority',
    `created_at`      datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created date',
    `updated_at`      datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modified date',
    PRIMARY KEY (`id`),
    UNIQUE `ux_4byteid` (`4byte_id`),
    KEY               `ix_bytessignature` (`bytes_signature`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `event_signatures`
(
    `id`             bigint      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `4byte_id`       bigint  DEFAULT NULL COMMENT 'id of 4byte',
    `hex_signature`  varchar(66) NOT NULL COMMENT 'signature hex',
    `text_signature` text        NOT NULL COMMENT 'signature',
    `primary`        tinyint DEFAULT NULL COMMENT 'apply priority',
    `created_at`     datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created date',
    `updated_at`     datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modified date',
    PRIMARY KEY (`id`),
    UNIQUE `ux_4byteid` (`4byte_id`),
    KEY              `ix_hexsignature` (`hex_signature`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `block_burns`
(
    `id`              bigint      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `number`          bigint               DEFAULT NULL COMMENT 'burnt blockNumber',
    `fees`            varchar(66) NOT NULL COMMENT 'burnt fee',
    `accumulate_fees` varchar(66) NOT NULL COMMENT 'burnt accumulated fees',
    `klay`            varchar(66)          DEFAULT NULL COMMENT 'burnt klay',
    `accumulate_klay` varchar(66) NOT NULL DEFAULT '0x00000000000000000000000000000000000000000004bd8585ed3483bfa46000',
    `timestamp`       int         NOT NULL COMMENT 'burnt timestamp',
    `created_at`      datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created date',
    `updated_at`      datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modified date',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ix_number` (`number` DESC)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `block_rewards`
(
    `id`         bigint      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `number`     bigint      NOT NULL,
    `minted`     varchar(66) NOT NULL COMMENT 'The amount minted',
    `total_fee`  varchar(66) NOT NULL COMMENT 'Total tx fee spent',
    `burnt_fee`  varchar(66) NOT NULL COMMENT 'The amount burnt',
    `proposer`   varchar(66) NOT NULL COMMENT 'The amount for the block proposer',
    `stakers`    varchar(66) NOT NULL COMMENT 'Total amount for stakers',
    `kgf`        varchar(66) NOT NULL COMMENT 'The amount for KGF',
    `kir`        varchar(66) NOT NULL COMMENT 'The amount for KIR',
    `rewards`    text        NOT NULL COMMENT 'A mapping from reward recipient addresses to reward amounts (json)',
    `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created date',
    `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modified date',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ix_number` (`number` DESC)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `governance_councils`
(
    `id`             bigint       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `square_id`      bigint       NOT NULL COMMENT 'id of klaytn square',
    `square_link`    varchar(200) NOT NULL COMMENT 'link of Klaytn square',
    `name`           varchar(200) NOT NULL COMMENT 'name of klaytn square',
    `thumbnail`      varchar(200) NOT NULL COMMENT 'gc icon url',
    `website`        varchar(200) NOT NULL COMMENT 'gc website',
    `joined_at`      datetime(6)           DEFAULT NULL COMMENT 'gc joined date',
    `activated_at`   datetime(6)           DEFAULT NULL COMMENT 'gc activated date',
    `deactivated_at` datetime(6)           DEFAULT NULL COMMENT 'gc deactivated date',
    `created_at`     datetime(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created date',
    `updated_at`     datetime(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modified date',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ix_squareid` (`square_id`),
    KEY              `ix_name` (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `governance_council_contracts`
(
    `id`           bigint      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `square_id`    bigint      NOT NULL COMMENT 'id of klaytn square',
    `address`      varchar(42) NOT NULL COMMENT 'contract address',
    `address_type` tinyint     NOT NULL COMMENT 'node(0), staking(1), reward(2)',
    `created_at`   datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created date',
    `updated_at`   datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modified date',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ix_address` (`address`),
    KEY            `ix_squareid` (`square_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `account_keys`
(
    `id`               bigint      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `block_number`     bigint      NOT NULL,
    `transaction_hash` varchar(66) DEFAULT NULL,
    `account_address`  varchar(42) NOT NULL,
    `account_key`      text        DEFAULT NULL COMMENT 'account key, json string',
    `created_at`       datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created date',
    `updated_at`       datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modified date',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_transactionhash_accountaddress_blocknumber` (`transaction_hash`, `account_address`, `block_number`),
    KEY                `ix_accountaddress_blocknumber` (`account_address`, `block_number` DESC)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `kaia_users`
(
    `id`                 bigint                                                        NOT NULL AUTO_INCREMENT COMMENT 'user_id',
    `name`               varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'name',
    `email`              varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'email',
    `password`           varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'password',
    `profile_image`      varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci          DEFAULT NULL COMMENT 'profile_image url',
    `is_subscribed`      tinyint                                                       NOT NULL DEFAULT '0' COMMENT '0: disagree, 1: agree',
    `status`             tinyint                                                       NOT NULL DEFAULT '0' COMMENT '0: unverified, 1: active, 9: deactivated',
    `register_timestamp` int                                                                    DEFAULT NULL COMMENT 'sign up date',
    `deleted_at`         datetime(6) DEFAULT NULL COMMENT 'deactivation date',
    `created_at`         datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created date',
    `updated_at`         datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modified date',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_username` (`name`) USING BTREE,
    UNIQUE KEY `ux_email` (`email`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

CREATE TABLE `kaia_user_login_history`
(
    `id`         bigint NOT NULL AUTO_INCREMENT,
    `user_id`    bigint NOT NULL,
    `timestamp`  int    NOT NULL,
    `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created date',
    `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modified date',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_userid_timestamp` (`user_id`,`timestamp`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
