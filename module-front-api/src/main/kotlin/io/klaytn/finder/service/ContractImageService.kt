package io.klaytn.finder.service

import io.klaytn.finder.config.ChainProperties
import io.klaytn.finder.infra.exception.InvalidContractSubmissionException
import org.apache.commons.io.FilenameUtils
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.util.*

@Component
class ContractImageService(
    private val finderContractImageProperties: FinderContractImageProperties,
    private val chainProperties: ChainProperties,
    private val s3Client: S3Client,
) {
    private val supportedImageMineType = listOf("image/png", "image/svg+xml")
    private val supportedImageMaxBytes = 65535

    fun checkAndGetImageHash(contractAddress: String, tokenImage: MultipartFile?): String? {
        return tokenImage?.let {
            if (!supportedImageMineType.contains(it.contentType ?: "")) {
                throw InvalidContractSubmissionException("not supported image type. please use png or svg.")
            }

            if (finderContractImageProperties.enabled) {
                val s3Bucket = finderContractImageProperties.s3Bucket!!
                val urlPrefix = finderContractImageProperties.urlPrefix!!

                val fileExt = FilenameUtils.getExtension(it.originalFilename)
                val filename = "${contractAddress}_${System.currentTimeMillis()}.$fileExt"
                val s3RelativeFilePath = "finder/static/img/contract/${chainProperties.type}/$filename"
                val imageUploadRequest = PutObjectRequest.builder()
                    .bucket(s3Bucket).key(s3RelativeFilePath).contentType(tokenImage.contentType).build()
                s3Client.putObject(imageUploadRequest, RequestBody.fromBytes(it.bytes))

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
    val s3Bucket: String?,
    val urlPrefix: String?,
)