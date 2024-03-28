package io.klaytn.finder.interfaces.rest.api.view.mapper

import io.klaytn.commons.model.mapper.Mapper
import io.klaytn.finder.domain.mysql.set1.ContractCode
import io.klaytn.finder.interfaces.rest.api.view.model.contract.ContractCodeView
import io.klaytn.finder.interfaces.rest.api.view.model.contract.RelatedContractCodeView
import io.klaytn.finder.service.ContractCodeService
import io.klaytn.finder.service.ContractService
import org.springframework.stereotype.Component

@Component
class ContractCodeToItemViewMapper(
    private val contractService: ContractService,
    private val contractCodeService: ContractCodeService,
) : Mapper<ContractCode, ContractCodeView> {
    override fun transform(source: ContractCode): ContractCodeView {
        val contract = contractService.getContract(source.contractAddress)

        val proxyContractType = contract?.implementationAddress != null
        val implementationContractType =
            if(!proxyContractType) {
                contractService.isImplementationContract(source.contractAddress)
            } else {
                false
            }

        val implementationContractCode =
            if(proxyContractType) {
                contractCodeService.getImplementationContractCode(source.contractAddress)?.let {
                    RelatedContractCodeView(
                        contractAddress = it.contractAddress,
                        contractName = it.contractName,
                        contractABI = it.contractAbi,
                    )
                }
            } else {
                null
            }

        val proxyContractCode =
            if(implementationContractType) {
                contractCodeService.getProxyContractCode(source.contractAddress)?.let {
                    RelatedContractCodeView(
                        contractAddress = it.contractAddress,
                        contractName = it.contractName,
                        contractABI = it.contractAbi,
                    )
                }
            } else {
                null
            }

        return ContractCodeView(
            contractName = source.contractName,
            contractSourceCode = source.contractSourceCode,
            contractABI = source.contractAbi,
            contractCreationCode = source.contractCreationCode,
            abiEncodedValue = source.abiEncodedValue,
            compilerVersion = source.compilerVersion,
            optimizationFlag = source.optimizationFlag,
            optimizationRunsCount = source.optimizationRunsCount,
            evmVersion = source.optimizationEvmVersion,
            licenseType = source.licenseType,
            proxyContractType = proxyContractType,
            implementationContractType = implementationContractType,
            proxyContractCode = proxyContractCode,
            implementationContractCode = implementationContractCode,
        )
    }
}