package io.klaytn.finder.domain.mysql.set3.nft

import io.klaytn.finder.domain.mysql.BaseRepository
import org.springframework.stereotype.Repository

@Repository
interface NftItemRepository : BaseRepository<NftItem> {
    fun findNftItemByContractAddressAndTokenId(contractAddress: String, tokenId: String): NftItem?
}
