package io.klaytn.finder.service

import io.klaytn.finder.domain.mysql.set1.ContractCode
import io.klaytn.finder.domain.mysql.set1.ContractCodeRepository
import org.springframework.stereotype.Service

@Service
class ContractCodeService(
    private val contractService: ContractService,
    private val contractCodeRepository: ContractCodeRepository,
) {
    fun getContractCode(contractAddress: String) = contractCodeRepository.findByContractAddress(contractAddress)

    /**
     * If the contractAddress is a proxy contract, returns the source of the implementation contract.
     */
    fun getImplementationContractCode(contractAddress: String) =
        contractService.getContract(contractAddress)?.implementationAddress?.let {
            getContractCode(it)
        }

    /**
     * If the contractAddress is an implementation contract, returns the source of the proxy contract.
     */
    fun getProxyContractCode(contractAddress: String) =
        contractService.getProxyAddressByImplementationAddress(contractAddress)?.let {
            getContractCode(it)
        }

    fun saveContractCode(contractCode: ContractCode) = contractCodeRepository.save(contractCode)
}