package io.klaytn.finder.service

import io.klaytn.finder.config.ChainProperties
import io.klaytn.finder.infra.exception.InvalidContractSubmissionException
import org.apache.commons.io.FilenameUtils
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import com.google.cloud.storage.Storage
import com.google.cloud.storage.BlobInfo
import java.util.*

@Component
class ContractImageService(
    private val finderContractImageProperties: FinderContractImageProperties,
    private val chainProperties: ChainProperties,
    private val gcsClient: Storage,
) {
    private val supportedImageMineType = listOf("image/png", "image/svg+xml")
    private val supportedImageMaxBytes = 65535

    fun checkAndGetImageHash(contractAddress: String, tokenImage: MultipartFile?): String? {
        return tokenImage?.let {
            if (!supportedImageMineType.contains(it.contentType ?: "")) {
                throw InvalidContractSubmissionException("not supported image type. please use png or svg.")
            }

            if (finderContractImageProperties.enabled) {
                val gcsBucket = finderContractImageProperties.gcsBucket!!
                val urlPrefix = finderContractImageProperties.urlPrefix!!

                val fileExt = FilenameUtils.getExtension(it.originalFilename)
                val filename = "${contractAddress}_${System.currentTimeMillis()}.$fileExt"
                val s3RelativeFilePath = "finder/static/img/contract/${chainProperties.type}/$filename"
                val blob = gcsClient.get(gcsBucket, s3RelativeFilePath)
                if (blob != null) {
                    throw InvalidContractSubmissionException("token image already exists.")
                } else {
                    val blobInfo = BlobInfo.newBuilder(gcsBucket, s3RelativeFilePath)
                        .setContentType(tokenImage.contentType)
                        .build()
                    gcsClient.create(blobInfo, it.bytes)
                }
                "$urlPrefix/$s3RelativeFilePath"
            } else {
                val encodedImage = String(Base64.getEncoder().encode(it.bytes))
                if (encodedImage.length > supportedImageMaxBytes) {
                    throw InvalidContractSubmissionException(
                        "token image size is too long. (max:$supportedImageMaxBytes bytes)"
                    )
                }
                "data:${it.contentType};base64,$encodedImage"
            }
        }
    }
}

@ConstructorBinding
@ConfigurationProperties(prefix = "finder.images.contract")
data class FinderContractImageProperties(
    val enabled: Boolean,
    val gcsBucket: String?,
    val urlPrefix: String?,
)