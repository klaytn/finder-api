package io.klaytn.finder.domain.mysql.set1.approve

import io.klaytn.finder.domain.mysql.BaseRepository
import io.klaytn.finder.domain.mysql.EntityId
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Page
import org.springframework.stereotype.Repository

@Repository
interface AccountNftApproveRepository : BaseRepository<AccountNftApprove> {
    fun findAllByAccountAddressAndApprovedAll(
        accountAddress: String, approvedAll: Boolean, pageable: Pageable
    ): Page<EntityId>

    fun findAllByAccountAddressAndSpenderAddressAndApprovedAll(
        accountAddress: String, spenderAddress: String, approvedAll: Boolean, pageable: Pageable
    ): Page<EntityId>
}
