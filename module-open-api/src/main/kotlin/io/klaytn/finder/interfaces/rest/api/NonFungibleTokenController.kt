package io.klaytn.finder.interfaces.rest.api

import io.klaytn.commons.model.response.SimpleResponse
import io.klaytn.finder.infra.exception.NotFoundNftException
import io.klaytn.finder.infra.exception.NotFoundNftTokenItemException
import io.klaytn.finder.infra.web.model.BlockRangeRequest
import io.klaytn.finder.infra.web.model.ScopePage
import io.klaytn.finder.infra.web.model.SimplePageRequest
import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.klaytn.finder.interfaces.rest.api.view.mapper.nft.*
import io.klaytn.finder.interfaces.rest.api.view.model.nft.NftTransferListView
import io.klaytn.finder.service.BlockRangeIntervalType
import io.klaytn.finder.service.BlockRangeService
import io.klaytn.finder.service.nft.NftInventoryRefreshRequestService
import io.klaytn.finder.service.nft.NftService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@Tag(name = SwaggerConstant.TAG_PUBLIC)
class NonFungibleTokenController(
    private val nftService: NftService,
    private val nftInventoryRefreshRequestService: NftInventoryRefreshRequestService,
    private val blockRangeService: BlockRangeService,
    private val contractToNftViewMapper: ContractToNftViewMapper,
    private val nftTransferToListViewMapper: NftTransferToListViewMapper,
    private val nftTokenHolderToListViewMapper: NftHolderToListViewMapper,
    private val nftInventoryToListViewMapper: NftInventoryToListViewMapper,
    private val nftTokenItemToViewMapper: NftTokenItemToViewMapper,
) {
    @Operation(
        description = "Retrieve non-fungible-token.",
        parameters = [
            Parameter(name = "tokenAddress", description = "token address", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/nfts/{tokenAddress}")
    fun getNonFungibleToken(
        @PathVariable tokenAddress: String
    ) = nftService.getNft(tokenAddress)?.let { contractToNftViewMapper.transform(it) } ?: throw NotFoundNftException()

    @Operation(
        description = "Retrieve the list of holders for a non-fungible-token.",
        parameters = [
            Parameter(name = "tokenAddress", description = "token address", `in` = ParameterIn.PATH),
            Parameter(name = "tokenId", description = "token id", `in` = ParameterIn.QUERY),
        ]
    )
    @GetMapping("/api/v1/nfts/{tokenAddress}/holders")
    fun getHoldersOfNonFungibleToken(
        @PathVariable tokenAddress: String,
        @RequestParam(name = "tokenId", required = false) tokenId: String?,
        @Valid simplePageRequest: SimplePageRequest
    ) =
        ScopePage.of(nftService.getNftHoldersByNftAddressAndTokenId(tokenAddress, tokenId, simplePageRequest),
            nftTokenHolderToListViewMapper)

    @Operation(
        description = "Retrieve the list of transfers for a non-fungible-token.",
        parameters = [
            Parameter(name = "tokenAddress", description = "token address", `in` = ParameterIn.PATH),
            Parameter(name = "tokenId", description = "token id", `in` = ParameterIn.QUERY),
        ]
    )
    @GetMapping("/api/v1/nfts/{tokenAddress}/transfers")
    fun getTransfersOfNonFungibleToken(
        @PathVariable tokenAddress: String,
        @Valid blockRangeRequest: BlockRangeRequest? = null,
        @RequestParam(name = "tokenId", required = false) tokenId: String?,
        @Valid simplePageRequest: SimplePageRequest
    ): ScopePage<NftTransferListView> {
        val blockRangeIntervalType = BlockRangeIntervalType.NFT_TRANSFER
        val blockRange = blockRangeService.getBlockRange(blockRangeRequest, blockRangeIntervalType)

        return ScopePage.of(
            nftService.getNftTransfersByNftAddressAndTokenId(tokenAddress, blockRange, tokenId, simplePageRequest),
            nftTransferToListViewMapper,
            blockRangeService.getBlockRangeCondition(blockRange, blockRangeIntervalType)
        )
    }

    @Operation(
        description = "Retrieve the list of inventory for a non-fungible-token.",
        parameters = [
            Parameter(name = "tokenAddress", description = "token address", `in` = ParameterIn.PATH),
            Parameter(name = "keyword", description = "accountAddress or tokenId", `in` = ParameterIn.QUERY),
        ]
    )
    @GetMapping("/api/v1/nfts/{tokenAddress}/inventories")
    fun getInventoriesOfNonFungibleToken(
        @PathVariable tokenAddress: String,
        keyword: String?,
        @Valid simplePageRequest: SimplePageRequest,
    ) =
        ScopePage.of(
            nftService.getNftInventories(tokenAddress, keyword, simplePageRequest),
            nftInventoryToListViewMapper
        )

    @Operation(
        description = "Retrieve the tokenId for a non-fungible-token.",
        parameters = [
            Parameter(name = "tokenAddress", description = "token address", `in` = ParameterIn.PATH),
            Parameter(name = "tokenId", description = "token id", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/nfts/{tokenAddress}/inventories/{tokenId}")
    fun getNftItem(
        @PathVariable tokenAddress: String,
        @PathVariable tokenId: String
    ) =
        nftService.getNftTokenItem(tokenAddress, tokenId)?.let { nftTokenItemToViewMapper.transform(it) }
            ?: throw NotFoundNftTokenItemException()

    @Operation(
        description = "Request to update the token-uri for a non-fungible-token in the inventory.",
        parameters = [
            Parameter(name = "tokenAddress", description = "tokenAddress address", `in` = ParameterIn.PATH),
            Parameter(name = "tokenId", description = "tokenId", `in` = ParameterIn.PATH),
        ]
    )
    @PutMapping("/api/v1/nfts/{tokenAddress}/inventories/{tokenId}/refresh")
    fun refreshNftItem(
        @PathVariable tokenAddress: String,
        @PathVariable tokenId: String
    ) = SimpleResponse(nftInventoryRefreshRequestService.refreshNftTokenUri(tokenAddress, tokenId))
}
