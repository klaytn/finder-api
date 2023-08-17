package io.klaytn.finder.interfaces.rest.papi

import io.klaytn.finder.domain.mysql.set1.AccountTag
import io.klaytn.finder.infra.ServerMode
import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.klaytn.finder.service.AccountTagService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.*

@Profile(ServerMode.PRIVATE_API_MODE)
@RestController
@Tag(name= SwaggerConstant.TAG_PRIVATE)
class PapiAccountTagController(
    private val accountTagService: AccountTagService,
) {
    @GetMapping("/papi/v1/account-tags")
    fun getAll() = accountTagService.getAll()

    @PutMapping("/papi/v1/account-tags")
    fun update(
        @RequestParam("tag") tag: String,
        @RequestParam("tagOrder") tagOrder: Int,
        @RequestParam("display") display: Boolean,
    ) = accountTagService.updateAccountTag(AccountTag(tag, tagOrder, display))

    @DeleteMapping("/papi/v1/account-tags")
    fun delete(@RequestParam("tag") tag: String) = accountTagService.deleteAccountTag(tag)
}