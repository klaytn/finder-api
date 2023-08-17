package io.klaytn.finder.domain.mysql.set3.nft

import io.klaytn.finder.domain.common.AccountAddress
import io.klaytn.finder.domain.common.ContractType
import io.klaytn.finder.domain.mysql.BaseRepository
import io.klaytn.finder.domain.mysql.EntityId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface NftInventoryRepository : BaseRepository<NftInventory> {
    fun findAllByContractTypeAndHolderAddress(
        contractType: ContractType,
        holderAddress: AccountAddress,
        pageable: Pageable,
    ): Page<EntityId>

    fun findAllByHolderAddress(
        holderAddress: AccountAddress,
        pageable: Pageable,
    ): Page<EntityId>

    fun findAllByHolderAddressAndTokenUriIsNot(
        holderAddress: AccountAddress,
        tokenUri: String,
        pageable: Pageable,
    ): Page<EntityId>

    @Query(
        nativeQuery = true,
        value = """
            SELECT 
                id 
            FROM 
                nft_inventories force index(ix_contractaddress_lasttransactiontime)
            where 
                contract_address = :contractAddress
            order by 
                last_transaction_time DESC
        """
    )
    fun findAllByContractAddressOrderByLastTransactionTime(contractAddress: String, pageable: Pageable): List<EntityId>

    @Query(
        nativeQuery = true,
        value = """
            SELECT 
                id 
            FROM 
                nft_inventories force index(ix_contractaddress_tokencount)
            where 
                contract_address = :contractAddress
            order by 
                token_count DESC
        """
    )
    fun findAllByContractAddressOrderByTokenCount(contractAddress: String, pageable: Pageable): List<EntityId>

    fun findAllByContractAddressAndHolderAddress(
        contractAddress: String,
        holderAddress: AccountAddress,
        pageable: Pageable,
    ): Page<EntityId>

    fun findAllByContractAddressAndTokenId(
        contractAddress: String,
        tokenId: String,
        pageable: Pageable,
    ): Page<EntityId>

    fun findFirstByContractAddressAndTokenId(contractAddress: String, tokenId: String): EntityId?

    @Query(
        nativeQuery = true,
        value = """
            SELECT count(*) FROM (
                SELECT 1 FROM nft_inventories WHERE `contract_address`= :contractAddress limit :maxTotalCount
            ) t1
        """
    )
    fun countAllByContractAddress(
        @Param("contractAddress") contractAddress: String,
        @Param("maxTotalCount") maxTotalCount: Long,
    ): Long

    fun existsByContractTypeAndHolderAddress(contractType: ContractType, holderAddress: AccountAddress): Boolean

    @Modifying
    @Query("UPDATE NftInventory a SET a.tokenUri = :tokenUri WHERE a.id in (:ids) AND a.tokenId = :tokenId")
    fun updateTokenUri(ids: List<Long>, tokenId: String, tokenUri: String): Int
}
