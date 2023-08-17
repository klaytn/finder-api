package io.klaytn.finder.domain.mysql.set1

import io.klaytn.finder.domain.mysql.BaseRepository
import org.springframework.stereotype.Repository

@Repository
interface ContractCodeRepository : BaseRepository<ContractCode> {
    fun findByContractAddress(contractAddress: String): ContractCode?

    fun existsByContractAddress(contractAddress: String): Boolean
}
