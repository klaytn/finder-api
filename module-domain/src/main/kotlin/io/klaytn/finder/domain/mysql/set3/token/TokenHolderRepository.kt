package io.klaytn.finder.domain.mysql.set3.token

import io.klaytn.finder.domain.common.AccountAddress
import io.klaytn.finder.domain.mysql.BaseRepository
import io.klaytn.finder.domain.mysql.EntityId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TokenHolderRepository : BaseRepository<TokenHolder> {
    fun findAllByContractAddress(contractAddress: String, pageable: Pageable): List<EntityId>
    fun findByContractAddressAndHolderAddress(contractAddress: String, holderAddress: AccountAddress): EntityId?
    fun findAllByHolderAddress(holderAddress: AccountAddress, pageable: Pageable): Page<EntityId>

    @Query(
        nativeQuery = true,
        value = """
            SELECT count(*) FROM (
                SELECT 1 FROM token_holders WHERE `contract_address`= :contractAddress limit :maxTotalCount
            ) t1
        """
    )
    fun countAllByContractAddress(
        @Param("contractAddress") contractAddress: String,
        @Param("maxTotalCount") maxTotalCount: Long,
    ): Long

    fun existsByHolderAddress(holderAddress: AccountAddress): Boolean
}
