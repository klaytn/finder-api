package io.klaytn.finder.infra.cache

object CacheName {
    const val ACCOUNT_BY_ADDRESS = "account-by-address"
    const val CONTRACT_BY_ADDRESS = "contract-by-address"

    const val BLOCK_BY_NUMBER = "block-by-number"
    const val BLOCK_LATEST_NUMBER = "block-latest-number"

    const val TRANSACTION_BY_HASH = "transaction-by-transaction-hash"
    const val TRANSACTION_LATEST_ID = "transaction-latest-id"
    const val INTERNAL_TRANSACTION = "internal-transaction"
    const val EVENT_LOG = "event-log"

    const val TOKEN_TRANSFER = "token-transfer2"
    const val TOKEN_HOLDER = "token-holder"
    const val TOKEN_BURN = "token-burn"
    const val TOKEN_HOLDER_COUNT_BY_CONTRACT = "token-holder-count-by-contract"

    const val NFT_TRANSFER = "nft-transfer2"
    const val NFT_INVENTORY = "nft-inventory"
    const val NFT_INVENTORY_COUNT_BY_CONTRACT = "nft-inventory-count-by-contract"
    const val NFT_17_HOLDER = "nft-17-holder"
    const val NFT_17_HOLDER_COUNT_BY_CONTRACT = "nft-17-holder-count-by-contract"
    const val NFT_BURN = "nft-burn"

    const val CAVER_NFT_TOKEN_ITEM_TOTAL_SUPPLY = "caver-nft-token-item-ts"
    const val CAVER_BLOCK_COMMITTEE = "caver-block-committee"
    const val CAVER_COUNCIL_SIZE = "caver-council-size"
    const val CAVER_BLOCK_REWARD = "caver-block-reward"

    const val ACCOUNT_ADDRESS_BY_KNS = "account-address-by-kns"

    const val GAS_PRICE = "gas-price"
    const val ACCOUNT_TAGS = "account-tags"
    const val ACCOUNT_RELATED_INFOS = "account-related-infos"
    const val USER_BY_ACCESS_KEY = "user-by-access-key"

    const val FUNCTION_SIGNATURE = "function-signature"
    const val EVENT_SIGNATURE = "event-signature"

    const val ACCOUNT_TOKEN_APPROVE = "account-token-approve"
    const val ACCOUNT_NFT_APPROVE = "account-nft-approve"

    const val BLOCK_BURN = "block-burn"
    const val BLOCK_REWARD_BY_NUMBER = "block-reward-by-number"

    const val GOVERNANCE_COUNCIL_BY_SQUARE_ID = "gc-by-squareid"
    const val GOVERNANCE_COUNCIL_CONTRACT = "gc-contract"
    const val GOVERNANCE_COUNCIL_CONTRACT_ID_BY_ADDRESS = "gc-contract-id-by-address"
    const val GOVERNANCE_COUNCIL_CONTRACT_IDS_BY_SQUARE_ID = "gc-contract-ids-by-squareid"

    const val APP_PRICE_PLANS = "app-price-plans"
    const val APP_USER = "app-user"
    const val APP_USER_KEY_BY_ID = "app-user-key-by-id"
    const val APP_USER_KEY_ID_BY_ACCESS_KEY = "app-user-key-id-by-access-key"
    const val APP_USER_KEY_IDS_BY_APP_USER_ID = "app-user-key-ids-by-app-user-id"

    const val ACCOUNT_KEY_BY_ID = "account-key-by-id"
    const val STAT_TOTAL_TRANSACTION_COUNT = "stat-total-tx-cnt"
}
