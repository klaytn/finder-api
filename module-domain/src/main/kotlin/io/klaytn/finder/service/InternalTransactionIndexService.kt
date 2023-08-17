package io.klaytn.finder.service

import io.klaytn.finder.config.dynamic.FinderServerPaging
import io.klaytn.finder.domain.mysql.InternalTxId
import io.klaytn.finder.domain.mysql.set2.index.InternalTransactionIndexRepository
import io.klaytn.finder.infra.db.shard.ShardNumContextHolder
import io.klaytn.finder.infra.db.shard.selector.AccountAddressShardNumSelector
import io.klaytn.finder.infra.web.model.SimplePageRequest
import org.springframework.stereotype.Service

@Service
class InternalTransactionIndexService(
    private val internalTransactionIndexRepository: InternalTransactionIndexRepository,
    private val set2AccountAddressShardNumSelector: AccountAddressShardNumSelector,
    private val finderServerPaging: FinderServerPaging
) {
    fun getIdsByAccountAddress(
        accountAddress: String,
        blockNumberRange: LongRange? = null,
        simplePageRequest: SimplePageRequest,
    ): List<InternalTxId> {
        try {
            val pageRequest = simplePageRequest.pageRequest()
            ShardNumContextHolder.setDataSourceType(set2AccountAddressShardNumSelector.getShardType(accountAddress))
            return blockNumberRange?.let {
                internalTransactionIndexRepository.findAllByAccountAddressAndBlockNumberBetween(
                    accountAddress, it.first, it.last, pageRequest)
            } ?: internalTransactionIndexRepository.findAllByAccountAddress(accountAddress, pageRequest)
        } finally {
            ShardNumContextHolder.clear()
        }
    }

    fun countByAccountAddress(
        accountAddress: String,
        blockNumberRange: LongRange? = null,
    ): Long {
        try {
            ShardNumContextHolder.setDataSourceType(set2AccountAddressShardNumSelector.getShardType(accountAddress))
            val maxLimit = finderServerPaging.limit.internalTransaction
            return blockNumberRange?.let {
                internalTransactionIndexRepository.countAllByAccountAddressAndBlockNumberBetween(
                    accountAddress, it.first, it.last, maxLimit)
            } ?: internalTransactionIndexRepository.countAllByAccountAddress(accountAddress, maxLimit)
        } finally {
            ShardNumContextHolder.clear()
        }
    }

    fun existsByAccountAddress(accountAddress: String): Boolean {
        try {
            ShardNumContextHolder.setDataSourceType(set2AccountAddressShardNumSelector.getShardType(accountAddress))
            return internalTransactionIndexRepository.existsByAccountAddress(accountAddress)
        } finally {
            ShardNumContextHolder.clear()
        }
    }
}