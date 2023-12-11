CREATE TABLE `event_logs`
(
    `id`                bigint      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `address`           varchar(42)          DEFAULT NULL,
    `block_hash`        varchar(66)          DEFAULT NULL,
    `block_number`      bigint               DEFAULT NULL,
    `signature`         varchar(255)         DEFAULT NULL,
    `data`              text,
    `log_index`         int                  DEFAULT NULL,
    `topics`            text,
    `transaction_hash`  varchar(66)          DEFAULT NULL,
    `transaction_index` int                  DEFAULT NULL,
    `removed`           tinyint              DEFAULT NULL COMMENT 'since klaytn 1.8.0',
    `created_at`        datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created date',
    `updated_at`        datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modified date',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ix_transactionhash_logindex` (`transaction_hash`,`log_index`) USING BTREE,
    KEY `ix_transactionhash_signature_logindex` (`transaction_hash`, `signature`, `log_index` DESC), -- new index
    KEY `ix_signature` (`signature`),
    KEY `ix_address_blocknumber_transactionindex_logindex`
        (`address`, `block_number` DESC, `transaction_index` DESC, `log_index` DESC),
    KEY `ix_address_signature_blocknumber_transactionindex_logindex`
        (`address`, `signature`, `block_number` DESC, `transaction_index` DESC, `log_index` DESC)    -- new index
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `token_holders`
(
    `id`                    bigint      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `contract_address`      varchar(42) NOT NULL,
    `holder_address`        varchar(42) NOT NULL,
    `amount`                varchar(67) NOT NULL,
    `last_transaction_time` int         NOT NULL,
    `created_at`            datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created date',
    `updated_at`            datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modified date',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_contractaddress_holderaddress` (`contract_address`, `holder_address`),
    KEY `ix_contractaddress_amount` (`contract_address`, `amount` DESC),
    KEY `ix_holderaddress_contractaddress` (`holder_address`, `contract_address`),
    KEY `ix_holderaddress_lasttransactiontime` (`holder_address`, `last_transaction_time` DESC)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `token_transfers`
(
    `id`               bigint      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `contract_address` varchar(42) NOT NULL,
    `from`             varchar(42) NOT NULL,
    `to`               varchar(42) NOT NULL,
    `amount`           varchar(66) NOT NULL,
    `timestamp`        int         NOT NULL,
    `block_number`     bigint      NOT NULL,
    `transaction_hash` varchar(66) NOT NULL,
    `display_order`    varchar(22) NOT NULL,
    `created_at`       datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created date',
    `updated_at`       datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modified date',
    PRIMARY KEY (`id`),
    KEY `ix_blocknumber` (`block_number` DESC),
    KEY `ix_contractaddress_blocknumber_displayorder` (`contract_address`, `block_number` DESC, `display_order` DESC),
    KEY `ix_from_blocknumber_displayorder` (`from`, `block_number` DESC, `display_order` DESC),
    KEY `ix_from_contractaddress_blocknumber_displayorder` (`from`, `contract_address`, `block_number` DESC, `display_order` DESC),
    KEY `ix_to_blocknumber_displayorder` (`to`, `block_number` DESC, `display_order` DESC),
    KEY `ix_to_contractaddress_blocknumber_displayorder` (`to`, `contract_address`, `block_number` DESC, `display_order` DESC),
    UNIQUE KEY `ix_transactionhash_displayorder` (`transaction_hash`,`display_order`) USING BTREE,
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `token_burns`
(
    `id`               bigint      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `contract_address` varchar(42) NOT NULL,
    `from`             varchar(42) NOT NULL,
    `to`               varchar(42) NOT NULL,
    `amount`           varchar(66) NOT NULL,
    `timestamp`        int         NOT NULL,
    `block_number`     bigint      NOT NULL,
    `transaction_hash` varchar(66) NOT NULL,
    `display_order`    varchar(22) NOT NULL,
    `created_at`       datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created date',
    `updated_at`       datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modified date',
    PRIMARY KEY (`id`),
    KEY `ix_contractaddress_displayorder` (`contract_address`, `display_order` DESC),
    KEY `ix_contractaddress_from_displayorder` (`contract_address`, `from`, `display_order` DESC),
    KEY `ix_contractaddress_to_displayorder` (`contract_address`, `to`, `display_order` DESC),
    UNIQUE KEY `ix_contractaddress_transactionhash_displayorder` (`contract_address`,`transaction_hash`,`display_order`) USING BTREE,
    KEY `ix_contractaddress_blocknumber_displayorder` (`contract_address`, `block_number` DESC, `display_order` DESC)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `nft_holders`
(
    `id`                    bigint      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `contract_address`      varchar(42) NOT NULL,
    `holder_address`        varchar(42) NOT NULL,
    `token_count`           varchar(67) NOT NULL,
    `last_transaction_time` int         NOT NULL,
    `created_at`            datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created date',
    `updated_at`            datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modified date',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_contractaddress_holderaddress` (`contract_address`, `holder_address`),
    KEY `ix_holderaddress_lasttransactiontime` (`holder_address`, `last_transaction_time` DESC), -- account/kip17_balances
    KEY `ix_holderaddress_tokencount` (`holder_address`, `token_count` DESC),                    -- account/kip17_balances
    KEY `ix_contractaddress_tokencount` (`contract_address`, `token_count` DESC),                 -- nft/holders (rank)
    KEY `ix_token_count` (`token_count` DESC)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `nft_inventories`
(
    `id`                    bigint       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `contract_type`         tinyint      NOT NULL,
    `contract_address`      varchar(42)  NOT NULL,
    `holder_address`        varchar(42)  NOT NULL,
    `token_id`              varchar(255) NOT NULL,
    `token_uri`             varchar(255) NOT NULL,
    `token_count`           varchar(67)  NOT NULL DEFAULT '0x0000000000000000000000000000000000000000000000000000000000000001',
    `last_transaction_time` int          NOT NULL,
    `created_at`            datetime(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created date',
    `updated_at`            datetime(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modified date',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_contractaddress_holderaddress_tokenid` (`contract_address`, `holder_address`, `token_id`),
    KEY `ix_holderaddress_lasttransactiontime` (`holder_address`, `last_transaction_time` DESC),
    KEY `ix_holderaddress_tokenuri_lasttransactiontime` (`holder_address`, `token_uri`, `last_transaction_time` DESC),
    KEY `ix_contracttype_holderaddress_lasttransactiontime` (`contract_type`, `holder_address`, `last_transaction_time` DESC),
    KEY `ix_contracttype_holderaddress_tokencount` (`contract_type`, `holder_address`, `token_count` DESC),
    KEY `ix_contractaddress_lasttransactiontime` (`contract_address`, `last_transaction_time` DESC),
    KEY `ix_contractaddress_tokenid_lasttransactiontime` (`contract_address`, `token_id`, `last_transaction_time` DESC),
    KEY `ix_contractaddress_holderaddress_lasttransactiontime` (`contract_address`, `holder_address`, `last_transaction_time` DESC),
    KEY `ix_contractaddress_tokencount` (`contract_address`, `token_count` DESC),
    KEY `ix_contractaddress_tokenid_tokencount` (`contract_address`, `token_id`, `token_count` DESC),
    KEY `ix_contractaddress_holderaddress_tokencount` (`contract_address`, `holder_address`, `token_count` DESC),
    KEY `ix_token_count` (`token_count` DESC)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `nft_transfers`
(
    `id`               bigint       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `contract_type`    tinyint      NOT NULL,
    `contract_address` varchar(42)  NOT NULL,
    `from`             varchar(42)  NOT NULL,
    `to`               varchar(42)  NOT NULL,
    `token_count`      varchar(67)  NOT NULL,
    `token_id`         varchar(255) NOT NULL,
    `timestamp`        int          NOT NULL,
    `block_number`     bigint       NOT NULL,
    `transaction_hash` varchar(66)  NOT NULL,
    `display_order`    varchar(22)           DEFAULT NULL,
    `created_at`       datetime(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created date',
    `updated_at`       datetime(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modified date',
    PRIMARY KEY (`id`),
    KEY `ix_blocknumber` (`block_number` DESC),
    KEY `ix_contractaddress_blocknumber_displayorder` (`contract_address`, `block_number` DESC, `display_order` DESC),
    KEY `ix_contractaddress_tokenid_displayorder` (`contract_address`, `token_id`, `display_order` DESC),
    KEY `ix_from_blocknumber_displayorder` (`from`, `block_number` DESC, `display_order` DESC),
    KEY `ix_from_contractaddress_blocknumber_displayorder` (`from`, `contract_address`, `block_number` DESC, `display_order` DESC),
    KEY `ix_to_blocknumber_displayorder` (`to`, `block_number` DESC, `display_order` DESC),
    KEY `ix_to_contractaddress_blocknumber_displayorder` (`to`, `contract_address`, `block_number` DESC, `display_order` DESC),
    UNIQUE KEY `ix_transactionhash_displayorder` (`transaction_hash`,`display_order`) USING BTREE,
    KEY `ix_to_contracttype_blocknumber_displayorder` (`to`, `contract_type`, `block_number`, `display_order`),
    KEY `ix_from_contracttype_blocknumber_displayorder` (`from`, `contract_type`, `block_number`, `display_order`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Table used by spark jobs
CREATE TABLE `nft_patterned_uri`
(
    `id`               bigint        NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `contract_address` varchar(42)   NOT NULL,
    `token_uri`        varchar(1024) NOT NULL,
    `created_at`       datetime(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created date',
    `updated_at`       datetime(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modified date',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_contractaddress` (`contract_address`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Details by nft token_id (token_id by contract_address is unique)
-- For kip17, token_uri information
-- For KIP-37, the information includes token_uri, total_supply, total_transfers, burn_amount, and total_burn.
CREATE TABLE `nft_items`
(
    `id`                   bigint       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `contract_type`        tinyint      NOT NULL COMMENT 'ERC20(0),KIP7(1),KIP17(2),KIP37(3),ERC721(4),ERC1155(5),CONSENSUS_NODE(126),CUSTOM(127)',
    `contract_address`     varchar(42)  NOT NULL,
    `token_id`             varchar(512) NOT NULL,
    `token_uri`            mediumtext   NOT NULL,
    `token_uri_exists`     tinyint      NOT NULL default '1' COMMENT 'EXIST(1), NONE(0)',
    `token_uri_updated_at` datetime(6)  NULL     DEFAULT NULL COMMENT 'last updated date of tokenUri',
    `total_supply`         varchar(67)           DEFAULT NULL,
    `total_transfer`       bigint       NOT NULL DEFAULT '0',
    `burn_amount`          varchar(67)           DEFAULT NULL,
    `total_burn`           bigint       NOT NULL DEFAULT '0',
    `created_at`           datetime(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created date',
    `updated_at`           datetime(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modified date',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_contractaddress_tokenid` (`contract_address`, `token_id`),
    INDEX `ix_contractaddress_tokenuriexists` (`contract_address`, `token_uri_exists`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- NFT burn information
-- In the case of KIP-17, the token_count for each token_id is 1
-- In the case of KIP-17, the token_count for each token_id can be 1 or more.
CREATE TABLE `nft_burns`
(
    `id`               bigint       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `contract_type`    tinyint      NOT NULL,
    `contract_address` varchar(42)  NOT NULL,
    `block_number`     bigint       NOT NULL,
    `transaction_hash` varchar(66)  NOT NULL,
    `from`             varchar(42)  NOT NULL,
    `to`               varchar(42)  NOT NULL,
    `token_id`         varchar(255) NOT NULL,
    `token_count`      varchar(67)  NOT NULL,
    `timestamp`        int          NOT NULL,
    `display_order`    varchar(22)           DEFAULT NULL,
    `created_at`       datetime(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created date',
    `updated_at`       datetime(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modified date',
    PRIMARY KEY (`id`),
    KEY `ix_contractaddress_displayorder` (`contract_address`, `display_order` DESC),
    KEY `ix_contractaddress_from_displayorder` (`contract_address`, `from`, `display_order` DESC),
    KEY `ix_contractaddress_to_displayorder` (`contract_address`, `to`, `display_order` DESC),
    KEY `ix_contractaddress_tokenid_displayorder` (`contract_address`, `token_id`, `display_order` DESC),
    KEY `ix_contractaddress_tokenid_from_displayorder` (`contract_address`, `token_id`, `from`, `display_order` DESC),
    KEY `ix_contractaddress_tokenid_to_displayorder` (`contract_address`, `token_id`, `to`, `display_order` DESC),
    UNIQUE KEY `ix_contractaddress_transactionhash_displayorder` (`contract_address`,`transaction_hash`,`display_order`) USING BTREE,
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- NFT holder, processed inventory information
-- last_id refers to the id in the nft_transfers table (the last processed transfer id)
-- is_running refers to whether the particular job is currently running
-- exe_rows refers to the number of NFT transfers rows to be processed at once
-- status refers to the operational state of procedures like proc_keep_calling_inven, proc_keep_calling_holder, etc. (0: stopped, 1: running)
-- If you want to execute, you can call proc_keep_calling_inven, proc_keep_calling_holder -> the status will change automatically
-- is_running, status cannot be changed
-- exe_rows, last_id (recommended for use in migration only) can be set.
CREATE TABLE `nft_aggregate_flag` (
  `id` int NOT NULL AUTO_INCREMENT,
  `job_name` varchar(42) DEFAULT NULL,
  `last_id` int DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_running` tinyint(1) DEFAULT '0',
  `exe_rows` bigint DEFAULT '0',
  `status` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_job_name` (`job_name`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


/**
  * Convert Decimal to Hex 
  * @param val: DECIMAL
  * @return hex_str: VARCHAR(64)
  */
CREATE FUNCTION `fn_convert_decimal_to_hex`(val DECIMAL) RETURNS varchar(66) CHARSET utf8mb4
BEGIN
  DECLARE hex_str VARCHAR(64);
  SET hex_str = LPAD(HEX(val), 64, '0');
  RETURN CONCAT('0x', hex_str);
END;

/**
  * Convert Hex to Decimal
  * @param val: VARCHAR(66)
  * @return DECIMAL(38,0)
  */
CREATE FUNCTION `fn_convert_hex_to_decimal`(val VARCHAR(67)) RETURNS decimal(38,0)
BEGIN
  return CAST(CONV(SUBSTR(val,3), 16, 10) AS SIGNED);
END;

/**
  * Aggregate nft_transfer to reflect in nft_holders table
  * @description Save the last id of the currently processed nft_transfers table to the nft_aggregate_flag table
  * @param v_rows_insert_count: Number of rows of nft_transfers table to process
  */
CREATE PROCEDURE `proc_nft_holder_aggregate`(IN v_rows_insert_count INT)
BEGIN
  DECLARE v_last_id bigint;
  DECLARE v_main_lasted_id bigint;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        CALL proc_save_error_log(CONCAT('HolderService:NFT:Holder:LastBlock', 'Error: ', SUBSTRING_INDEX(STRERROR(ERROR_CODE()), ':', 1)));
        ROLLBACK;  -- rollback any changes made in the transaction
    END;


  UPDATE nft_aggregate_flag SET is_running = true WHERE job_name = 'HolderService:NFT:Holder:LastBlock';

  SELECT last_id INTO v_last_id FROM nft_aggregate_flag WHERE job_name = 'HolderService:NFT:Holder:LastBlock' LIMIT 1;
  SELECT id INTO v_main_lasted_id FROM nft_transfers ORDER BY id DESC LIMIT 1;

  IF v_rows_insert_count > 1 THEN


	  IF v_last_id >= v_main_lasted_id THEN
	 	 SET v_rows_insert_count = -1;
	  ELSE
	  	 IF v_main_lasted_id - v_last_id > v_rows_insert_count THEN
	  	 	SET v_rows_insert_count = v_rows_insert_count;
		 ELSE
	     	SET v_rows_insert_count = v_main_lasted_id - v_last_id ;
		 END IF;
	  END IF;

	  IF v_rows_insert_count > 0 THEN

	  START TRANSACTION;
		  DO SLEEP(0.5);
		  INSERT INTO nft_holders (contract_address, holder_address, token_count, last_transaction_time)
		    SELECT T1.contract_address, T1.holder AS holder_address, fn_convert_decimal_to_hex(sum(T1.count)) token_count, max(T1.last_transaction_time) last_transaction_time FROM (
		      SELECT `contract_address`, `from` as holder, sum(fn_convert_hex_to_decimal(token_count))*-1 as count, max(`timestamp`) as last_transaction_time FROM nft_transfers WHERE id BETWEEN v_last_id + 1 AND v_last_id + v_rows_insert_count GROUP BY `contract_address`, `from`
		      UNION ALL
		      SELECT `contract_address`, `to` as holder, sum(fn_convert_hex_to_decimal(token_count)) as count, max(`timestamp`) as last_transaction_time  FROM nft_transfers WHERE id BETWEEN v_last_id + 1 AND v_last_id + v_rows_insert_count GROUP BY `contract_address`, `to`
		    ) T1
		    GROUP BY contract_address, holder_address HAVING holder_address not in ('0x0000000000000000000000000000000000000000', '0x000000000000000000000000000000000000dead')
		    ON DUPLICATE KEY UPDATE token_count = fn_convert_decimal_to_hex(fn_convert_hex_to_decimal(token_count) + fn_convert_hex_to_decimal(VALUES(token_count))), last_transaction_time = VALUES(last_transaction_time);

		  DELETE FROM nft_holders where token_count = '0x0000000000000000000000000000000000000000000000000000000000000000';

		  UPDATE nft_aggregate_flag SET last_id = (last_id + v_rows_insert_count) WHERE job_name = 'HolderService:NFT:Holder:LastBlock';

	  COMMIT;

	  END IF;

	END IF;


	UPDATE nft_aggregate_flag SET is_running = false WHERE job_name = 'HolderService:NFT:Holder:LastBlock';

END;

/**
  * Aggregate nft_transfer to reflect in the nft_inventories table.
  * @description Save the last ID of the currently processed nft_transfers table to the nft_aggregate_flag table.
  * @param v_rows_insert_count: Number of rows to process in the nft_transfers table.
  * @return
  */
CREATE PROCEDURE `proc_nft_inven_aggregate`(IN v_rows_insert_count INT)
BEGIN

  DECLARE v_last_id bigint;

  DECLARE v_main_lasted_id bigint;

  DECLARE EXIT HANDLER FOR SQLEXCEPTION

  BEGIN
     CALL proc_save_error_log('HolderService:NFT:Inventory:LastBlock', CONCAT('Error: ', SUBSTRING_INDEX(STRERROR(ERROR_CODE()), ':', 1)));
     ROLLBACK;  -- rollback any changes made in the transaction
  END;

  UPDATE nft_aggregate_flag SET is_running = true WHERE job_name = 'HolderService:NFT:Inventory:LastBlock';

  SELECT last_id INTO v_last_id FROM nft_aggregate_flag WHERE job_name = 'HolderService:NFT:Inventory:LastBlock' LIMIT 1;

  SELECT id INTO v_main_lasted_id FROM nft_transfers ORDER BY id DESC LIMIT 1;

  IF v_rows_insert_count > 1 THEN

	  IF v_last_id >= v_main_lasted_id THEN

	 	 SET v_rows_insert_count = -1;

	  ELSE

	  	 IF v_main_lasted_id - v_last_id > v_rows_insert_count THEN

	  	 	SET v_rows_insert_count = v_rows_insert_count;

		 ELSE

	     	SET v_rows_insert_count = v_main_lasted_id - v_last_id ;

		 END IF;

	  END IF;


	  IF v_rows_insert_count > 0 THEN

	  --## Update the Inventories Table
	  START TRANSACTION;
		  DO SLEEP(0.5);
		  INSERT INTO nft_inventories (contract_type, contract_address, token_id, holder_address, token_count, token_uri, last_transaction_time)

		  SELECT
		    T1.contract_type, T1.contract_address, T1.token_id, T1.holder AS holder_address, fn_convert_decimal_to_hex(sum(T1.count)) token_count, '-', max(T1.last_transaction_time) last_transaction_time

		  FROM (


			      SELECT
			      	`contract_type`, `contract_address`, `token_id`, `from` as holder, sum(fn_convert_hex_to_decimal(token_count))*-1 as count, max(`timestamp`) as last_transaction_time

			      FROM nft_transfers

			      WHERE id BETWEEN v_last_id + 1 AND v_last_id + v_rows_insert_count

			      GROUP BY `contract_address`, `token_id`, `from`

			      UNION ALL

			      SELECT
			      	`contract_type`, `contract_address`, `token_id`, `to` as holder, sum(fn_convert_hex_to_decimal(token_count)) as count, max(`timestamp`) as last_transaction_time

			      FROM nft_transfers

			      WHERE id BETWEEN v_last_id + 1 AND v_last_id + v_rows_insert_count

			      GROUP BY `contract_address`, `token_id`, `to`

			    ) T1

			  GROUP BY T1.contract_address, T1.token_id, holder_address

			  HAVING holder_address not in ('0x0000000000000000000000000000000000000000', '0x000000000000000000000000000000000000dead')

			  	AND token_count not in ('0x0000000000000000000000000000000000000000000000000000000000000000')

		  ON DUPLICATE KEY UPDATE
		  	token_count = fn_convert_decimal_to_hex(fn_convert_hex_to_decimal(token_count) + fn_convert_hex_to_decimal(VALUES(token_count))),
		  	last_transaction_time = VALUES(last_transaction_time);

		-- #Use after annotation when migrating from an existing table, Uncomment after migration - when token_count value starts with 0
		  DELETE FROM nft_inventories where token_count = '0x0000000000000000000000000000000000000000000000000000000000000000';

		  UPDATE nft_aggregate_flag SET last_id = (last_id + v_rows_insert_count) WHERE job_name = 'HolderService:NFT:Inventory:LastBlock';

		  COMMIT;

	  END IF;

	END IF;

    UPDATE nft_aggregate_flag SET is_running = false WHERE job_name = 'HolderService:NFT:Inventory:LastBlock';

END;

/**
  * Reflect the token_uri from the existing inventories table to the new inventories table.
  * @param v_rows_insert_count: Number of rows to process in the inventories table.
  * @return
  */
CREATE PROCEDURE `proc_nft_token_uri_aggregate`(IN v_rows_insert_count INT)
BEGIN

  DECLARE v_last_id bigint;

  DECLARE v_main_lasted_id bigint;

  DECLARE EXIT HANDLER FOR SQLEXCEPTION

  BEGIN
    ROLLBACK;
	SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'An error occurred and the transaction has been rolled back.';
  END;

  UPDATE nft_aggregate_flag SET is_running = true WHERE job_name = 'Inventory:TokenUri:LastBlock';

  SELECT last_id INTO v_last_id FROM nft_aggregate_flag WHERE job_name = 'Inventory:TokenUri:LastBlock' LIMIT 1;

  SELECT id INTO v_main_lasted_id FROM nft_inventories ORDER BY id DESC LIMIT 1;

  IF v_rows_insert_count > 1 THEN

	  IF v_last_id >= v_main_lasted_id THEN

	 	 SET v_rows_insert_count = -1;

	  ELSE

	  	 IF v_main_lasted_id - v_last_id > v_rows_insert_count THEN

	  	 	SET v_rows_insert_count = v_rows_insert_count;

		 ELSE

	     	SET v_rows_insert_count = v_main_lasted_id - v_last_id ;

		 END IF;

	  END IF;


	  IF v_rows_insert_count > 0 THEN

	  --## Update token URIs in the inventories table
	  START TRANSACTION;

		  UPDATE nft_inventories T1 join nft_inventories T2 ON
		  T1.contract_address = T2.contract_address
		  AND T1.token_id = T2.token_id AND T1.holder_address = T2.holder_address
		  SET T1.token_uri = T2.token_uri
		  WHERE T2.id BETWEEN v_last_id + 1 AND v_last_id + v_rows_insert_count;

		  UPDATE nft_aggregate_flag SET last_id = (last_id + v_rows_insert_count) WHERE job_name = 'Inventory:TokenUri:LastBlock';

		  COMMIT;

	  END IF;

	END IF;

    UPDATE nft_aggregate_flag SET is_running = false WHERE job_name = 'Inventory:TokenUri:LastBlock';

END;

/**
  * Continuously call the proc_nft_holder_aggregate procedure.
  */
DELIMITER $$
CREATE PROCEDURE `proc_keep_calling_holder`()
BEGIN
  DECLARE is_running BOOLEAN;
  DECLARE v_status BOOLEAN;
  DECLARE v_exe_rows BIGINT;

  UPDATE nft_aggregate_flag SET `status` = true WHERE job_name = 'HolderService:NFT:Holder:LastBlock';
  Procs: LOOP
    SELECT is_running, exe_rows, `status` INTO is_running, v_exe_rows, v_status FROM nft_aggregate_flag WHERE job_name = 'HolderService:NFT:Holder:LastBlock';
    IF is_running THEN
      DO SLEEP(2);
    ELSE
      IF v_status THEN
      	IF v_exe_rows > 1 THEN
	        CALL proc_nft_holder_aggregate(v_exe_rows);
  			DO SLEEP(2);
		END IF;
      ELSE
        LEAVE Procs;
      END IF;
    END IF;
  END LOOP Procs;
END $$
DELIMITER;

DELIMITER $$
CREATE PROCEDURE `proc_keep_calling_holder_bk`()
BEGIN
  DECLARE is_running BOOLEAN;
  DECLARE v_status BOOLEAN;
  DECLARE v_exe_rows BIGINT;

  UPDATE nft_aggregate_flag_bk SET `status` = true WHERE job_name = 'HolderService:NFT:Holder:LastBlock';
  Procs: LOOP
    SELECT is_running, exe_rows, `status` INTO is_running, v_exe_rows, v_status FROM nft_aggregate_flag_bk WHERE job_name = 'HolderService:NFT:Holder:LastBlock';
    IF is_running THEN
      DO SLEEP(2);
    ELSE
      IF v_status THEN
      	IF v_exe_rows > 1 THEN
	        CALL proc_nft_holder_aggregate_bk(v_exe_rows);
  			DO SLEEP(2);
		END IF;
      ELSE
        LEAVE Procs;
      END IF;
    END IF;
  END LOOP Procs;
END $$
DELIMITER;

/**
  * Continuously call the proc_nft_inven_aggregate procedure.
  */
DELIMITER $$
CREATE PROCEDURE `proc_keep_calling_inven`()
BEGIN
  DECLARE is_running BOOLEAN;
  DECLARE v_status BOOLEAN;
  DECLARE v_exe_rows BIGINT;

  UPDATE nft_aggregate_flag SET `status` = true WHERE job_name = 'HolderService:NFT:Inventory:LastBlock';
  Procs: LOOP
    SELECT is_running, exe_rows, `status` INTO is_running, v_exe_rows, v_status FROM nft_aggregate_flag WHERE job_name = 'HolderService:NFT:Inventory:LastBlock';
    IF is_running THEN
      DO SLEEP(2);
    ELSE
      IF v_status THEN
      	IF v_exe_rows > 1 THEN
	        CALL proc_nft_inven_aggregate(v_exe_rows);
			DO SLEEP(2);
		END IF;
      ELSE
        LEAVE Procs;
      END IF;
    END IF;
  END LOOP Procs;
END $$
DELIMITER;


DELIMITER $$
CREATE PROCEDURE `proc_keep_calling_inven_bk`()
BEGIN
  DECLARE is_running BOOLEAN;
  DECLARE v_status BOOLEAN;
  DECLARE v_exe_rows BIGINT;

  UPDATE nft_aggregate_flag_bk SET `status` = true WHERE job_name = 'HolderService:NFT:Inventory:LastBlock';
  Procs: LOOP
    SELECT is_running, exe_rows, `status` INTO is_running, v_exe_rows, v_status FROM nft_aggregate_flag_bk WHERE job_name = 'HolderService:NFT:Inventory:LastBlock';
    IF is_running THEN
      DO SLEEP(2);
    ELSE
      IF v_status THEN
      	IF v_exe_rows > 1 THEN
	        CALL proc_nft_inven_aggregate_bk(v_exe_rows);
  			DO SLEEP(2);
		END IF;
      ELSE
        LEAVE Procs;
      END IF;
    END IF;
  END LOOP Procs;
END $$
DELIMITER;

/**
  * Continuously call the proc_nft_token_uri_aggregate procedure.
  */
DELIMITER $$
CREATE PROCEDURE `proc_keep_calling_token_uri`()
BEGIN
  DECLARE is_running BOOLEAN;
  DECLARE v_status BOOLEAN;
  DECLARE v_exe_rows BIGINT;

  SELECT SLEEP(1);
  UPDATE nft_aggregate_flag SET `status` = true WHERE job_name = 'Inventory:TokenUri:LastBlock';
  Procs: LOOP
    SELECT is_running, exe_rows, `status` INTO is_running, v_exe_rows, v_status FROM nft_aggregate_flag WHERE job_name = 'Inventory:TokenUri:LastBlock';
    IF is_running THEN
      SELECT SLEEP(1);
    ELSE
      IF v_status THEN
      	IF v_exe_rows > 1 THEN
	        CALL proc_nft_token_uri_aggregate(v_exe_rows);
		END IF;
      ELSE
        LEAVE Procs;
      END IF;
    END IF;
  END LOOP Procs;
END $$
DELIMITER;

CREATE PROCEDURE `proc_save_error_log`(IN v_error_message TEXT, IN v_job_name VARCHAR(255))
BEGIN
  INSERT INTO nft_aggregation_err_logs (job_name, error_message) VALUES (v_job_name, v_error_message);
END;


CREATE TABLE `nft_aggregation_err_logs` (
  `job_name` varchar(255) DEFAULT NULL,
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;