package io.klaytn.finder.interfaces.rest.api

import io.klaytn.finder.infra.ServerMode
import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.RestController
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.Operation
import io.klaytn.finder.service.KaiaUserService
import org.springframework.web.bind.annotation.*
import io.klaytn.finder.domain.mysql.set1.KaiaUser

@Profile(ServerMode.API_MODE)
@RestController
@Tag(name = SwaggerConstant.TAG_PUBLIC)
class KaiaUserController(
    val kaiaUserService: KaiaUserService
) {
    @Operation(description = "Sign Up")
    @PostMapping("/api/v1/kaia/users")
    fun signUp(@RequestBody kaiaUser: KaiaUser) {
        kaiaUserService.signUp(kaiaUser)
    }
}