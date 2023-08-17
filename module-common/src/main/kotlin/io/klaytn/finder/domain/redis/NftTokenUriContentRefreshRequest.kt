package io.klaytn.finder.domain.redis

data class NftTokenUriContentRefreshRequest(
        val chain: String,
        val contractAddress: String,
        val tokenId: String,
        val tokenUri: String,
)
