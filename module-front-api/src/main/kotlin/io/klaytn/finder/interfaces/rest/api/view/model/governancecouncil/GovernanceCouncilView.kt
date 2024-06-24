package io.klaytn.finder.interfaces.rest.api.view.model.governancecouncil

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import io.klaytn.finder.domain.common.GovernanceCouncilContractType
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema
data class GovernanceCouncilView(
    @JsonIgnore val squareId: Long,
    val name: String,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val squareLink: String,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val thumbnail: String,
    val website: List<String>,
    val contracts: Map<GovernanceCouncilContractType, List<String>>,
    val joinedAt: LocalDateTime?,
)

@Schema
data class GovernanceCouncilWithCategoryView(
    val name: String,
    @JsonProperty("square_link") val squareLink: String,
    val thumbnail: String,
    @JsonProperty("total_staking") val totalStaking: String,
    val joinedAt: LocalDateTime?,
    val apy: String,
    val description: String,
    val categories: List<GovernanceCouncilCategory>,
)

@Schema
data class GovernanceCouncilCategory(
    val id: Long,
    val name: String
)