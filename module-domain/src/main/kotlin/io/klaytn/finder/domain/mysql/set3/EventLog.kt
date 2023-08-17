package io.klaytn.finder.domain.mysql.set3

import io.klaytn.finder.domain.mysql.BaseEntity
import io.klaytn.finder.infra.db.jpa.JsonStringToStringListConverter
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "event_logs")
data class EventLog(
    @Column
    val logIndex: Int,

    @Column
    val transactionIndex: Int?,

    @Column
    val transactionHash: String,

    /**
     * When todo data is received, change to nullable (including the database).
     */
    @Column
    val signature: String?,


    @Column
    val blockHash: String,

    @Column
    val blockNumber: Long,

    @Column
    val address: String,

    @Column(columnDefinition = "TEXT")
    val data: String,

    @Column(columnDefinition = "TEXT")
    @Convert(converter = JsonStringToStringListConverter::class)
    val topics: List<String>,

    @Column(columnDefinition = "TINYINT")
    var removed: Boolean?,
) : BaseEntity()
