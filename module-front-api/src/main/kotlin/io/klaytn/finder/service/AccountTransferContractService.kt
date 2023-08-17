package io.klaytn.finder.service

import io.klaytn.finder.config.dynamic.FinderServerFeatureConfig
import io.klaytn.finder.domain.common.ContractType
import io.klaytn.finder.domain.common.TransferType
import io.klaytn.finder.domain.mysql.set1.AccountTransferContractRepository
import io.klaytn.finder.infra.web.model.AccountTransferContractPageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class AccountTransferContractService(
    private val accountTransferContractRepository: AccountTransferContractRepository,
    private val contractService: ContractService,
    private val finderServerFeatureConfig: FinderServerFeatureConfig
) {
    private val accountTransferContractSort = Sort.by(Sort.Order.desc("updated_at"))

    fun getAccountTransferContracts(
        accountAddress: String,
        transferType: TransferType,
        accountTransferContractPageRequest: AccountTransferContractPageRequest
    ): List<AccountTransferContract> {
        val pageRequest = accountTransferContractPageRequest.pageRequest(accountTransferContractSort)
        val contractAddresses =
            if(finderServerFeatureConfig.accountTransferContractWithJoin) {
                val contractTypes = if(transferType == TransferType.TOKEN) {
                    ContractType.getTokenTypes()
                } else {
                    ContractType.getNftTypes()
                }

                accountTransferContractRepository.findAllByAccountAddressAndContractTypeOrderByUpdatedAtDesc(
                    accountAddress, contractTypes.map { it.value }.toSet(), pageRequest)
            } else {
                accountTransferContractRepository.findAllByAccountAddressAndTransferTypeOrderByUpdatedAtDesc(
                    accountAddress, transferType.value, pageRequest)
            }
            .map { it.contractAddress }
        val contractMap = contractService.getContractMap(contractAddresses.toSet())

        return contractAddresses
            .mapNotNull { contractMap[it] }
            .filter { it.contractType != ContractType.CUSTOM }
            .map {
                val name = if(it.name.isNullOrBlank()) it.contractAddress else it.name ?: it.contractAddress

                AccountTransferContract(
                    it.contractAddress,
                    it.contractType,
                    name,
                    it.symbol ?: "-",
                    it.icon
                )
            }.toList()
    }
}

data class AccountTransferContract(
    val contractAddress: String,
    val contractType: ContractType,
    val contractName: String,
    val contractSymbol: String,
    val contractIcon: String?,
)