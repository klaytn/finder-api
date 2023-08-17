package io.klaytn.finder.interfaces.rest.api.view.model.governancecouncil

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
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
