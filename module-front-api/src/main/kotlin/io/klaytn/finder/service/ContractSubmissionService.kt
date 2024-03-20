package io.klaytn.finder.service

import io.klaytn.commons.utils.logback.logger
import io.klaytn.commons.utils.retrofit2.orElseThrow
import io.klaytn.finder.config.ChainProperties
import io.klaytn.finder.config.dynamic.FinderServerFeatureConfig
import io.klaytn.finder.domain.common.AccountType
import io.klaytn.finder.domain.common.WalletType
import io.klaytn.finder.domain.mysql.set1.*
import io.klaytn.finder.infra.client.CompileRequest
import io.klaytn.finder.infra.client.CompileResult
import io.klaytn.finder.infra.client.ContractCompilerClient
import io.klaytn.finder.infra.db.DbConstants
import io.klaytn.finder.infra.exception.InvalidContractSubmissionException
import io.klaytn.finder.infra.exception.NotFoundContractException
import io.klaytn.finder.infra.utils.KlayUtils
import io.klaytn.finder.infra.web.model.SimplePageRequest
import io.klaytn.finder.service.caver.CaverAccountService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class ContractSubmissionService(
    private val caverAccountService: CaverAccountService,
    private val accountService: AccountService,
    private val contractCompilerClient: ContractCompilerClient,
    private val transactionService: TransactionService,
    private val internalTransactionService: InternalTransactionService,
    private val contractService: ContractService,
    private val contractCodeService: ContractCodeService,
    private val contractSubmissionRequestRepository: ContractSubmissionRequestRepository,
    private val chainProperties: ChainProperties,
    private val finderServerFeatureConfig: FinderServerFeatureConfig
) {
    private val logger = logger(javaClass)

    fun checkAndCompile(
        contractSubmissionRequest: ContractSubmissionRequest,
    ): CompileResult {
        with(contractSubmissionRequest) {
            // 0) get contract & contract creator account
            val contractAccount = accountService.getAccount(contractAddress)
            if(contractAccount.contractCreatorAddress.isNullOrBlank() || contractAccount.contractCreatorTransactionHash.isNullOrBlank()) {
                throw InvalidContractSubmissionException("Contract does not have creator-address or creator-transaction.")
            }
            val contractCreatorAccount = accountService.getAccount(contractAccount.contractCreatorAddress!!)

            verifyContractCreatorSignature(
                contractAddress, contractAccount, contractCreatorAccount, contractCreatorSignature,
                WalletType.getOrDefaultIfNull(walletType))

            // 2) compile source code
            val libraryMap = this.libraries?.let { libraries ->
                libraries.map { it.split("=") }.associate { it[0] to it[1] }
            }
            val compileResults = compileContractSource(
                compilerVersion, licenseType, optimization, optimizationRuns, evmVersion, contractSourceCode,
                libraryMap
            )
            if (compileResults.isEmpty()) {
                throw InvalidContractSubmissionException("Contract source is not compiled.")
            }

            // 3) get contractCode by caver
            val contractCodeWithoutSwarmHash = removeSwarmHash(contractCreationCode)

            // 4) get input data
            var inputData: String? = null
            val transaction = transactionService.getTransactionByHash(contractAccount.contractCreatorTransactionHash!!)
                ?: throw InvalidContractSubmissionException("Transaction for contract address is not valid.")
            if(contractCreatorAccount.accountType == AccountType.SCA) {
                // If the constructor is a contract, you should retrieve the input data from internal transactions.
                var currentPage = 1
                while(true) {
                    val internalTransactionPage =
                        internalTransactionService.getInternalTransactionsByBlockNumberAndIndex(
                            transaction.blockNumber,
                            transaction.transactionIndex,
                            SimplePageRequest(currentPage++, 10)
                        )
                    internalTransactionPage.content.find { it.type.startsWith("create", true) }?.let {
                        inputData = KlayUtils.stripHexPrefix(it.input)
                    }

                    if(!inputData.isNullOrBlank() || internalTransactionPage.isLast) {
                        break
                    }
                }

            } else {
                inputData = transaction.input?.let { KlayUtils.stripHexPrefix(it) }
            }
            if(inputData.isNullOrBlank()) {
                throw InvalidContractSubmissionException("Transaction input data for contract-address is not valid.")
            }

            // 5) Extract constructorArguments from inputData
            val constructorArgumentsFromInputData = inputData?.let { getConstructorArguments(it) }
            val revisedConstructorArguments =
                if(finderServerFeatureConfig.contractSubmissionConstructorArguments) {
                    constructorArgumentsFromInputData
                } else {
                    constructorArguments
                }

            // 6) compare
            val inputDataWithoutSwarmHash = inputData?.let { removeSwarmHash(it) }
            val matchedSourceCompileResult = compileResults.find {
                val binaryWithoutSwarmHash = removeSwarmHash(it.binary)
                val runtimeBinaryWithoutSwarmHash = removeSwarmHash(it.runtimeBinary)

                contractCodeWithoutSwarmHash == runtimeBinaryWithoutSwarmHash ||
                        inputDataWithoutSwarmHash == "$binaryWithoutSwarmHash${revisedConstructorArguments ?: ""}"

            } ?: throw InvalidContractSubmissionException("Contract source is not valid.")

            contractSubmissionRequest.constructorArguments = constructorArgumentsFromInputData
            return matchedSourceCompileResult
        }
    }

    @Transactional(DbConstants.set1TransactionManager)
    fun submission(
        contractSubmissionRequest: ContractSubmissionRequest,
        contractCreationCode: String,
        compileResult: CompileResult,
    ) {
        with(contractSubmissionRequest) {
            val contract: Contract = contractService.getContract(contractAddress)?.apply {
                if(!tokenName.isNullOrBlank()) {
                    this.name = tokenName!!.trim()
                }
                if(!tokenSymbol.isNullOrBlank()) {
                    this.symbol = tokenSymbol!!.trim()
                }
                if(!tokenIcon.isNullOrBlank()) {
                    this.icon = tokenIcon
                }
                if(!officialWebSite.isNullOrBlank()) {
                    this.officialSite = officialWebSite
                }
                if(!contractSubmissionRequest.officialEmailAddress.isNullOrBlank()){
                    this.officialEmailAddress = contractSubmissionRequest.officialEmailAddress
                }
            } ?: throw NotFoundContractException()
            contractService.saveContract(contract)

            var contractAbi = compileResult.abi
            if(contractAbi.startsWith("\"") && contractAbi.endsWith("\"")) {
                contractAbi = contractAbi.removeSurrounding("\"")
                contractAbi = contractAbi.replace("\\", "")
            }

            val contractCode = contractCodeService.getContractCode(contractAddress) ?: ContractCode.of(contractAddress)
            with(contractCode) {
                this.contractName = compileResult.name
                this.compilerVersion = contractSubmissionRequest.compilerVersion
                this.optimizationFlag = optimization
                this.optimizationRunsCount = optimizationRuns ?: 0
                this.optimizationEvmVersion = evmVersion ?: "compiler default"
                this.contractSourceCode = contractSubmissionRequest.contractSourceCode
                this.contractAbi = contractAbi
                this.contractCreationCode = contractCreationCode
                this.licenseType = contractSubmissionRequest.licenseType
                this.abiEncodedValue = constructorArguments
            }
            contractCodeService.saveContractCode(contractCode)
        }
    }

    @Transactional(DbConstants.set1TransactionManager)
    fun registerSubmissionRequest(contractSubmissionRequest: ContractSubmissionRequest) =
        contractSubmissionRequestRepository.save(contractSubmissionRequest)

    private fun verifyContractCreatorSignature(
        requestedContractAddress: String,
        contractAccount: Account,
        contractCreatorAccount: Account,
        contractCreatorSignature: String,
        walletType: WalletType,
    ) {
        // verify request and get contract address from signature
        val signMessage = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + requestedContractAddress
        val recoveredAccountAddressFor = caverAccountService.getRecoveredAccountAddress(walletType, signMessage, contractCreatorSignature)

        // get contract creator address
        var contractCreatorAddress = contractCreatorAccount.address
        if(contractCreatorAccount.accountType == AccountType.SCA) {
            // If the contract constructor is another contract, the 'from' address of contractCreatorTransactionHash is the constructor's address.
            transactionService.getTransactionByHash(contractAccount.contractCreatorTransactionHash!!)?.let {
                contractCreatorAddress = it.from.address
            }
        }

        if (!recoveredAccountAddressFor.equals(contractCreatorAddress, true)) {
            throw InvalidContractSubmissionException("Contract creator signature is not valid.")
        }
    }

    private fun compileContractSource(
        compilerVersion: String,
        licenseType: String,
        optimization: Boolean,
        optimizationRuns: Long?,
        evmVersion: String?,
        sourceCode: String,
        libraries: Map<String, String>?,

    ): List<CompileResult> {
        val compileResults = contractCompilerClient.compileContractCode(
            CompileRequest(
                version = compilerVersion,
                license = licenseType,
                optimize = optimization,
                optimizeRuns = optimizationRuns ?: 200,
                evmVersion = evmVersion,
                libraries = libraries,
                solidity = sourceCode
            )
        ).orElseThrow { IllegalStateException() }

        compileResults.forEach {
            logger.info(it.toString())
        }
        return compileResults
    }

    private fun removeSwarmHash(binary: String): String =
        getSwarmHashRegex(binary)?.let { binary.replace(it, "") } ?: binary

    private fun getConstructorArguments(inputData: String): String? =
        getSwarmHashRegex(inputData)?.let {
            val matches = it.findAll(inputData)
            if(matches.count() != 0) {
                inputData.substring(matches.last().range.last+1).ifBlank { null }
            } else {
                null
            }
        }

    /**
     * ex) https://docs.soliditylang.org/en/v0.4.17/metadata.html
     * ex) https://docs.soliditylang.org/en/v0.5.x/metadata.html
     * ex) https://docs.soliditylang.org/en/v0.6.x/metadata.html
     * ex) https://docs.soliditylang.org/en/v0.7.x/metadata.html
     * ex) https://docs.soliditylang.org/en/v0.8.x/metadata.html
     */
    private fun getSwarmHashRegex(binary: String): Regex? =
        if(binary.indexOf("a165627a7a72305820") >= 0) {
            // 0.4.17~0.5.8
            // -> a165627a7a72305820<32 bytes swarm hash>0029
            Regex("a165627a7a72305820[a-fA-F0-9]{64}0029")
        } else if(binary.indexOf("a265627a7a72305820") >= 0) {
            // 0.5.9~0.5.11
            // -> a265627a7a72305820<32 bytes swarm hash>64736f6c6343<3 byte version encoding>0032
            Regex("a265627a7a72305820[a-fA-F0-9]{64}64736f6c6343[a-fA-F0-9]{6}0032")
        } else if(binary.indexOf("a265627a7a72315820") >= 0) {
            // 0.5.12~0.5.17
            // -> a265627a7a72315820<32 bytes swarm hash>64736f6c6343<3 byte version encoding>0032
            Regex("a265627a7a72315820[a-fA-F0-9]{64}64736f6c6343[a-fA-F0-9]{6}0032")
        } else if(binary.indexOf("a264697066735822") >= 0) {
            if(binary.endsWith("0032")) {
                // 0.6.1
                // -> a264697066735822<34 bytes IPFS hash>64736f6c6343<3 byte version encoding>0032
                Regex("a264697066735822[a-fA-F0-9]{68}64736f6c6343[a-fA-F0-9]{6}0032")
            } else {
                // 0.6.2~, 0.7.1~, 0.8.1~
                // -> a264697066735822<34 bytes IPFS hash>64736f6c6343<3 byte version encoding>0033
                Regex("a264697066735822[a-fA-F0-9]{68}64736f6c6343[a-fA-F0-9]{6}0033")
            }
        } else {
            null
        }
}