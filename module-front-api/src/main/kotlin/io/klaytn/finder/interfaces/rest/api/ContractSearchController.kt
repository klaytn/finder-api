package io.klaytn.finder.interfaces.rest.api

import io.klaytn.finder.infra.ServerMode
import io.klaytn.finder.infra.client.opensearch.model.ContractSearchPageRequest
import io.klaytn.finder.infra.client.opensearch.model.ContractSearchRequest
import io.klaytn.finder.infra.web.model.ScopePage
import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.klaytn.finder.interfaces.rest.api.view.model.nft.NftListView
import io.klaytn.finder.interfaces.rest.api.view.model.token.TokenListView
import io.klaytn.finder.interfaces.rest.api.view.mapper.ContractToNftListViewMapper
import io.klaytn.finder.interfaces.rest.api.view.mapper.ContractToTokenListViewMapper
import io.klaytn.finder.service.ContractService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import javax.validation.Valid

@Profile(ServerMode.API_MODE)
@RestController
@Tag(name= SwaggerConstant.TAG_PUBLIC)
class ContractSearchController(
    val contractService: ContractService,
    val contractToTokenListViewMapper: ContractToTokenListViewMapper,
    val contractToNftListViewMapper: ContractToNftListViewMapper,
) {
    @Operation(
        description = "Search for Token lists by Name or Symbol.",
        parameters = [
            Parameter(name = "keyword", description = "Search keyword", required = true, `in` = ParameterIn.QUERY),
            Parameter(name = "fromDate", description = "Start date for search (based on TOKEN creation)", `in` = ParameterIn.QUERY),
            Parameter(name = "toDate", description = "End date for search (based on TOKEN creation)", `in` = ParameterIn.QUERY),
        ]
    )
    @GetMapping("/api/v1/searches/tokens")
    fun searchTokens(
        @RequestParam(required = true) keyword: String?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) fromDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) toDate: LocalDate?,
        @Valid contractSearchPageRequest: ContractSearchPageRequest,
    ): ScopePage<TokenListView> {
        val contractSearchRequest =
            ContractSearchRequest.ofPageForToken(keyword, null, fromDate, toDate, contractSearchPageRequest)
        return ScopePage.of(contractService.search(contractSearchRequest), contractToTokenListViewMapper)
    }

    @Operation(
        description = "Search for verified-Token lists.",
        parameters = [
            Parameter(name = "fromDate", description = "Start date for search (based on TOKEN creation)", `in` = ParameterIn.QUERY),
            Parameter(name = "toDate", description = "End date for search (based on TOKEN creation)", `in` = ParameterIn.QUERY),
        ]
    )
    @GetMapping("/api/v1/searches/verified-tokens")
    fun searchVerifiedTokens(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) fromDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) toDate: LocalDate?,
        @Valid contractSearchPageRequest: ContractSearchPageRequest,
    ): ScopePage<TokenListView> {
        val contractSearchRequest =
            ContractSearchRequest.ofPageForToken(null, true, fromDate, toDate, contractSearchPageRequest)
        return ScopePage.of(contractService.search(contractSearchRequest), contractToTokenListViewMapper)
    }

    @Operation(
        description = "Search for NFT lists by Name or Symbol.",
        parameters = [
            Parameter(name = "keyword", description = "Keyword", required = true, `in` = ParameterIn.QUERY),
            Parameter(name = "fromDate", description = "Start date for search (based on NFT creation)", `in` = ParameterIn.QUERY),
            Parameter(name = "toDate", description = "End date for search (based on NFT creation)", `in` = ParameterIn.QUERY),
        ]
    )
    @GetMapping("/api/v1/searches/nfts")
    fun searchNfts(
        @RequestParam(required = true) keyword: String?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) fromDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) toDate: LocalDate?,
        @Valid contractSearchPageRequest: ContractSearchPageRequest,
    ): ScopePage<NftListView> {
        val contractSearchRequest =
            ContractSearchRequest.ofPageForNft(keyword, null, fromDate, toDate, contractSearchPageRequest)
        return ScopePage.of(contractService.search(contractSearchRequest), contractToNftListViewMapper)
    }

    @Operation(
        description = "Search for Verified-NFT lists.",
        parameters = [
            Parameter(name = "fromDate", description = "Start date for search (based on NFT creation)", `in` = ParameterIn.QUERY),
            Parameter(name = "toDate", description = "End date for search (based on NFT creation)", `in` = ParameterIn.QUERY),
        ]
    )
    @GetMapping("/api/v1/searches/verified-nfts")
    fun searchVerifiedNfts(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) fromDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) toDate: LocalDate?,
        @Valid contractSearchPageRequest: ContractSearchPageRequest,
    ): ScopePage<NftListView> {
        val contractSearchRequest =
            ContractSearchRequest.ofPageForNft(null, true, fromDate, toDate, contractSearchPageRequest)
        return ScopePage.of(contractService.search(contractSearchRequest), contractToNftListViewMapper)
    }
}