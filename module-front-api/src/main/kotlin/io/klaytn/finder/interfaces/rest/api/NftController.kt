package io.klaytn.finder.interfaces.rest.api

import io.klaytn.commons.model.response.SimpleResponse
import io.klaytn.finder.infra.ServerMode
import io.klaytn.finder.infra.exception.NotFoundNftException
import io.klaytn.finder.infra.exception.NotFoundNftTokenItemException
import io.klaytn.finder.infra.web.model.BlockRangeRequest
import io.klaytn.finder.infra.web.model.ScopePage
import io.klaytn.finder.infra.web.model.SimplePageRequest
import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.klaytn.finder.interfaces.rest.api.view.mapper.*
import io.klaytn.finder.interfaces.rest.api.view.model.nft.NftTransferListView
import io.klaytn.finder.service.BlockRangeIntervalType
import io.klaytn.finder.service.BlockRangeService
import io.klaytn.finder.service.nft.NftInventoryRefreshRequestService
import io.klaytn.finder.service.nft.NftService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@Profile(ServerMode.API_MODE)
@RestController
@Tag(name= SwaggerConstant.TAG_PUBLIC)
class NftController(
    val nftService: NftService,
    val nftInventoryRefreshRequestService: NftInventoryRefreshRequestService,
    val blockRangeService: BlockRangeService,
    val contractToNftItemViewMapper: ContractToNftItemViewMapper,
    val contractToNftListViewMapper: ContractToNftListViewMapper,
    val nftTransferToListViewMapper: NftTransferToListViewMapper,
    val nftTokenHolderToListViewMapper: NftHolderToListViewMapper,
    val nftInventoryToListViewMapper: NftInventoryToListViewMapper,
    val nftTokenItemToViewMapper: NftTokenItemToViewMapper,
    val nftBurnToListViewMapper: NftBurnToListViewMapper
) {
    @Operation(
        description = "Retrieve a list of NFTs.",
    )
    @GetMapping("/api/v1/nfts")
    fun getNfts(@Valid simplePageRequest: SimplePageRequest) =
        ScopePage.of(nftService.getVerifiedNfts(simplePageRequest), contractToNftListViewMapper)

    @Operation(
        description = "Retrieve an NFT.",
        parameters = [
            Parameter(name = "nftAddress", description = "nft address", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/nfts/{nftAddress}")
    fun getNft(@PathVariable nftAddress: String) =
        nftService.getNft(nftAddress)?.let { contractToNftItemViewMapper.transform(it) } ?: throw NotFoundNftException()
    @Operation(
        description = "Retrieve the transfer list of an NFT.",
        parameters = [
            Parameter(name = "nftAddress", description = "nft address", `in` = ParameterIn.PATH),
            Parameter(name = "tokenId", description = "token id", `in` = ParameterIn.QUERY),
        ]
    )
    @GetMapping("/api/v1/nfts/{nftAddress}/transfers")
    fun getTransfers(
        @PathVariable nftAddress: String,
        @Valid blockRangeRequest: BlockRangeRequest? = null,
        @RequestParam(name = "tokenId", required = false) tokenId: String?,
        @Valid simplePageRequest: SimplePageRequest,
    ): ScopePage<NftTransferListView> {
        val blockRangeIntervalType = BlockRangeIntervalType.NFT_TRANSFER
        val blockRange = blockRangeService.getBlockRange(blockRangeRequest, blockRangeIntervalType)

        return ScopePage.of(
            nftService.getNftTransfersByNftAddressAndTokenId(nftAddress, blockRange, tokenId, simplePageRequest),
            nftTransferToListViewMapper,
            blockRangeService.getBlockRangeCondition(blockRange, blockRangeIntervalType)
        )
    }


    @Operation(
        description = "Retrieve the holder list of an NFT.",
        parameters = [
            Parameter(name = "nftAddress", description = "nft address", `in` = ParameterIn.PATH),
            Parameter(name = "tokenId", description = "token id", `in` = ParameterIn.QUERY),
        ]
    )
    @GetMapping("/api/v1/nfts/{nftAddress}/holders")
    fun getHolders(
        @PathVariable nftAddress: String,
        @RequestParam(name = "tokenId", required = false) tokenId: String?,
        @Valid simplePageRequest: SimplePageRequest,
    ) =
        ScopePage.of(nftService.getNftHoldersByNftAddressAndTokenId(nftAddress, tokenId, simplePageRequest),
            nftTokenHolderToListViewMapper)

    @Operation(
        description = "Retrieve the inventory list of an NFT.",
        parameters = [
            Parameter(name = "nftAddress", description = "nft address", `in` = ParameterIn.PATH),
            Parameter(name = "keyword", description = "accountAddress or tokenId", `in` = ParameterIn.QUERY),
        ]
    )
    @GetMapping("/api/v1/nfts/{nftAddress}/inventories")
    fun getKip17Inventories(
        @PathVariable nftAddress: String,
        keyword: String?,
        @Valid simplePageRequest: SimplePageRequest,
    ) =
        ScopePage.of(
            nftService.getNftInventories(nftAddress, keyword, simplePageRequest),
            nftInventoryToListViewMapper
        )

    @Operation(
        description = "Retrieve the token item of an NFT.",
        parameters = [
            Parameter(name = "nftAddress", description = "nft address", `in` = ParameterIn.PATH),
            Parameter(name = "tokenId", description = "token id", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/nfts/{nftAddress}/inventories/{tokenId}")
    fun getNftItem(@PathVariable nftAddress: String, @PathVariable tokenId: String) =
        nftService.getNftTokenItem2(nftAddress, tokenId)?.let { nftTokenItemToViewMapper.transform(it) }
            ?: throw NotFoundNftTokenItemException()

    @Operation(
        description = "Request an update of the inventory token URI for an NFT.",
        parameters = [
            Parameter(name = "nftAddress", description = "nft address", `in` = ParameterIn.PATH),
            Parameter(name = "tokenId", description = "tokenId", `in` = ParameterIn.PATH),
        ]
    )
    @PutMapping("/api/v1/nfts/{nftAddress}/inventories/{tokenId}/refresh")
    fun refreshNftItem(@PathVariable nftAddress: String, @PathVariable tokenId: String) =
        SimpleResponse(nftInventoryRefreshRequestService.refreshNftTokenUri(nftAddress, tokenId))

    @Operation(
        description = "Retrieve a list of burned NFTs.",
        parameters = [
            Parameter(name = "nftAddress", description = "nft address", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/nfts/{nftAddress}/burns")
    fun getBurns(
        @PathVariable nftAddress: String,
        @RequestParam(name = "tokenId", required = false) tokenId: String?,
        @Valid simplePageRequest: SimplePageRequest
    ) =
        ScopePage.of(
            nftService.getBurnsByNftAddressAndTokenId(nftAddress, tokenId, simplePageRequest),
            nftBurnToListViewMapper
        )

    // -- --------------------------------------------------------------------------------------------------------------
    // -- deprecate
    // -- --------------------------------------------------------------------------------------------------------------

    /**
     * To be replaced by getNftItem
     */
    @Operation(
        deprecated = true,
        description = "Retrieve the tokenItem of the NFT.",
        parameters = [
            Parameter(name = "nftAddress", description = "nft address", `in` = ParameterIn.PATH),
            Parameter(name = "tokenId", description = "token id", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/nfts/{nftAddress}/tokenids/{tokenId}")
    fun getNftTokenItem(@PathVariable nftAddress: String, @PathVariable tokenId: String) =
        nftService.getNftTokenItem(nftAddress, tokenId)?.let { nftTokenItemToViewMapper.transform(it) }
            ?: throw NotFoundNftTokenItemException()
}
