package io.klaytn.finder.service

import io.klaytn.finder.domain.common.AccountAddress
import io.klaytn.finder.domain.common.AccountAddressContract
import io.klaytn.finder.domain.common.AccountType
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils

@Service
class AccountAddressService(
    private val accountService: AccountService,
    private val contractService: ContractService,
) {
    fun getAccountAddress(accountAddress: String?) =
        if(!accountAddress.isNullOrBlank()) {
            AccountAddress(accountAddress).apply {
                fillAccountAddress(listOf(this))
            }
        } else {
            null
        }

    fun fillAccountAddress(vararg accountAddresses: List<AccountAddress>) {
        val mergedAccountAddresses = mutableListOf<AccountAddress>()
        accountAddresses.forEach {
            mergedAccountAddresses.addAll(it)
        }

        val addresses = mergedAccountAddresses.map { it.address }.toSet()
        val accountMap = accountService.getAccountMap(addresses)

        val contractAddresses =
            accountMap.values.filter { it.accountType == AccountType.SCA }.map { it.address }.toSet()
        val contractMap = contractService.getContractMap(contractAddresses)

        mergedAccountAddresses.forEach { accountAddress ->
            accountMap[accountAddress.address]?.let { account ->
                accountAddress.accountType = account.accountType
                accountAddress.contractType = account.contractType
                accountAddress.knsDomain = account.knsDomain
                accountAddress.addressLabel = account.addressLabel

                contractMap[account.address]?.let { contract ->
                    if(StringUtils.hasText(contract.name) || StringUtils.hasText(contract.symbol)) {
                        accountAddress.contract = AccountAddressContract(
                            name = contract.name ?: contract.contractAddress,
                            symbol = contract.symbol ?: "-",
                            icon = contract.icon,
                            verified = contract.verified
                        )
                    }
                }
            }
        }
    }
}
