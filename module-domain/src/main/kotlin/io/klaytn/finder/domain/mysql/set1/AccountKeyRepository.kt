package io.klaytn.finder.domain.mysql.set1

import io.klaytn.finder.domain.common.AccountAddress
import io.klaytn.finder.domain.common.ContractType
import io.klaytn.finder.domain.mysql.BaseRepository
import io.klaytn.finder.domain.mysql.EntityId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
interface AccountKeyRepository : BaseRepository<AccountKey> {
    fun findAllByAccountAddress(accountAddress: String, pageable: Pageable): Page<EntityId>

    fun findByTransactionHash(transactionHash: String): EntityId?

    fun existsByAccountAddress(accountAddress: String): Boolean
}

