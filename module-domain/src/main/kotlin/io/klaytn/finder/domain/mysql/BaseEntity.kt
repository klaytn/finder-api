package io.klaytn.finder.domain.mysql

import java.time.LocalDateTime
import javax.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp

@MappedSuperclass
abstract class BaseEntity(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long = 0L,
        @Column(nullable = false, updatable = false)
        @CreationTimestamp
        var createdAt: LocalDateTime? = null,
        @Column @UpdateTimestamp var updatedAt: LocalDateTime? = null,
)
