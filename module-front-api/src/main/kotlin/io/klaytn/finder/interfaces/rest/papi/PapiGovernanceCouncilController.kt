package io.klaytn.finder.interfaces.rest.papi

import io.klaytn.commons.model.response.SimpleResponse
import io.klaytn.finder.infra.ServerMode
import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.klaytn.finder.service.GovernanceCouncilSyncService
import io.klaytn.finder.service.GovernanceCouncilInfoService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Profile(ServerMode.PRIVATE_API_MODE)
@RestController
@Tag(name= SwaggerConstant.TAG_PRIVATE)
class PapiGovernanceCouncilController(
    private val governanceCouncilSyncService: GovernanceCouncilSyncService,
    private val governanceCouncilInfoService: GovernanceCouncilInfoService,
) {
    @Operation(
        description = "Synchronize Governance Council information.",
    )
    @PutMapping("/papi/v1/governance-councils/sync")
    fun governanceCouncilSync(@RequestParam dryRun: Boolean) =
        SimpleResponse(governanceCouncilSyncService.doSync(dryRun))

    @PutMapping("/papi/v1/governance-councils/info")
    fun governanceCouncilInfoSync(@RequestParam dryRun: Boolean) =
        SimpleResponse(governanceCouncilInfoService.doSync(dryRun))
}
