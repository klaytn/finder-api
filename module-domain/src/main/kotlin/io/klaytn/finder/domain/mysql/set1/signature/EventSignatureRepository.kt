package io.klaytn.finder.domain.mysql.set1.signature

import io.klaytn.finder.domain.mysql.BaseRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface EventSignatureRepository : BaseRepository<EventSignature> {
    fun findAllByHexSignature(hexSignature: String): List<EventSignature>
    fun findByFourByteId(fourByteId: Long): EventSignature?

    @Modifying
    @Query("UPDATE EventSignature e SET e.primary = :primary WHERE e.id = :id")
    fun updatePrimary(id: Long, primary: Boolean): Int
}