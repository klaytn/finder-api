package io.klaytn.finder.interfaces.rest.api.view.mapper

import io.klaytn.commons.model.mapper.ListMapper
import io.klaytn.commons.model.mapper.Mapper
import io.klaytn.finder.domain.mysql.set1.Contract
import io.klaytn.finder.infra.utils.DateUtils
import io.klaytn.finder.infra.utils.applyDecimal
import io.klaytn.finder.interfaces.rest.api.view.model.contract.ContractItemView
import io.klaytn.finder.interfaces.rest.api.view.model.contract.ContractListView
import io.klaytn.finder.service.AccountService
import org.springframework.stereotype.Component

@Component
class ContractToListViewMapper(
    val accountService: AccountService,
) : ListMapper<Contract, ContractListView> {
    override fun transform(source: List<Contract>): List<ContractListView> {
        val accountAddresses = source.map { it.contractAddress }
        val accountMap = accountService.getAccountMap(accountAddresses.toSet())

        return source.map { contract ->
            val account = accountMap[contract.contractAddress]

            ContractListView(
                address = contract.contractAddress,
                type = contract.contractType,
                name = contract.name,
                symbol = contract.symbol,
                icon = contract.icon,
                officialSite = contract.officialSite,
                officialEmailAddress = contract.officialEmailAddress,
                contractCreatorAddress = account?.contractCreatorAddress,
                contractCreatorTransactionHash =  account?.contractCreatorTransactionHash,
                contractCreated = !contract.txError,
                createdAt = DateUtils.localDateTimeToDate(contract.createdAt!!),
                updatedAt = DateUtils.localDateTimeToDate(contract.updatedAt!!),
            )
        }
    }
}

@Component
class ContractToItemViewMapper(
) : Mapper<Contract, ContractItemView> {
    override fun transform(source: Contract): ContractItemView {
        return ContractItemView(
            address = source.contractAddress,
            type = source.contractType,
            name = source.name,
            symbol = source.symbol,
            icon = source.icon,
            officialSite = source.officialSite,
            officialEmailAddress = source.officialEmailAddress,
            decimal = source.decimal,
            totalSupply = source.totalSupply.applyDecimal(source.decimal),
            totalTransfer = source.totalTransfer,
            contractCreated = !source.txError,
            implementationAddress = source.implementationAddress
        )
    }
}