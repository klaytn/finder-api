package io.klaytn.finder.domain.mysql.set1.signature

import io.klaytn.finder.domain.mysql.BaseEntity
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "event_signatures")
data class EventSignature(
    @Column(name="4byte_id")
    val fourByteId: Long?,

    @Column
    val hexSignature: String,

    @Column(columnDefinition = "TEXT")
    val textSignature: String,

    @Column(columnDefinition = "TINYINT")
    val primary: Boolean?,
) : BaseEntity()