package io.klaytn.finder.interfaces.rest.api

import io.klaytn.finder.infra.ServerMode
import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.klaytn.finder.interfaces.rest.api.view.model.kaiauser.KaiaUserSignInView
import io.klaytn.finder.interfaces.rest.api.view.model.kaiauser.KaiaUserSignupView
import io.klaytn.finder.service.KaiaUserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.*

@Profile(ServerMode.API_MODE)
@RestController
@Tag(name = SwaggerConstant.TAG_PUBLIC)
class KaiaUserController(
    val kaiaUserService: KaiaUserService
) {
    @Operation(description = "Sign Up")
    @PostMapping("/api/v1/kaia/users/sign-up")
    fun signUp(@RequestBody kaiaUser: KaiaUserSignupView) =
        kaiaUserService.signUp(kaiaUser)

    @Operation(description = "Sign In")
    @PostMapping("/api/v1/kaia/users/sign-in")
    fun signIn(@RequestBody kaiaUser: KaiaUserSignInView) =
        kaiaUserService.signIn(kaiaUser)

    @Operation(description = "Verify Email")
    @GetMapping("/api/v1/kaia/users/verify-email")
    fun verifyEmail(@RequestParam jwtToken: String) =
        kaiaUserService.verifyEmail(jwtToken)

    @Operation(description = "Account personal information")
    @GetMapping("/api/v1/kaia/users/account")
    fun account() =
        kaiaUserService.account()

}