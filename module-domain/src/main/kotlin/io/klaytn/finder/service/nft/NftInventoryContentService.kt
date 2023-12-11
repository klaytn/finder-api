package io.klaytn.finder.service.nft

import io.klaytn.finder.config.ChainProperties
import io.klaytn.finder.infra.exception.NotFoundNftInventoryContentFromS3
import org.springframework.stereotype.Service
import com.google.cloud.storage.Storage
import io.klaytn.finder.config.FinderGcsProperties

@Service
class NftInventoryContentService(
    private val chainProperties: ChainProperties,
    private val gcsClient: Storage,
    private val finderGcsProperties: FinderGcsProperties,
) {
    fun getNftInventoryContent(nftAddress: String, tokenId: String): String {
        val chainType = chainProperties.type
        val key = "finder/$chainType/nft-inventory-contents/$nftAddress/$tokenId"
        try {
            val blob = gcsClient.get(finderGcsProperties.privateBucket, key)
            return String(blob.getContent())
        } catch (e: Exception) {
            throw NotFoundNftInventoryContentFromS3(e)
        }
    }
}
