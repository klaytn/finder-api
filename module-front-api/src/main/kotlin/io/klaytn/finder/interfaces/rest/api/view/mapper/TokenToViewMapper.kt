package io.klaytn.finder.interfaces.rest.api.view.mapper

import io.klaytn.commons.model.mapper.ListMapper
import io.klaytn.commons.model.mapper.Mapper
import io.klaytn.finder.domain.mysql.set1.Contract
import io.klaytn.finder.domain.mysql.set3.token.TokenBurn
import io.klaytn.finder.domain.mysql.set3.token.TokenHolder
import io.klaytn.finder.domain.mysql.set3.token.TokenTransfer
import io.klaytn.finder.infra.utils.DateUtils
import io.klaytn.finder.infra.utils.applyDecimal
import io.klaytn.finder.interfaces.rest.api.view.model.contract.ContractSummary
import io.klaytn.finder.interfaces.rest.api.view.model.token.*
import io.klaytn.finder.service.ContractService
import io.klaytn.finder.service.caver.CaverContractService
import io.klaytn.finder.view.mapper.AccountAddressToViewMapper
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode

@Component
class ContractToTokenItemViewMapper(
    private val caverContractService: CaverContractService,
) : Mapper<Contract, TokenItemView> {
    override fun transform(source: Contract): TokenItemView {
        val caverTotalSupply = caverContractService.getTotalSupply(source.contractType, source.contractAddress)
        val totalSupply = caverTotalSupply?.toBigDecimal() ?: source.totalSupply
        val burnAmount = source.burnAmount?.toBigDecimal()?.applyDecimal(source.decimal)

        return TokenItemView(
            info = ContractSummary.of(source)!!,
            type = source.contractType,
            totalSupply = totalSupply.applyDecimal(source.decimal),
            totalTransfers = source.totalTransfer,
            officialSite = source.officialSite,
            burnAmount = burnAmount,
            totalBurns = source.totalBurn
        )
    }
}

@Component
class ContractToTokenListViewMapper : Mapper<Contract, TokenListView> {
    override fun transform(source: Contract): TokenListView {
        return TokenListView(
            info = ContractSummary.of(source)!!,
            totalSupply = source.totalSupply.applyDecimal(source.decimal),
            totalTransfers = source.totalTransfer,
            totalBurns = source.totalBurn ?: 0L,
            burnAmount = source.burnAmount?.toBigDecimal()?.applyDecimal(source.decimal) ?: BigDecimal.ZERO
        )
    }
}

@Component
class TokenHolderToListViewMapper(
    private val contractService: ContractService,
    private val accountAddressToViewMapper: AccountAddressToViewMapper,
) : ListMapper<TokenHolder, TokenHolderListView> {
    override fun transform(source: List<TokenHolder>): List<TokenHolderListView> {
        val contractMap = contractService.getContractMap(source.map { it.contractAddress }.toSet())

        return source.map { tokenHolder ->
            val contract = contractMap[tokenHolder.contractAddress]
            val totalSupply = contract?.let { it.totalSupply.applyDecimal(it.decimal) }
                ?: BigDecimal.ZERO
            val amount = contract?.let { tokenHolder.amount.toBigDecimal().applyDecimal(it.decimal) }
                ?: tokenHolder.amount.toBigDecimal()

            val percentage =
                if (totalSupply > BigDecimal.ZERO) {
                    amount.multiply(BigDecimal(100)).divide(totalSupply, 4, RoundingMode.HALF_UP)
                } else {
                    BigDecimal.ZERO
                }

            TokenHolderListView(
                holder = accountAddressToViewMapper.transform(tokenHolder.holderAddress)!!,
                amount = amount,
                percentage = percentage
            )
        }
    }
}

@Component
class TokenHolderToTokenBalanceListViewMapper(
    private val contractService: ContractService,
) : ListMapper<TokenHolder, TokenBalanceListView> {
    override fun transform(source: List<TokenHolder>): List<TokenBalanceListView> {
        val contractMap = contractService.getContractMap(source.map { it.contractAddress }.toSet())

        return source.map { tokenHolder ->
            val contract = contractMap[tokenHolder.contractAddress]
            val amount = contract?.let { tokenHolder.amount.toBigDecimal().applyDecimal(it.decimal) }
                ?: tokenHolder.amount.toBigDecimal()

            TokenBalanceListView(
                info = ContractSummary.of(tokenHolder.contractAddress, contract),
                balance = amount,
                latestTransactionDateTime = DateUtils.from(tokenHolder.lastTransactionTime)
            )
        }
    }
}

@Component
class TokenTransferToListViewMapper(
    private val accountAddressToViewMapper: AccountAddressToViewMapper,
    private val contractService: ContractService,
) : ListMapper<TokenTransfer, TokenTransferListView> {
    override fun transform(source: List<TokenTransfer>): List<TokenTransferListView> {
        val contractMap = contractService.getContractMap(source.map { it.contractAddress }.toSet())

        return source.map { tokenTransfer ->
            val contract = contractMap[tokenTransfer.contractAddress]
            val contractSummary = ContractSummary.of(tokenTransfer.contractAddress, contract)
            val amount = contract?.let { tokenTransfer.amount.toBigDecimal().applyDecimal(it.decimal) }
                ?: tokenTransfer.amount.toBigDecimal()

            TokenTransferListView(
                blockId = tokenTransfer.blockNumber,
                transactionHash = tokenTransfer.transactionHash,
                datetime = DateUtils.from(tokenTransfer.timestamp),
                from = accountAddressToViewMapper.transform(tokenTransfer.from)!!,
                to = accountAddressToViewMapper.transform(tokenTransfer.to)!!,
                token = contractSummary,
                amount = amount
            )
        }
    }
}

@Component
class TokenBurnToListViewMapper(
    private val accountAddressToViewMapper: AccountAddressToViewMapper,
    private val contractService: ContractService,
) : ListMapper<TokenBurn, TokenBurnListView> {
    override fun transform(source: List<TokenBurn>): List<TokenBurnListView> {
        val contractMap = contractService.getContractMap(source.map { it.contractAddress }.toSet())
        return source.map { tokenBurn ->
            val contract = contractMap[tokenBurn.contractAddress]
            val contractSummary = ContractSummary.of(tokenBurn.contractAddress, contract)
            val amount = contract?.let { tokenBurn.amount.toBigDecimal().applyDecimal(it.decimal) }
                ?: tokenBurn.amount.toBigDecimal()

            TokenBurnListView(
                blockId = tokenBurn.blockNumber,
                transactionHash = tokenBurn.transactionHash,
                datetime = DateUtils.from(tokenBurn.timestamp),
                from = accountAddressToViewMapper.transform(tokenBurn.from)!!,
                to = accountAddressToViewMapper.transform(tokenBurn.to)!!,
                token = contractSummary,
                amount = amount
            )
        }
    }
}
