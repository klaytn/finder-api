package io.klaytn.finder.domain.mysql

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean

@NoRepositoryBean
interface BaseRepository<T : BaseEntity> : JpaRepository<T, Long> {
    fun findAllByIdIn(ids: List<Long>): List<T>
}
