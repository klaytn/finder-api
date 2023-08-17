package io.klaytn.finder.config.jpa

import com.zaxxer.hikari.HikariConfig
import io.klaytn.finder.infra.db.shard.ShardNum
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "spring.db")
data class FinderDbProperties(
    val set01: DbProperties,
    val set02: DbProperties,
    val set03: DbProperties,
    val set04: DbProperties,
) {
    data class DbProperties(
        val dataSource: HikariConfig,
        val jpa: JpaProperties,
        val hibernate: HibernateProperties,

        val sharding: Boolean = false,
        val shardDataSources: Map<ShardNum, HikariConfig>? = null
    )
}
