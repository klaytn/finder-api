package io.klaytn.finder.interfaces.rest.api

import io.klaytn.finder.infra.ServerMode
import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.klaytn.finder.service.GovernanceCouncilInfoService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Profile(ServerMode.API_MODE)
@RestController
@Tag(name = SwaggerConstant.TAG_PUBLIC)
class GovernanceCouncilController(
    val governanceCouncilInfoService: GovernanceCouncilInfoService
) {
    //TODO : Newly Joined API
    //TODO : Category - Governance Council
    @GetMapping("/api/v1/governance-councils")
    fun governanceCouncilInfo() = governanceCouncilInfoService.getAll()
    //TODO : Detail API

}