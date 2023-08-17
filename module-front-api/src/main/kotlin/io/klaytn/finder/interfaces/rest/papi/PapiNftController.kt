package io.klaytn.finder.interfaces.rest.papi

import io.klaytn.commons.model.response.SimpleResponse
import io.klaytn.commons.utils.logback.logger
import io.klaytn.finder.infra.ServerMode
import io.klaytn.finder.infra.exception.NotFoundContractException
import io.klaytn.finder.infra.exception.NotFoundNftException
import io.klaytn.finder.infra.exception.NotFoundNftTokenItemException
import io.klaytn.finder.infra.web.model.BlockRangeRequest
import io.klaytn.finder.infra.web.model.PapiSimplePageRequest
import io.klaytn.finder.infra.web.model.ScopePage
import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.klaytn.finder.interfaces.rest.api.view.mapper.*
import io.klaytn.finder.interfaces.rest.api.view.model.nft.NftTransferListView
import io.klaytn.finder.service.BlockRangeIntervalType
import io.klaytn.finder.service.BlockRangeService
import io.klaytn.finder.service.ContractService
import io.klaytn.finder.service.nft.NftInventoryContentService
import io.klaytn.finder.service.nft.NftInventoryService
import io.klaytn.finder.service.nft.NftService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@Profile(ServerMode.PRIVATE_API_MODE)
@RestController
@Tag(name= SwaggerConstant.TAG_PRIVATE)
class PapiNftController(
    val nftService: NftService,
    val nftInventoryService: NftInventoryService,
    val contractService: ContractService,
    val blockRangeService: BlockRangeService,
    val contractToNftItemViewMapper: ContractToNftItemViewMapper,
    val nftTransferToListViewMapper: NftTransferToListViewMapper,
    val nftTokenHolderToListViewMapper: NftHolderToListViewMapper,
    val nftInventoryToListViewMapper: NftInventoryToListViewMapper,
    val nftTokenItemToViewMapper: NftTokenItemToViewMapper,
    val nftInventoryContentService: NftInventoryContentService,
) {
    private val logger = logger(this::class.java)

    @Operation(
        description = "Retrieve a specific NFT.",
        parameters = [
            Parameter(name = "nftAddress", description = "nft address", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/papi/v1/nfts/{nftAddress}")
    fun getNft(@PathVariable nftAddress: String) =
        nftService.getNft(nftAddress)?.let { contractToNftItemViewMapper.transform(it) } ?: throw NotFoundNftException()

    @Operation(
        description = "Retrieve a specific tokenItem of a specific NFT.",
        parameters = [
            Parameter(name = "nftAddress", description = "nft address", `in` = ParameterIn.PATH),
            Parameter(name = "tokenId", description = "token id", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/papi/v1/nfts/{nftAddress}/tokenids/{tokenId}")
    fun getNftTokenItem(@PathVariable nftAddress: String, @PathVariable tokenId: String) =
        nftService.getNftTokenItem(nftAddress, tokenId)?.let { nftTokenItemToViewMapper.transform(it) }
            ?: throw NotFoundNftTokenItemException()

    @Operation(
        description = "Retrieve the transfer list of a specific NFT.",
        parameters = [
            Parameter(name = "nftAddress", description = "nft address", `in` = ParameterIn.PATH),
            Parameter(name = "tokenId", description = "token id", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/papi/v1/nfts/{nftAddress}/transfers")
    fun getTransfers(
        @PathVariable nftAddress: String,
        @Valid blockRangeRequest: BlockRangeRequest? = null,
        @RequestParam(name = "tokenId", required = false) tokenId: String?,
        @Valid papiSimplePageRequest: PapiSimplePageRequest,
    ): ScopePage<NftTransferListView> {
        val blockRangeIntervalType = BlockRangeIntervalType.NFT_TRANSFER
        val blockRange = blockRangeService.getBlockRange(blockRangeRequest, blockRangeIntervalType)

        return ScopePage.of(
            nftService.getNftTransfersByNftAddressAndTokenId(
                nftAddress, blockRange, tokenId, papiSimplePageRequest.toSimplePageRequest()),
            nftTransferToListViewMapper,
            blockRangeService.getBlockRangeCondition(blockRange, blockRangeIntervalType)
        )
    }

    @Operation(
        description = "Retrieve the holder list of a specific NFT.",
        parameters = [
            Parameter(name = "nftAddress", description = "nft address", `in` = ParameterIn.PATH),
            Parameter(name = "tokenId", description = "token id (only kip37)", `in` = ParameterIn.QUERY),
        ]
    )
    @GetMapping("/papi/v1/nfts/{nftAddress}/holders")
    fun getHolders(
        @PathVariable nftAddress: String,
        @RequestParam(name = "tokenId", required = false) tokenId: String?,
        @Valid papiSimplePageRequest: PapiSimplePageRequest,
    ) =
        ScopePage.of(nftService.getNftHoldersByNftAddressAndTokenId(nftAddress,
            tokenId,
            papiSimplePageRequest.toSimplePageRequest()),
            nftTokenHolderToListViewMapper)

    @Operation(
        description = "Retrieve the inventory list of a specific NFT.",
        parameters = [
            Parameter(name = "nftAddress", description = "nft address", `in` = ParameterIn.PATH),
            Parameter(name = "keyword", description = "accountAddress or tokenId", `in` = ParameterIn.QUERY),
        ]
    )
    @GetMapping("/papi/v1/nfts/{nftAddress}/inventories")
    fun getNftInventories(
        @PathVariable nftAddress: String,
        keyword: String?,
        @Valid papiSimplePageRequest: PapiSimplePageRequest,
    ) =
        ScopePage.of(
            nftService.getNftInventories(nftAddress, keyword, papiSimplePageRequest.toSimplePageRequest()),
            nftInventoryToListViewMapper
        )

    @GetMapping(
        path = ["/papi/v1/nfts/{nftAddress}/inventories/{tokenId}/contents"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getNftInventoryContent(
        @PathVariable nftAddress: String,
        @PathVariable tokenId: String
    ) = nftInventoryContentService.getNftInventoryContent(nftAddress, tokenId)

    @PutMapping("/papi/v1/nfts/{nftAddress}/inventories/{tokenId}/refresh")
    fun refreshNftItem(
        @PathVariable nftAddress: String,
        @PathVariable tokenId: String,
        @RequestParam(required = false, defaultValue = "100") batchSize: Int = 100,
    ): SimpleResponse<Boolean> {
        try {
            val contract = contractService.getContract(nftAddress) ?: throw NotFoundContractException()
            val nftTokenUri = nftInventoryService.getNftTokenUri(contract, tokenId, null)
            return SimpleResponse(nftInventoryService.refreshNftTokenUri(nftAddress, tokenId, nftTokenUri, batchSize))
        } catch (dataIntegrityViolationException: DataIntegrityViolationException) {
            logger.error(
                "[NFT($nftAddress / $tokenId)] fail to refresh. caused by ${dataIntegrityViolationException.message}")
        }
        return SimpleResponse(false)
    }
}
