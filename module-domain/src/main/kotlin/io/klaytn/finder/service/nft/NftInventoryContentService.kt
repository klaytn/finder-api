package io.klaytn.finder.service.nft

import io.klaytn.finder.config.ChainProperties
import io.klaytn.finder.config.FinderS3Properties
import io.klaytn.finder.infra.exception.NotFoundNftInventoryContentFromS3
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.NoSuchKeyException

@Service
class NftInventoryContentService(
    private val chainProperties: ChainProperties,
    private val s3Client: S3Client,
    private val finderS3Properties: FinderS3Properties,
) {
    fun getNftInventoryContent(nftAddress: String, tokenId: String): String {
        val chainType = chainProperties.type
        val getObjectRequest: GetObjectRequest = GetObjectRequest.builder()
            .bucket(finderS3Properties.privateBucket)
            .key("finder/$chainType/nft-inventory-contents/$nftAddress/$tokenId")
            .build()

        try {
            val response = s3Client.getObject(getObjectRequest)
            return String(response.readBytes())
        } catch (noSuchKeyException: NoSuchKeyException) {
            throw NotFoundNftInventoryContentFromS3(noSuchKeyException)
        }
    }
}
