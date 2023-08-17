package io.klaytn.finder.domain.mysql.set1.approve

import io.klaytn.finder.domain.mysql.BaseRepository
import io.klaytn.finder.domain.mysql.EntityId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
interface AccountTokenApproveRepository : BaseRepository<AccountTokenApprove> {
    fun findAllByAccountAddress(accountAddress: String, pageable: Pageable): Page<EntityId>
    fun findAllByAccountAddressAndSpenderAddress(
        accountAddress: String, spenderAddress: String, pageable: Pageable
    ): Page<EntityId>
}
