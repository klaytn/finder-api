package io.klaytn.finder.domain.mysql.set1

import io.klaytn.finder.domain.mysql.BaseRepository
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface AccountTransferContractRepository : BaseRepository<AccountTransferContract> {
    @Query(
        nativeQuery = true,
        value = """
            SELECT 
                contract_address as contractAddress
            FROM
                account_transfer_contracts
            WHERE
                account_address = :accountAddress and
                transfer_type = :transferType
        """
    )
    fun findAllByAccountAddressAndTransferTypeOrderByUpdatedAtDesc(
        @Param("accountAddress") accountAddress: String,
        @Param("transferType") transferType: Int,
        pageable: Pageable
    ): List<ContractAddress>

    @Query(
        nativeQuery = true,
        value = """
            SELECT 
                a.contract_address as contractAddress
            FROM 
                account_transfer_contracts a
            INNER JOIN 
                contracts c ON a.contract_address = c.contract_address
            WHERE 
                account_address = :accountAddress
                AND c.contract_type in (:contractTypes)
        """
    )
    fun findAllByAccountAddressAndContractTypeOrderByUpdatedAtDesc(
        @Param("accountAddress") accountAddress: String,
        @Param("contractTypes") contractTypes: Set<Int>,
        pageable: Pageable
    ): List<ContractAddress>
}