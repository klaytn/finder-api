package io.klaytn.finder.domain.mysql.set1

import io.klaytn.finder.domain.mysql.BaseEntity
import io.klaytn.finder.infra.db.jpa.JsonStringToStringListConverter
import io.klaytn.finder.infra.db.jpa.PebToKlayConverter
import java.math.BigDecimal
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "blocks")
data class Block(
    @Column
    val number: Long,

    @Column
    val hash: String,

    @Column
    val parentHash: String,

    @Column(columnDefinition = "TEXT")
    val logsBloom: String,

    @Column
    val transactionCount: Long,

    @Column
    val transactionsRoot: String,

    @Column
    val stateRoot: String,

    @Column
    val receiptsRoot: String,

    @Column
    val proposer: String,

    @Column
    val reward: String,

    @Column
    val blockScore: Int?,

    @Column
    val totalBlockScore: Int,

    @Column(columnDefinition = "TEXT")
    val extraData: String,

    @Column
    val size: Long,

    @Column
    val gasUsed: Int,

    @Column
    val timestamp: Int,

    @Column
    val timestampFos: Int?,

   /**
    * Value converted from timestamp to (yyyyMM, KST) format.
    * - Field for querying a specific proposer's period.
    */
    @Column
    val date: String?,

   /**
    * Only has a value when block is 0, otherwise 0x.
    */
    @Column
    val governanceData: String,

    @Column
    val voteData: String,

    @Column(columnDefinition = "TEXT")
    @Convert(converter = JsonStringToStringListConverter::class)
    val committee: List<String>,

    @Column
    @Convert(converter = PebToKlayConverter::class)
    val baseFeePerGas: BigDecimal?,
) : BaseEntity()
