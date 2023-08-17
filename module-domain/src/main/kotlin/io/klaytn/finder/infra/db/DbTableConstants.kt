package io.klaytn.finder.infra.db

class DbTableConstants {
    companion object {
        const val transactions = "transactions"

        const val tokenTransfers = "token_transfers"
        const val tokenBurns = "token_burns"

        const val nftTransfers = "nft_transfers"
        const val nftBurns = "nft_burns"
    }
}