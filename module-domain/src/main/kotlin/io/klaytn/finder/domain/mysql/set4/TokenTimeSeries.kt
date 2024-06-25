package io.klaytn.finder.domain.mysql.set4

import io.klaytn.finder.domain.mysql.BaseEntity
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "token_time_series")
data class TokenTimeSeries(
    @Column()
    val tokenInfoId: Long,

    @Column()
    val symbol: String,

    @Column()
    val price: String,

    @Column()
    val kaiaPrice: String,

    @Column()
    val changeRate: String,

    @Column()
    val volume: String,

    @Column()
    val marketCap: String,

    @Column()
    val onChainMarketCap: String,

    @Column()
    val circulatingMarketCap: String,

    @Column()
    val timestamp: Int,
) : BaseEntity()
