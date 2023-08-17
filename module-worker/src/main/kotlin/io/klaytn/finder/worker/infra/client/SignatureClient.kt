package io.klaytn.finder.worker.infra.client

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface SignatureClient {
    @GET("/api/v1/signatures")
    fun getSignatures(
            @Query("page") page: Int = 1,
            @Query("ordering") order: String = "-created_at"
    ): Call<SignatureResponse>

    @GET("/api/v1/event-signatures")
    fun getEventSignatures(
            @Query("page") page: Int = 1,
            @Query("ordering") order: String = "-created_at"
    ): Call<SignatureResponse>
}

enum class SignatureType(val key: String) {
    FUNCTION("function-signatures"),
    EVENT("event-signatures")
}

data class SignatureResponse(
        val count: Int,
        val previous: String?,
        val next: String?,
        val results: List<Signature>
)

data class Signature(
        val id: Int,
        @JsonProperty("hex_signature") @JsonAlias("hex_signature", "hex") val hexSignature: String,
        @JsonProperty("text_signature")
        @JsonAlias("text_signature", "text")
        val textSignature: String,
)
