package io.klaytn.finder.infra.client

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

interface KlaytnSquareClient {
    @Headers("Content-Type: application/json", "Accept:application/json")
    @GET("/api/v1/councils")
    fun getGovernanceCouncils():
            Call<KlaytnSquareResult<List<KlaytnSquareGovernanceCouncilSummaryResult>>>

    @Headers("Content-Type: application/json", "Accept:application/json")
    @GET("/api/v1/councils/{squareId}")
    fun getGovernanceCouncil(
            @Path("squareId") squareId: Long
    ): Call<KlaytnSquareResult<KlaytnSquareGovernanceCouncilDetailResult>>
}

data class KlaytnSquareResult<T>(
        val code: Int,
        @JsonProperty("err_msg") val errorMessage: String,
        val result: T
)

data class KlaytnSquareGovernanceCouncilSummaryResult(
        val id: Long,
        val name: String,
        @JsonProperty("joined_at") val joinedAt: Date,
        val thumbnail: String,
        val website: String,
        @JsonProperty("gc_config_by_site") val gcConfigBySite: String?
)

data class KlaytnSquareGovernanceCouncilDetailResult(
        val id: Long,
        val name: String,
        @JsonProperty("contracts") val contracts: List<KlaytnSquareGovernanceCouncilContract>
)

data class KlaytnSquareGovernanceCouncilContract(val address: String, val type: String)

data class KlaytnSquareGovernanceCouncilConfig(
        val status: String,
)
