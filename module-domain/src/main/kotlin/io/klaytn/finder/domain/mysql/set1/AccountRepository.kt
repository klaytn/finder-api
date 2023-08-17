package io.klaytn.finder.domain.mysql.set1

import io.klaytn.finder.domain.common.ContractType
import io.klaytn.finder.domain.mysql.BaseRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
interface AccountRepository : BaseRepository<Account> {
    fun findAllByAddressIn(addresses: List<String>): List<Account>

    fun findAllByContractDeployerAddressAndContractTypeIn(
        contractDeployerAddress: String, contractType: Set<ContractType>, pageable: Pageable
    ): Page<AccountAddressOnly>
}

interface AccountAddressOnly {
    val address: String
}
