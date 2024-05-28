package io.klaytn.finder.domain.mysql.set4

import io.klaytn.finder.domain.mysql.BaseRepository
import org.springframework.stereotype.Repository

@Repository
interface TokenInfoRepository : BaseRepository<TokenInfo> {
    fun findByCmcIdIsNotNull(): List<TokenInfo>
}