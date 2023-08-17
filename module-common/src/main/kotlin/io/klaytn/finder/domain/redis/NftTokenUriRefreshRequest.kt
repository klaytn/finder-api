package io.klaytn.finder.domain.redis

data class NftTokenUriRefreshRequest(
        val chain: String,
        val contractAddress: String,
        val tokenId: String,
)
