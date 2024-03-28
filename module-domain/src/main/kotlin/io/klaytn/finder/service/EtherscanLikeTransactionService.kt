package io.klaytn.finder.service

import io.klaytn.finder.domain.common.ContractType
import io.klaytn.finder.domain.mysql.set1.ContractRepository
import io.klaytn.finder.domain.mysql.set1.Transaction
import io.klaytn.finder.domain.mysql.set1.TransactionRepository
import io.klaytn.finder.domain.mysql.set2.InternalTransaction
import io.klaytn.finder.domain.mysql.set2.InternalTransactionRepository
import io.klaytn.finder.domain.mysql.set2.index.InternalTransactionIndexRepository
import io.klaytn.finder.domain.mysql.set3.NFTTransactionRepository
import io.klaytn.finder.domain.mysql.set3.TokenTransactionRepository
import io.klaytn.finder.infra.db.shard.ShardNumContextHolder
import io.klaytn.finder.infra.db.shard.selector.AccountAddressShardNumSelector
import io.klaytn.finder.infra.db.shard.selector.BlockShardNumSelector
import io.klaytn.finder.infra.web.model.EtherscanLikeSimplePageRequest
import io.klaytn.finder.infra.web.model.SimplePageRequest
import io.klaytn.finder.view.model.transaction.EtherscanLikeInternalTransaction
import io.klaytn.finder.view.model.transaction.EtherscanLikeNFTTransaction
import io.klaytn.finder.view.model.transaction.EtherscanLikeTokenTransaction
import io.klaytn.finder.view.model.transaction.EtherscanLikeTransaction
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class EtherscanLikeTransactionService(
    private val transactionRepository: TransactionRepository,
    private val tokenTransactionRepository: TokenTransactionRepository,
    private val nftTransactionRepository: NFTTransactionRepository,
    private val contractRepository: ContractRepository,
    private val internalTransactionIndexRepository: InternalTransactionIndexRepository,
    private val internalTransactionRepository: InternalTransactionRepository,
    private val internalTransactionService: InternalTransactionService,
    private val transactionService: TransactionService,
    private val set2AccountShardNumSelector: AccountAddressShardNumSelector,
    private val set2BlockShardNumSelector: BlockShardNumSelector,
) {

    // Returns maxTotalCount, offset, limit
    private fun getPageParams(
        etherscanLikeSimplePageRequest: EtherscanLikeSimplePageRequest
    ): Triple<Long, Long, Long> {
        val maxTotalCount =
            (etherscanLikeSimplePageRequest.page *
                    etherscanLikeSimplePageRequest
                        .offset)
                .toLong()
        val offset = etherscanLikeSimplePageRequest.offset().toLong()
        val limit = etherscanLikeSimplePageRequest.offset.toLong()
        return Triple(maxTotalCount, offset, limit)
    }

    fun getTransactionsByAddress(
        address: String,
        blockRange: LongRange? = null,
        etherscanLikeSimplePageRequest: EtherscanLikeSimplePageRequest
    ): List<EtherscanLikeTransaction> {
        // Since the query is divided based on from and to for UNION processing (OR is faster),
        // calculate the maximum number of selections to be made in each UNION.
        // This is calculated before UNION since LIMIT processing is done after UNION.

        val (maxTotalCount, offset, limit) = getPageParams(etherscanLikeSimplePageRequest)
        transactionRepository.setSessionBufferSize()

        val queryResult =
            if (blockRange == null) {
                if (etherscanLikeSimplePageRequest.sort.equals("asc")) {
                    transactionRepository
                        .findAllByAccountAddressEtherscanLikeAsc(
                            address,
                            maxTotalCount,
                            offset,
                            limit
                        )
                } else {
                    transactionRepository
                        .findAllByAccountAddressEtherscanLikeDesc(
                            address,
                            maxTotalCount,
                            offset,
                            limit
                        )
                }
            } else {
                if (etherscanLikeSimplePageRequest.sort.equals("asc")) {
                    transactionRepository
                        .findAllByAccountAddressBlockNumberBetweenEtherscanLikeAsc(
                            address,
                            blockRange.first,
                            blockRange.last,
                            maxTotalCount,
                            offset,
                            limit
                        )
                } else {
                    transactionRepository
                        .findAllByAccountAddressBlockNumberBetweenEtherscanLikeDesc(
                            address,
                            blockRange.first,
                            blockRange.last,
                            maxTotalCount,
                            offset,
                            limit
                        )
                }
            }
        return queryResult.map {
            EtherscanLikeTransaction(
                hash = it[0],
                blockNumber = it[1],
                timeStamp = it[2],
                nonce = it[3],
                blockHash = it[4],
                transactionIndex = it[5],
                from = it[6],
                to = it[7],
                value = it[8],
                gas = it[9],
                gasPrice = it[10],
                iserror = it[11],
                txreceipt_status = it[12],
                input = it[13],
                contractAddress = it[14],
                gasUsed = it[15],
                confirmations = it[16],
                methodId = it[17],
                functionName = it[18],
                cumulativeGasUsed = it[19],
            )
        }
    }

    fun getTokenTransactionsByAddress(
        address: String,
        contractAddress: String? = null,
        blockRange: LongRange? = null,
        etherscanLikeSimplePageRequest: EtherscanLikeSimplePageRequest
    ): List<EtherscanLikeTokenTransaction> {
        val (maxTotalCount, offset, limit) = getPageParams(etherscanLikeSimplePageRequest)
        //        transactionRepository.setSessionBufferSize()

        val queryResult =
            if (blockRange == null) {
                if (contractAddress == null) {
                    if (etherscanLikeSimplePageRequest.sort.equals(
                            "asc"
                        )
                    ) {
                        tokenTransactionRepository
                            .findAllByAccountAddressEtherscanLikeAsc(
                                address,
                                maxTotalCount,
                                offset,
                                limit
                            )
                    } else {
                        tokenTransactionRepository
                            .findAllByAccountAddressEtherscanLikeDesc(
                                address,
                                maxTotalCount,
                                offset,
                                limit
                            )
                    }
                } else {
                    if (etherscanLikeSimplePageRequest.sort.equals(
                            "asc"
                        )
                    ) {
                        tokenTransactionRepository
                            .findAllByAccountAddressWithContractAddressEtherscanLikeAsc(
                                address,
                                contractAddress,
                                maxTotalCount,
                                offset,
                                limit
                            )
                    } else {
                        tokenTransactionRepository
                            .findAllByAccountAddressWithContractAddressEtherscanLikeDesc(
                                address,
                                contractAddress,
                                maxTotalCount,
                                offset,
                                limit
                            )
                    }
                }
            } else {
                if (contractAddress == null) {
                    if (etherscanLikeSimplePageRequest.sort.equals(
                            "asc"
                        )
                    ) {
                        tokenTransactionRepository
                            .findAllByAccountAddressBlockNumberBetweenEtherscanLikeAsc(
                                address,
                                blockRange.first,
                                blockRange.last,
                                maxTotalCount,
                                offset,
                                limit
                            )
                    } else {
                        tokenTransactionRepository
                            .findAllByAccountAddressBlockNumberBetweenEtherscanLikeDesc(
                                address,
                                blockRange.first,
                                blockRange.last,
                                maxTotalCount,
                                offset,
                                limit
                            )
                    }
                } else {
                    if (etherscanLikeSimplePageRequest.sort.equals(
                            "asc"
                        )
                    ) {
                        tokenTransactionRepository
                            .findAllByAccountAddressWithContractAddressBlockNumberBetweenEtherscanLikeAsc(
                                address,
                                contractAddress,
                                blockRange.first,
                                blockRange.last,
                                maxTotalCount,
                                offset,
                                limit
                            )
                    } else {
                        tokenTransactionRepository
                            .findAllByAccountAddressWithContractAddressBlockNumberBetweenEtherscanLikeDesc(
                                address,
                                contractAddress,
                                blockRange.first,
                                blockRange.last,
                                maxTotalCount,
                                offset,
                                limit
                            )
                    }
                }
            }

        var tokenTransferList =
            queryResult.map {
                EtherscanLikeTokenTransaction(
                    hash = it[0],
                    blockNumber = it[1],
                    timeStamp = it[2],
                    nonce = "",
                    blockHash = "",
                    transactionIndex = "",
                    from = it[3],
                    to = it[4],
                    contractAddress = it[5],
                    value =
                    it[6].substring(2)
                        .toBigInteger(
                            16
                        )
                        .toString(),
                    gas = "",
                    gasPrice = "",
                    input = "",
                    gasUsed = "",
                    confirmations = "",
                    tokenName = "",
                    tokenSymbol = "",
                    tokenDecimal = "",
                    cumulativeGasUsed = "",
                )
            }
        val transactionHashMap = tokenTransferList.groupBy { it.hash }
        val transactionHashList = transactionHashMap.keys.toList().distinct()
        val contractAddressList =
            tokenTransferList
                .groupBy { it.contractAddress.lowercase() }
                .keys
                .toList()
                .distinct()

        transactionRepository.findAllByTransactionHashInEtherscanLike(transactionHashList)
            .map { tx ->
                val nonce = tx[3]
                val blockHash = tx[4]
                val transactionIndex = tx[5]
                val gas = tx[9]
                val gasPrice = tx[10]
                val input = tx[13]
                val gasUsed = tx[15]
                val confirmations = tx[16]
                val transactionHash = tx[0]
                val cumulativeGasUsed = tx[19]
                val tokenTransfers =
                    transactionHashMap[transactionHash]!!
                tokenTransfers.map { transfer ->
                    transfer.nonce = nonce
                    transfer.blockHash = blockHash
                    transfer.transactionIndex = transactionIndex
                    transfer.gas = gas
                    transfer.gasPrice = gasPrice
                    transfer.input = input
                    transfer.gasUsed = gasUsed
                    transfer.confirmations = confirmations
                    transfer.cumulativeGasUsed = cumulativeGasUsed
                }
            }

        contractRepository.findAllByContractAddressIn(contractAddressList).map { contract ->
            val tokenTransferList =
                transactionHashMap.values.flatten().filter {
                    it.contractAddress.lowercase() ==
                            contract.contractAddress.lowercase()
                }
            tokenTransferList.map { transfer ->
                transfer.tokenName = contract.name.toString()
                transfer.tokenSymbol = contract.symbol.toString()
                transfer.tokenDecimal = contract.decimal.toString()
            }
        }
        return tokenTransferList
    }

    fun getTokenNftTransactionsByAddress(
        address: String,
        nftTypes: Array<ContractType>,
        contractAddress: String? = null,
        blockRange: LongRange? = null,
        etherscanLikeSimplePageRequest: EtherscanLikeSimplePageRequest
    ): List<EtherscanLikeNFTTransaction> {
        val (maxTotalCount, offset, limit) = getPageParams(etherscanLikeSimplePageRequest)
        // nftTransactionRepository.setSessionBufferSize()
        val contractTypes = nftTypes.map { it.value }
        val queryResult =
            if (blockRange == null) {
                if (contractAddress == null) {
                    if (etherscanLikeSimplePageRequest.sort.equals(
                            "asc"
                        )
                    ) {
                        nftTransactionRepository
                            .findAllByAccountAddressEtherscanLikeAsc(
                                address,
                                contractTypes,
                                maxTotalCount,
                                offset,
                                limit
                            )
                    } else {
                        nftTransactionRepository
                            .findAllByAccountAddressEtherscanLikeDesc(
                                address,
                                contractTypes,
                                maxTotalCount,
                                offset,
                                limit
                            )
                    }
                } else {
                    if (etherscanLikeSimplePageRequest.sort.equals(
                            "asc"
                        )
                    ) {
                        nftTransactionRepository
                            .findAllByAccountAddressWithContractAddressEtherscanLikeAsc(
                                address,
                                contractTypes,
                                contractAddress,
                                maxTotalCount,
                                offset,
                                limit
                            )
                    } else {
                        nftTransactionRepository
                            .findAllByAccountAddressWithContractAddressEtherscanLikeDesc(
                                address,
                                contractTypes,
                                contractAddress,
                                maxTotalCount,
                                offset,
                                limit
                            )
                    }
                }
            } else {
                if (contractAddress == null) {
                    if (etherscanLikeSimplePageRequest.sort.equals(
                            "asc"
                        )
                    ) {
                        nftTransactionRepository
                            .findAllByAccountAddressBlockNumberBetweenEtherscanLikeAsc(
                                address,
                                contractTypes,
                                blockRange.first,
                                blockRange.last,
                                maxTotalCount,
                                offset,
                                limit
                            )
                    } else {
                        nftTransactionRepository
                            .findAllByAccountAddressBlockNumberBetweenEtherscanLikeDesc(
                                address,
                                contractTypes,
                                blockRange.first,
                                blockRange.last,
                                maxTotalCount,
                                offset,
                                limit
                            )
                    }
                } else {
                    if (etherscanLikeSimplePageRequest.sort.equals(
                            "asc"
                        )
                    ) {
                        nftTransactionRepository
                            .findAllByAccountAddressWithContractAddressBlockNumberBetweenEtherscanLikeAsc(
                                address,
                                contractTypes,
                                contractAddress,
                                blockRange.first,
                                blockRange.last,
                                maxTotalCount,
                                offset,
                                limit
                            )
                    } else {
                        nftTransactionRepository
                            .findAllByAccountAddressWithContractAddressBlockNumberBetweenEtherscanLikeDesc(
                                address,
                                contractTypes,
                                contractAddress,
                                blockRange.first,
                                blockRange.last,
                                maxTotalCount,
                                offset,
                                limit
                            )
                    }
                }
            }

        var nftTransferList =
            queryResult.map { it ->
                val contractType = ContractType.of(it[8].toInt())
                EtherscanLikeNFTTransaction(
                    hash = it[0],
                    blockNumber = it[1],
                    timeStamp = it[2],
                    nonce = "",
                    blockHash = "",
                    transactionIndex = "",
                    from = it[3],
                    to = it[4],
                    contractAddress = it[5],
                    tokenValue =
                    if (isKIP17ERC721(
                            contractType
                        )
                    )
                        it[6]
                            .substring(
                                2
                            )
                            .toBigInteger(
                                16
                            )
                            .toString()
                    else null,
                    gas = "",
                    gasPrice = "",
                    input = "",
                    gasUsed = "",
                    confirmations = "",
                    tokenName = "",
                    tokenSymbol = "",
                    tokenDecimal = null,
                    tokenID = it[7],
                    cumulativeGasUsed = "",
                )
            }
        val transactionHashMap = nftTransferList.groupBy { it.hash }
        val transactionHashList = transactionHashMap.keys.toList().distinct()
        val contractAddressList =
            nftTransferList
                .groupBy { it.contractAddress.lowercase() }
                .keys
                .toList()
                .distinct()

        transactionRepository.findAllByTransactionHashInEtherscanLike(transactionHashList)
            .map { tx ->
                val nonce = tx[3]
                val blockHash = tx[4]
                val transactionIndex = tx[5]
                val gas = tx[9]
                val gasPrice = tx[10]
                val input = tx[13]
                val gasUsed = tx[15]
                val confirmations = tx[16]
                val transactionHash = tx[0]
                val cumulativeGasUsed = tx[19]
                val nftTransfers = transactionHashMap[transactionHash]!!
                nftTransfers.map { transfer ->
                    transfer.nonce = nonce
                    transfer.blockHash = blockHash
                    transfer.transactionIndex = transactionIndex
                    transfer.gas = gas
                    transfer.gasPrice = gasPrice
                    transfer.input = input
                    transfer.gasUsed = gasUsed
                    transfer.confirmations = confirmations
                    transfer.cumulativeGasUsed = cumulativeGasUsed
                }
            }

        contractRepository.findAllByContractAddressIn(contractAddressList).map { contract ->
            val nftTransferList =
                transactionHashMap.values.flatten().filter {
                    it.contractAddress.lowercase() ==
                            contract.contractAddress.lowercase()
                }
            nftTransferList.map { transfer ->
                transfer.tokenName = contract.name.toString()
                transfer.tokenSymbol = contract.symbol.toString()
                if (isKIP37ERC1155(contract.contractType)) {
                    transfer.tokenDecimal = contract.decimal.toString()
                }
            }
        }
        return nftTransferList
    }

    fun getInternalTransactionsByAddress(
        address: String,
        blockRange: LongRange? = null,
        etherscanLikeSimplePageRequest: EtherscanLikeSimplePageRequest
    ): List<EtherscanLikeInternalTransaction> {

        val pageable =
            SimplePageRequest(
                etherscanLikeSimplePageRequest.page,
                etherscanLikeSimplePageRequest
                    .offset
            )
                .pageRequest()
        var internalTxList = emptyList<InternalTransaction>()
        try {
            ShardNumContextHolder.setDataSourceType(set2AccountShardNumSelector.getShardType(address))
            val indicesResult =
                if (etherscanLikeSimplePageRequest.sort.equals("asc")) {
                    if (blockRange == null) {
                        internalTransactionIndexRepository
                            .findAllByAccountAddressAsc(
                                address,
                                pageable
                            )
                    } else {
                        internalTransactionIndexRepository
                            .findAllByAccountAddressAndBlockNumberBetweenAsc(
                                address,
                                blockRange.first,
                                blockRange.last,
                                pageable
                            )
                    }
                } else {
                    if (blockRange == null) {
                        internalTransactionIndexRepository
                            .findAllByAccountAddressAsc(
                                address,
                                pageable
                            )
                    } else {
                        internalTransactionIndexRepository
                            .findAllByAccountAddressAndBlockNumberBetween(
                                address,
                                blockRange.first,
                                blockRange.last,
                                pageable
                            )
                    }
                }
            val shardNumberAndInternalTxIndices =
                indicesResult
                    .map { it ->
                        Pair(
                            set2BlockShardNumSelector.select(it.internalTxId.split("_")[0].toLong()),
                            it.internalTxId
                        )
                    }
                    .groupBy { it.first }
                    .entries
                    .associate { it.key to it.value.map { it.second } }

            shardNumberAndInternalTxIndices.entries.map { entry ->
                ShardNumContextHolder.setDataSourceType(entry.key)
                val queryResult =
                    internalTransactionRepository.findAllByInternalTxIdIn(
                        entry.value,
                    )
                internalTxList += queryResult
            }

        } finally {
            ShardNumContextHolder.clear()
        }

        val transactions = emptyList<Transaction>().toMutableList()
        val chunkedInternalTxList = internalTxList.chunked(1000)
        chunkedInternalTxList.map { chunkedInternalTxs ->
            val blockNumbers = chunkedInternalTxs.map { it.blockNumber }
            val blocksTxIndices = chunkedInternalTxs.map { "${it.blockNumber},${it.transactionIndex}" }
            transactions += transactionRepository.findAllByTransactionByBlockNumbersAndTransactionIndices(
                blockNumbers,
                blocksTxIndices
            )
        }

        val transactionMap: Map<Long, Map<Int, Transaction>> = transactions
            .groupBy { it.blockNumber }
            .entries
            .associate { entry ->
                entry.key to entry.value.map { it.transactionIndex to it }.toMap()
            }

        val result =
            internalTxList.map { it ->
                val transaction =
                    transactionMap[it.blockNumber]?.get(
                        it.transactionIndex
                    )
                EtherscanLikeInternalTransaction(
                    hash =
                    transaction?.transactionHash ?: "",
                    blockNumber = it.blockNumber.toString(),
                    timeStamp = if (transaction?.timestamp == null) "" else transaction.timestamp.toString(),
                    from = it.from.address,
                    to = it.from.address,
                    value = it.value.toString(),
                    gas = it.gas.toString(),
                    input = it.input,
                    gasUsed = if (it.gasUsed == null) "" else it.gasUsed.toString(),
                    contractAddress =
                    if (transaction?.contractAddress == null) "" else transaction.contractAddress,
                    iserror = if (it.error == null) "0" else "1",
                    errCode = it.error ?: "",
                    type = it.type,
                    traceId = it.internalTxId
                )
            }
        return if (etherscanLikeSimplePageRequest.sort.equals("asc")) {
            result.sortedBy { it.traceId }
        } else {
            result.sortedByDescending { it.traceId }
        }
    }

    fun isKIP17ERC721(contractType: ContractType): Boolean {
        return contractType.equals(ContractType.KIP17) ||
                contractType.equals(ContractType.ERC721)
    }

    fun isKIP37ERC1155(contractType: ContractType): Boolean {
        return contractType.equals(ContractType.KIP37) ||
                contractType.equals(ContractType.ERC1155)
    }
}
