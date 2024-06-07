package io.klaytn.finder.infra.client

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

interface KaiaSquareClient {
    @Headers("Content-Type: application/json", "Accept:application/json")
    @GET("/api/v1/councils")
    fun getGovernanceCouncils():
            Call<KaiaSquareResult<List<KaiaSquareGovernanceCouncilSummaryResult>>>

    @Headers("Content-Type: application/json", "Accept:application/json")
    @GET("/api/v1/councils/{squareId}")
    fun getGovernanceCouncil(
            @Path("squareId") squareId: Long
    ): Call<KaiaSquareResult<KaiaSquareGovernanceCouncilDetailResult>>
}

data class KaiaSquareResult<T>(
        val code: Int,
        @JsonProperty("err_msg") val errorMessage: String,
        val result: T
)

data class Category(
    val id: Long,
    val name: String,
)

data class SiteConf(
    @JsonProperty("is_foundation") val isFoundation: Boolean,
    val status: String,
)

data class KaiaSquareGovernanceCouncilSummaryResult(
        val id: Long,
        val name: String,
        val summary: String? = null,
        val description: String,
        val categories: List<Category>,
        @JsonProperty("site_conf") val siteConf: SiteConf? = null,
        @JsonProperty("joined_at") val joinedAt: Date,
        val thumbnail: String,
        val websites: List<Map<String, String>>?,
        @JsonProperty("gc_config_by_site") val gcConfigBySite: String?
)

data class KaiaSquareGovernanceCouncilDetailResult(
        val id: Long,
        val name: String,
        @JsonProperty("contracts") val contracts: List<KaiaSquareGovernanceCouncilContract>
)

data class KaiaSquareGovernanceCouncilContract(val address: String, val type: String)

data class KaiaSquareGovernanceCouncilConfig(
        val status: String,
)