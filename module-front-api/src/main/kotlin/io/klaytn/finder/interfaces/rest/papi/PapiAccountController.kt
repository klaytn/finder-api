package io.klaytn.finder.interfaces.rest.papi

import io.klaytn.commons.model.request.SimpleRequest
import io.klaytn.commons.utils.logback.logger
import io.klaytn.finder.infra.ServerMode
import io.klaytn.finder.infra.client.opensearch.AccountSearchType
import io.klaytn.finder.infra.web.model.PapiSimplePageRequest
import io.klaytn.finder.infra.web.model.ScopePage
import io.klaytn.finder.infra.web.model.SimplePageRequest
import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.klaytn.finder.interfaces.rest.papi.view.PapiAccountItemView
import io.klaytn.finder.interfaces.rest.papi.view.mapper.PapiAccountToItemViewMapper
import io.klaytn.finder.interfaces.rest.papi.view.mapper.PapiNftInventoryToListViewMapper
import io.klaytn.finder.service.AccountService
import io.klaytn.finder.service.AccountUpdateService
import io.klaytn.finder.service.nft.NftInventoryService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.tags.Tag
import org.apache.commons.io.IOUtils
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.nio.charset.StandardCharsets
import javax.validation.Valid

@Profile(ServerMode.PRIVATE_API_MODE)
@RestController
@Tag(name= SwaggerConstant.TAG_PRIVATE)
class PapiAccountController(
    private val accountService: AccountService,
    private val accountUpdateService: AccountUpdateService,
    private val nftInventoryService: NftInventoryService,
    private val papiNftInventoryToListViewMapper: PapiNftInventoryToListViewMapper,
    private val papiAccountToItemViewMapper: PapiAccountToItemViewMapper
) {
    private val logger = logger(this::class.java)

    @Operation(
        description = "Retrieve an account.",
        parameters = [
            Parameter(name = "accountAddress", description = "account address", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/papi/v1/accounts/{accountAddress}")
    fun getAccount(@PathVariable accountAddress: String) =
        papiAccountToItemViewMapper.transform(accountService.getAccount(accountAddress))

    @GetMapping("/papi/v1/accounts/searches/{accountSearchType}")
    fun searchAccounts(
        @PathVariable accountSearchType: AccountSearchType,
        @RequestParam keyword: String,
        @Valid simplePageRequest: SimplePageRequest
    ) =
        ScopePage.of(
            accountService.search(accountSearchType, keyword, simplePageRequest),
            papiAccountToItemViewMapper
        )

    @Operation(
        description = "Modify the address label of an account.",
        parameters = [
            Parameter(name = "accountAddress", description = "account address", `in` = ParameterIn.PATH),
            Parameter(name = "addressLabel", description = "address label", `in` = ParameterIn.QUERY),
        ]
    )
    @PutMapping("/papi/v1/accounts/{accountAddress}/address-labels")
    fun updateAddressLabel(
        @PathVariable accountAddress: String,
        @RequestParam addressLabel: String
    ): PapiAccountItemView {
        accountUpdateService.updateAddressLabel(accountAddress = accountAddress, addressLabel = addressLabel.ifBlank { null })
        return papiAccountToItemViewMapper.transform(accountService.getAccount(accountAddress))
    }

    @Operation(
        description = "Update the KNS domain of an account.",
        parameters = [
            Parameter(name = "accountAddress", description = "account address", `in` = ParameterIn.PATH),
            Parameter(name = "knsDomain", description = "kns domain", `in` = ParameterIn.QUERY),
        ]
    )
    @PutMapping("/papi/v1/accounts/{accountAddress}/kns-domain")
    fun updateKnsDoamin(
        @PathVariable accountAddress: String,
        @RequestParam knsDomain: String
    ): PapiAccountItemView {
        accountUpdateService.updateKnsDomain(accountAddress = accountAddress, knsDomain = knsDomain.ifBlank { null })
        return papiAccountToItemViewMapper.transform(accountService.getAccount(accountAddress))
    }

    @Operation(
        description = "Update the address label of an account.",
    )
    @PutMapping("/papi/v1/accounts/address-labels")
    fun updateAddressLabels(@RequestPart addressLabelFile: MultipartFile, @RequestParam dryRun: Boolean) {
        val addressLabels = IOUtils.readLines(addressLabelFile.inputStream, StandardCharsets.UTF_8)
        addressLabels.forEach {
            val addressLabelToken = it.split(",")
            val accountAddress = addressLabelToken[0].trim()
            val addressLabel = addressLabelToken[1].trim()
            logger.info("$accountAddress => $addressLabel")

            if (!dryRun) {
                accountUpdateService.updateAddressLabel(accountAddress = accountAddress, addressLabel = addressLabel)
            }
        }
    }

    @Operation(
        description = "Update the tags of an account with the requested tags.",
        parameters = [
            Parameter(name = "accountAddress", description = "account address", `in` = ParameterIn.PATH),
        ],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "List of address tags",
            required = true,
        )
    )
    @PutMapping("/papi/v1/accounts/{accountAddress}/tags")
    fun updateTags(
        @PathVariable accountAddress: String,
        @RequestBody simpleRequest: SimpleRequest<List<String>>,
    ): PapiAccountItemView {
        accountUpdateService.updateTags(accountAddress = accountAddress, tags = simpleRequest.request)
        return papiAccountToItemViewMapper.transform(accountService.getAccount(accountAddress))
    }

    @Operation(
        description = "Add a tag to the account.",
        parameters = [
            Parameter(name = "accountAddress", description = "account address", `in` = ParameterIn.PATH),
            Parameter(name = "tag", description = "Tag to be added.", `in` = ParameterIn.PATH),
        ],
    )
    @PostMapping("/papi/v1/accounts/{accountAddress}/tags/{tag}")
    fun addTag(@PathVariable accountAddress: String, @PathVariable tag: String): PapiAccountItemView {
        accountUpdateService.addTags(accountAddress = accountAddress, listOf(tag))
        return papiAccountToItemViewMapper.transform(accountService.getAccount(accountAddress))
    }

    @Operation(
        description = "Delete the specified tag of the account.",
        parameters = [
            Parameter(name = "accountAddress", description = "account address", `in` = ParameterIn.PATH),
            Parameter(name = "tag", description = "Tag to be deleted.", `in` = ParameterIn.PATH),
        ],
    )
    @DeleteMapping("/papi/v1/accounts/{accountAddress}/tags/{tag}")
    fun deleteTag(@PathVariable accountAddress: String, @PathVariable tag: String): PapiAccountItemView {
        accountUpdateService.removeTags(accountAddress = accountAddress, listOf(tag))
        return papiAccountToItemViewMapper.transform(accountService.getAccount(accountAddress))
    }

    @Operation(
        description = "Retrieve the list of NFT balances for the account.",
        parameters = [
            Parameter(name = "accountAddress", description = "account address", `in` = ParameterIn.PATH),
        ],
    )
    @GetMapping("/papi/v1/accounts/{accountAddress}/nft-inventories")
    fun getNftBalances(
        @PathVariable accountAddress: String,
        @RequestParam(required = false, defaultValue = "true") excludeIfTokenUriIsEmpty: Boolean = true,
        @Valid papiSimplePageRequest: PapiSimplePageRequest,
    ) =
        ScopePage.of(
            nftInventoryService.getNftInventoriesByHolderAddress(
                accountAddress, excludeIfTokenUriIsEmpty, papiSimplePageRequest.toSimplePageRequest()
            ),
            papiNftInventoryToListViewMapper
        )
}