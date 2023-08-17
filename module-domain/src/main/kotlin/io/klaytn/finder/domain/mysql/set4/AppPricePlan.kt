package io.klaytn.finder.domain.mysql.set4

import io.klaytn.finder.domain.mysql.BaseEntity
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "app_price_plans")
data class AppPricePlan(
    @Column
    val name: String,

    @Column
    val requestLimitPerSecond: Long,

    @Column
    val requestLimitPerDay: Long,

    @Column
    val requestLimitPerMonth: Long,

    @Column(columnDefinition = "TEXT")
    val description: String?,

    @Column(columnDefinition = "TINYINT")
    val displayOrder: Int,

    @Column(columnDefinition = "TINYINT")
    val hidden: Boolean,

    @Column(columnDefinition = "TINYINT")
    val allowLimitOver: Boolean,

    @Column
    val activatedAt: LocalDateTime?,

    @Column
    val deactivatedAt: LocalDateTime?,
) : BaseEntity() {
    companion object {
        fun getAllRequestLimitPlan() =
            AppPricePlan(
                name = "all request limit",
                requestLimitPerSecond = 0,
                requestLimitPerDay = 0,
                requestLimitPerMonth = 0,
                description = "all request is limited.",
                displayOrder = 0,
                hidden = true,
                allowLimitOver = false,
                activatedAt = null,
                deactivatedAt = null)
    }
}
