package io.klaytn.finder.domain.mysql.set1

import io.klaytn.finder.domain.mysql.BaseRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
interface ContractRepository : BaseRepository<Contract> {
    fun findAllByContractAddressIn(addresses: List<String>): List<Contract>

    fun findAllByImplementationAddress(implementationAddress: String, pageable: Pageable): Page<ContractAddress>
    fun findFirst2ByImplementationAddress(implementationAddress: String): List<ContractAddress>
    fun existsByImplementationAddress(address: String): Boolean
}

interface ContractAddress {
    val contractAddress: String
}
