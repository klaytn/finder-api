package io.klaytn.finder.interfaces.rest.api.view.mapper

import io.klaytn.commons.model.mapper.ListMapper
import io.klaytn.commons.model.mapper.Mapper
import io.klaytn.finder.config.dynamic.FinderServerFeatureConfig
import io.klaytn.finder.domain.mysql.set1.Account
import io.klaytn.finder.domain.mysql.set1.approve.AccountNftApprove
import io.klaytn.finder.domain.mysql.set1.approve.AccountTokenApprove
import io.klaytn.finder.infra.utils.DateUtils
import io.klaytn.finder.infra.utils.applyDecimal
import io.klaytn.finder.interfaces.rest.api.view.model.account.*
import io.klaytn.finder.interfaces.rest.api.view.model.contract.ContractSummary
import io.klaytn.finder.service.*
import io.klaytn.finder.service.accountkey.KlaytnAccountKeyService
import io.klaytn.finder.service.caver.CaverAccountService
import io.klaytn.finder.service.governancecouncil.GovernanceCouncilService
import io.klaytn.finder.view.mapper.AccountAddressToViewMapper
import io.klaytn.finder.view.mapper.AccountKeyToViewMapper
import org.springframework.stereotype.Component

@Component
class AccountToItemViewMapper(
    val accountAddressService: AccountAddressService,
    val accountTagService: AccountTagService,
    val contractService: ContractService,
    val caverAccountService: CaverAccountService,
    val accountRelatedInfoChecker: AccountRelatedInfoChecker,
    val governanceCouncilService: GovernanceCouncilService,
    val klaytnAccountKeyService: KlaytnAccountKeyService,
    val accountAddressToViewMapper: AccountAddressToViewMapper,
    val governanceCouncilToViewMapper: GovernanceCouncilToViewMapper,
    val accountKeyToViewMapper: AccountKeyToViewMapper,
    val finderServerFeatureConfig: FinderServerFeatureConfig
) : Mapper<Account, AccountItemView> {
    override fun transform(source: Account): AccountItemView {
        val balance = caverAccountService.getAccountBalance(source.address).applyDecimal(18)
        val transactionCount = caverAccountService.getTransactionCount(source.address)
        val accountKey = klaytnAccountKeyService.getKlaytnAccountKeyByAccountAddress(source.address)?.let {
            accountKeyToViewMapper.transform(it)
        }
        val contract = contractService.getContract(source.address)

        val accountAddressView =
            accountAddressService.getAccountAddress(source.contractCreatorAddress)?.let {
                accountAddressToViewMapper.transform(it)
            }
        val accountTags = accountTagService.getSortedTags(source.tags)
        val associatedInfos = accountRelatedInfoChecker.get(source)

        val governanceCouncilView =
            if(finderServerFeatureConfig.governanceCouncil) {
                governanceCouncilService.getGovernanceCouncil(source.address)?.let {
                    governanceCouncilToViewMapper.transform(it)
                }
            } else {
                null
            }

        return AccountItemView(
            address = source.address,
            accountType = source.accountType,
            balance = balance,
            totalTransactionCount = transactionCount.toLong(),
            contractType = source.contractType,
            info = ContractSummary.of(contract),
            contractCreatorAddress = source.contractCreatorAddress,
            contractCreator = accountAddressView,
            contractCreatorTransactionHash = source.contractCreatorTransactionHash,
            contractCreated = if(contract?.txError == true) false else null,
            knsDomain = source.knsDomain,
            addressLabel = source.addressLabel,
            tags = accountTags,
            associatedInfos = associatedInfos,
            governanceCouncil = governanceCouncilView,
            accountKey = accountKey
        )
    }
}

@Component
class AccountToAccountListViewMapper(
    val contractService: ContractService,
    val accountTagService: AccountTagService,
) : ListMapper<Account, AccountListView> {
    override fun transform(source: List<Account>): List<AccountListView> {
        val contractMap = contractService.getContractMap(source.map { it.address }.toSet())

        return source.map { account ->
            val contract = contractMap[account.address]
            val accountTags = accountTagService.getSortedTags(account.tags)
            AccountListView(
                address = account.address,
                accountType = account.accountType,
                contractType = account.contractType,
                info = ContractSummary.of(contract),
                knsDomain = account.knsDomain,
                addressLabel = account.addressLabel,
                tags = accountTags,
            )
        }
    }
}

@Component
class AccountTokenApproveToListViewMapper(
    val accountAddressToViewMapper: AccountAddressToViewMapper,
    val blockService: BlockService,
    val contractService: ContractService,
) : ListMapper<AccountTokenApprove, AccountTokenApproveListView> {
    override fun transform(source: List<AccountTokenApprove>): List<AccountTokenApproveListView> {
        val blockMap = blockService.getBlocks(source.map { it.blockNumber }).associateBy { it.number }
        val contractMap = contractService.getContractMap(source.map { it.contractAddress }.toSet())

        return source.map { approve ->
            val block = blockMap[approve.blockNumber]!!
            val contract = contractMap[approve.contractAddress]

            val contractSummary = ContractSummary.of(approve.contractAddress, contract)
            val approvedAmount = contract?.let {
                approve.approvedAmount.toBigDecimal().applyDecimal(it.decimal)
            } ?: approve.approvedAmount.toBigDecimal()
            val spenderAccount = accountAddressToViewMapper.transform(approve.spenderAddress)!!
            val timestamp = if(approve.timestamp > 0) approve.timestamp else block.timestamp

            AccountTokenApproveListView(
                blockNumber = approve.blockNumber,
                transactionHash = approve.transactionHash,
                contractSummary = contractSummary,
                spenderAccount = spenderAccount,
                approvedAmount = approvedAmount,
                timestamp = DateUtils.from(timestamp)
            )
        }
    }
}

@Component
class AccountNftApproveToNftListViewMapper(
    val accountAddressToViewMapper: AccountAddressToViewMapper,
    val blockService: BlockService,
    val contractService: ContractService,
) : ListMapper<AccountNftApprove, AccountNftApproveListView> {
    override fun transform(source: List<AccountNftApprove>): List<AccountNftApproveListView> {
        val blockMap = blockService.getBlocks(source.map { it.blockNumber }).associateBy { it.number }
        val contractMap = contractService.getContractMap(source.map { it.contractAddress }.toSet())

        return source.map { approve ->
            val block = blockMap[approve.blockNumber]!!
            val contract = contractMap[approve.contractAddress]
            val contractSummary = ContractSummary.of(approve.contractAddress, contract)
            val spenderAccount = accountAddressToViewMapper.transform(approve.spenderAddress)!!
            val timestamp = if(approve.timestamp > 0) approve.timestamp else block.timestamp

            AccountNftApproveListView(
                blockNumber = approve.blockNumber,
                transactionHash = approve.transactionHash,
                contractSummary = contractSummary,
                spenderAccount = spenderAccount,
                approvedAll = approve.approvedAll,
                approvedTokenId = approve.approvedTokenId,
                timestamp = DateUtils.from(timestamp)
            )
        }
    }
}