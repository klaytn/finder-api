package io.klaytn.finder.domain.mysql.set1.signature

import io.klaytn.finder.domain.mysql.BaseRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface FunctionSignatureRepository : BaseRepository<FunctionSignature> {
    fun findAllByBytesSignature(bytesSignature: String): List<FunctionSignature>
    fun findByFourByteId(fourByteId: Long): FunctionSignature?

    @Modifying
    @Query("UPDATE FunctionSignature f SET f.primary = :primary WHERE f.id = :id")
    fun updatePrimary(id: Long, primary: Boolean): Int
}