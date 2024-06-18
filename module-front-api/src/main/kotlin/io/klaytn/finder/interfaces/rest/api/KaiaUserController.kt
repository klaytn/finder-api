package io.klaytn.finder.interfaces.rest.api

import io.klaytn.finder.infra.ServerMode
import io.klaytn.finder.infra.exception.InvalidRequestException
import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.klaytn.finder.interfaces.rest.api.view.model.kaiauser.*
import io.klaytn.finder.service.KaiaUserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

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
    fun signIn(@RequestBody kaiaUser: KaiaUserSignInView, response: HttpServletResponse): ResponseEntity<KaiaUserView> {
        val (userView, sessionId) = kaiaUserService.signIn(kaiaUser)
        val sessionCookie = Cookie("_KAIA.sessionId", sessionId).apply {
            maxAge = 86400 // 1 Day
            isHttpOnly = true
            path = "/"
            domain = "localhost"  //TODO: Domain Settings
        }
        response.addCookie(sessionCookie)
        return ResponseEntity.ok(userView)
    }

    @Operation(description = "Sign Out")
    @PostMapping("/api/v1/kaia/users/sign-out")
    fun signOut(request: HttpServletRequest, response: HttpServletResponse): ResponseEntity<Unit> {
        val cookies = request.cookies ?: throw InvalidRequestException("No cookies present in the request")
        val sessionCookie = cookies.firstOrNull { it.name == "_KAIA.sessionId" }
            ?: throw InvalidRequestException("Session cookie not found")
        val sessionKey = sessionCookie.value

        kaiaUserService.signOut(sessionKey)

        val expireSessionCookie = Cookie("_KAIA.sessionId", "").apply {
            maxAge = 0
            isHttpOnly = true
            path = "/"
            domain = "localhost"  //TODO: Domain Settings
        }
        response.addCookie(expireSessionCookie)

        return ResponseEntity.noContent().build()
    }


    @Operation(description = "Verify Email")
    @GetMapping("/api/v1/kaia/users/verify-email")
    fun verifyEmail(@RequestParam jwtToken: String) =
        kaiaUserService.verifyEmail(jwtToken)

    @Operation(description = "Account personal information")
    @GetMapping("/api/v1/kaia/users/account")
    fun account() =
        kaiaUserService.account()

    @Operation(description = "Change password")
    @PutMapping("/api/v1/kaia/users/change-password")
    fun changePassword(@RequestBody kaiaUserChangePasswordView: KaiaUserChangePasswordView) =
        kaiaUserService.changePassword(kaiaUserChangePasswordView)

    @Operation(description = "Delete account")
    @DeleteMapping("/api/v1/kaia/users/delete-account")
    fun deleteAccount(@RequestBody deleteAccountView: KaiaUserDeleteAccountView) {
        kaiaUserService.deleteAccount(deleteAccountView)
    }

    @Operation(description = "Login history")
    @GetMapping("/api/v1/kaia/users/login-history")
    fun loginHistory(): List<String> =
        kaiaUserService.loginHistory()
}