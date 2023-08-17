package io.klaytn.finder.interfaces.rest.api.view.mapper

import io.klaytn.commons.model.mapper.ListMapper
import io.klaytn.commons.model.mapper.Mapper
import io.klaytn.finder.domain.mysql.set1.Contract
import io.klaytn.finder.domain.mysql.set3.nft.NftBurn
import io.klaytn.finder.domain.mysql.set3.nft.NftInventory
import io.klaytn.finder.domain.mysql.set3.nft.NftTransfer
import io.klaytn.finder.infra.utils.DateUtils
import io.klaytn.finder.infra.utils.applyDecimal
import io.klaytn.finder.interfaces.rest.api.view.model.account.AccountNftBalanceListView
import io.klaytn.finder.interfaces.rest.api.view.model.contract.ContractSummary
import io.klaytn.finder.interfaces.rest.api.view.model.nft.*
import io.klaytn.finder.service.ContractService
import io.klaytn.finder.service.caver.CaverContractService
import io.klaytn.finder.service.nft.NftHolder
import io.klaytn.finder.service.nft.NftInventoryRefreshRequestService
import io.klaytn.finder.service.nft.NftService
import io.klaytn.finder.service.nft.NftTokenItem
import io.klaytn.finder.view.mapper.AccountAddressToViewMapper
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode

@Component
class ContractToNftItemViewMapper(
    private val caverContractService: CaverContractService,
) : Mapper<Contract, NftItemView> {
    override fun transform(source: Contract): NftItemView {
        val caverTotalSupply = caverContractService.getTotalSupply(source.contractType, source.contractAddress)
        val totalSupply = caverTotalSupply?.toBigDecimal() ?: source.totalSupply
        val burnAmount = source.burnAmount?.toBigDecimal()?.applyDecimal(source.decimal)

        return NftItemView(
            info = ContractSummary.of(source)!!,
            type = source.contractType,
            totalSupply = totalSupply.applyDecimal(source.decimal),
            totalTransfers = source.totalTransfer,
            officialSite = source.officialSite,
            holderCount = source.holderCount,
            burnAmount = burnAmount,
            totalBurns = source.totalBurn
        )
    }
}

@Component
class ContractToNftListViewMapper : Mapper<Contract, NftListView> {
    override fun transform(source: Contract): NftListView {
        return NftListView(
            info = ContractSummary.of(source)!!,
            totalSupply = source.totalSupply.applyDecimal(source.decimal),
            totalTransfers = source.totalTransfer
        )
    }
}

@Component
class NftHolderToAccountNftBalanceListViewMapper(
    private val contractService: ContractService,
) : ListMapper<NftHolder, AccountNftBalanceListView> {
    override fun transform(source: List<NftHolder>): List<AccountNftBalanceListView> {
        val contractMap = contractService.getContractMap(source.map { it.contractAddress }.toSet())

        return source.map { nftHolder ->
            val contract = contractMap[nftHolder.contractAddress]
            AccountNftBalanceListView(
                contractType = nftHolder.contractType,
                info = ContractSummary.of(nftHolder.contractAddress, contract),
                tokenId = nftHolder.tokenId,
                tokenCount = nftHolder.tokenCount,
                latestTransaction = DateUtils.from(nftHolder.lastTransactionTime)
            )
        }
    }
}

@Component
class NftTransferToListViewMapper(
    private val accountAddressToViewMapper: AccountAddressToViewMapper,
    private val contractService: ContractService,
) : ListMapper<NftTransfer, NftTransferListView> {
    override fun transform(source: List<NftTransfer>): List<NftTransferListView> {
        val contractMap = contractService.getContractMap(source.map { it.contractAddress }.toSet())

        return source.map { nftTransfer ->
            val contract = contractMap[nftTransfer.contractAddress]
            NftTransferListView(
                blockId = nftTransfer.blockNumber,
                transactionHash = nftTransfer.transactionHash,
                datetime = DateUtils.from(nftTransfer.timestamp),
                from = accountAddressToViewMapper.transform(nftTransfer.from)!!,
                to = accountAddressToViewMapper.transform(nftTransfer.to)!!,
                nft = ContractSummary.of(nftTransfer.contractAddress, contract),
                tokenId = nftTransfer.tokenId,
                tokenCount = nftTransfer.tokenCount
            )
        }
    }
}

@Component
class NftHolderToListViewMapper(
    private val accountAddressToViewMapper: AccountAddressToViewMapper,
    private val caverContractService: CaverContractService,
    private val nftService: NftService,
) : Mapper<NftHolder, NftHolderListView> {
    override fun transform(source: NftHolder): NftHolderListView {
        val totalSupply =
            if (source.tokenId.isNullOrBlank()) {
                nftService.getNft(source.contractAddress)?.let {
                    it.totalSupply.applyDecimal(it.decimal)
                } ?: BigDecimal.ZERO
            } else {
                caverContractService.getTotalSupplyOfTokenId(source.contractAddress, source.tokenId!!).toBigDecimal()
            }
        val percentage =
            if (totalSupply > BigDecimal.ZERO) {
                BigDecimal(source.tokenCount).multiply(BigDecimal(100)).divide(totalSupply, 4, RoundingMode.HALF_UP)
            } else {
                BigDecimal.ZERO
            }

        return NftHolderListView(
            contractType = source.contractType,
            holder = accountAddressToViewMapper.transform(source.holderAddress)!!,
            tokenCount = source.tokenCount,
            percentage = percentage,
            tokenId = source.tokenId
        )
    }
}

@Component
class NftInventoryToListViewMapper(
    private val accountAddressToViewMapper: AccountAddressToViewMapper,
) : Mapper<NftInventory, NftInventoryListView> {
    override fun transform(source: NftInventory): NftInventoryListView {
        return NftInventoryListView(
            contractType = source.contractType,
            tokenId = source.tokenId,
            holder = accountAddressToViewMapper.transform(source.holderAddress)!!,
            tokenUri = source.tokenUri,
            tokenCount = source.tokenCount,
            updatedAt = DateUtils.localDateTimeToDate(source.updatedAt!!)
        )
    }
}

@Component
class NftTokenItemToViewMapper(
    private val accountAddressToViewMapper: AccountAddressToViewMapper,
    private val contractService: ContractService,
    private val nftInventoryRefreshRequestService: NftInventoryRefreshRequestService,
) : Mapper<NftTokenItem, NftTokenItemView> {
    override fun transform(source: NftTokenItem): NftTokenItemView {
        val contract = contractService.getContract(source.contractAddress)
        val tokenUriRefreshable = !nftInventoryRefreshRequestService.existsRefreshNftTokenUriLimiter(
            source.contractAddress, source.tokenId)

        return NftTokenItemView(
            contractType = source.contractType,
            info = ContractSummary.of(source.contractAddress, contract),
            tokenId = source.tokenId,
            tokenUri = source.tokenUri,
            tokenUriUpdatedAt = DateUtils.localDateTimeToDate(source.tokenUriUpdatedAt),
            holder = accountAddressToViewMapper.transform(source.holderAddress),
            totalSupply = source.totalSupply,
            totalTransfer = source.totalTransfer,
            burnAmount = source.burnAmount,
            totalBurn = source.totalBurn,
            tokenUriRefreshable = tokenUriRefreshable
        )
    }
}

@Component
class NftBurnToListViewMapper(
    private val accountAddressToViewMapper: AccountAddressToViewMapper,
    private val contractService: ContractService,
) : ListMapper<NftBurn, NftBurnListView> {
    override fun transform(source: List<NftBurn>): List<NftBurnListView> {
        val contractMap = contractService.getContractMap(source.map { it.contractAddress }.toSet())

        return source.map { nftBurn ->
            val contract = contractMap[nftBurn.contractAddress]
            NftBurnListView(
                blockId = nftBurn.blockNumber,
                transactionHash = nftBurn.transactionHash,
                datetime = DateUtils.from(nftBurn.timestamp),
                from = accountAddressToViewMapper.transform(nftBurn.from)!!,
                to = accountAddressToViewMapper.transform(nftBurn.to)!!,
                nft = ContractSummary.of(nftBurn.contractAddress, contract),
                tokenId = nftBurn.tokenId,
                tokenCount = nftBurn.tokenCount
            )
        }
    }
}