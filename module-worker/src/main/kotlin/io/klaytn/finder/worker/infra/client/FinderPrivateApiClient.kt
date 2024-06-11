package io.klaytn.finder.worker.infra.client

import com.fasterxml.jackson.annotation.JsonProperty
import io.klaytn.commons.utils.logback.logger
import org.springframework.web.bind.annotation.PostMapping
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.*

interface FinderPrivateApiClient {
    @POST("/papi/v1/function-signatures")
    fun addFunctionSignature(
        @Query("fourByteId") fourByteId: Long?,
        @Query("bytesSignature") bytesSignature: String,
        @Query("textSignature") textSignature: String,
    ): Call<FunctionSignatureResponse>

    @POST("/papi/v1/event-signatures")
    fun addEventSignature(
        @Query("fourByteId") fourByteId: Long?,
        @Query("hexSignature") hexSignature: String,
        @Query("textSignature") textSignature: String,
    ): Call<EventSignatureResponse>

    @PUT("/papi/v1/nfts/{nftAddress}/inventories/{tokenId}/refresh")
    fun refreshNftItem(
        @Path("nftAddress") nftAddress: String,
        @Path("tokenId") tokenId: String,
        @Query("batchSize") batchSize: Int?,
    ): Call<SimpleApiResponse>

    @PUT("/papi/v1/governance-councils/sync")
    fun governanceCouncilSync(
        @Query("dryRun") dryRun: Boolean
    ): Call<SimpleApiResponse>

    @PUT("/papi/v1/governance-councils/info")
    fun governanceCouncilInfoSync(
        @Query("dryRun") dryRun: Boolean
    ): Call<SimpleApiResponse>

    @GET("/papi/v1/coin-market-cap/cryptocurrency")
    fun getCoinMarketCap(): Call<SimpleApiResponse>

    @POST("/papi/v1/block-proposers/{yearMonth}/source")
    fun blockProposerSource(
        @Path("yearMonth") yearMonth: String,
    ): Call<SimpleApiResponse>

    @POST("/papi/v1/block-proposers/{yearMonth}/csv")
    fun blockProposerCSV(
        @Path("yearMonth") yearMonth: String,
    ): Call<SimpleApiResponse>
}

data class FunctionSignatureResponse(
    @JsonProperty("four_byte_id")
    val fourByteId: Long?,
    @JsonProperty("bytes_signature")
    val bytesSignature: String,
    @JsonProperty("text_signature")
    val textSignature: String
)

data class EventSignatureResponse(
    @JsonProperty("four_byte_id")
    val fourByteId: Long?,
    @JsonProperty("hex_signature")
    val hexSignature: String,
    @JsonProperty("text_signature")
    val textSignature: String
)

data class SimpleApiResponse(
    @JsonProperty("result")
    val result: Boolean,
)

class FunctionSignatureCallback(
    private val signatureType: SignatureType,
    private val chainType: String,
): Callback<FunctionSignatureResponse> {
    private val logger = logger(this::class.java)

    override fun onResponse(call: Call<FunctionSignatureResponse>, response: Response<FunctionSignatureResponse>) {
        logger.info("[$signatureType/$chainType] ${response.raw()}")
    }

    override fun onFailure(call: Call<FunctionSignatureResponse>, t: Throwable) {
        logger.warn("[$signatureType/$chainType] ${t.message}", t)
    }
}

class EventSignatureCallback(
    private val signatureType: SignatureType,
    private val chainType: String,
): Callback<EventSignatureResponse> {
    private val logger = logger(this::class.java)

    override fun onResponse(call: Call<EventSignatureResponse>, response: Response<EventSignatureResponse>) {
        logger.info("[$signatureType/$chainType] ${response.raw()}")
    }

    override fun onFailure(call: Call<EventSignatureResponse>, t: Throwable) {
        logger.warn("[$signatureType/$chainType] ${t.message}", t)
    }
}

class SimpleApiResponseCallback(private val name: String): Callback<SimpleApiResponse> {
    private val logger = logger(this::class.java)

    override fun onResponse(call: Call<SimpleApiResponse>, response: Response<SimpleApiResponse>) {
        logger.info("[$name] ${response.raw()}")
    }

    override fun onFailure(call: Call<SimpleApiResponse>, t: Throwable) {
        logger.warn("[$name] ${t.message}", t)
    }
}
