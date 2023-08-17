package io.klaytn.finder.config.jpa

import com.zaxxer.hikari.HikariDataSource
import io.klaytn.finder.infra.db.DbConstants
import io.klaytn.finder.infra.db.shard.ShardDataSourceRouter
import io.klaytn.finder.infra.db.shard.ShardNum
import io.klaytn.finder.infra.db.shard.selector.AccountAddressShardNumSelector
import io.klaytn.finder.infra.db.shard.selector.BlockShardNumSelector
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import javax.persistence.EntityManagerFactory
import javax.sql.DataSource

@Configuration
@EnableJpaRepositories(
    basePackages = [DbConstants.baseDomainPackage, DbConstants.set2DomainPackage],
    entityManagerFactoryRef = DbConstants.set2EntityManagerFactory,
    transactionManagerRef = DbConstants.set2TransactionManager
)
@EnableConfigurationProperties(FinderDbProperties::class)
class FinderDbSet2Config {
    @Bean
    fun set2EntityManagerFactory(
        finderDbProperties: FinderDbProperties,
        set2DataSource: DataSource,
    ): LocalContainerEntityManagerFactoryBean {
        val enableJpaRepositories = this.javaClass.getAnnotation(EnableJpaRepositories::class.java)

        val dataSource = set2DataSource
        val vendorAdapter = HibernateJpaVendorAdapter()

        val dbProperty = finderDbProperties.set02
        val vendorProperties = dbProperty.hibernate.determineHibernateProperties(
            java.util.LinkedHashMap(dbProperty.jpa.properties), HibernateSettings()
        )

        val localContainerEntityManagerFactoryBean = LocalContainerEntityManagerFactoryBean()
        localContainerEntityManagerFactoryBean.setPackagesToScan(*enableJpaRepositories.basePackages)
        localContainerEntityManagerFactoryBean.persistenceUnitName = DbConstants.set2EntityManager
        localContainerEntityManagerFactoryBean.dataSource = dataSource
        localContainerEntityManagerFactoryBean.jpaVendorAdapter = vendorAdapter
        localContainerEntityManagerFactoryBean.jpaPropertyMap.putAll(vendorProperties)
        return localContainerEntityManagerFactoryBean
    }

    @Bean
    fun set2DataSource(finderDbProperties: FinderDbProperties): ShardDataSourceRouter {
        val dbProperties = finderDbProperties.set02
        val rootDataSourceProperties = dbProperties.dataSource

        val targetDataSources = mutableMapOf<Any, Any>()
        if(dbProperties.sharding) {
            val shardDataSources = dbProperties.shardDataSources
            shardDataSources?.forEach {
                it.value.driverClassName = rootDataSourceProperties.driverClassName
                it.value.username = rootDataSourceProperties.username
                it.value.password = rootDataSourceProperties.password

                targetDataSources[it.key] = HikariDataSource(it.value)
            }
        } else {
            targetDataSources[ShardNum.SHARD_0] = HikariDataSource(rootDataSourceProperties)
        }

        val routingDataSource = ShardDataSourceRouter()
        routingDataSource.setTargetDataSources(targetDataSources)
        return routingDataSource
    }

    @Bean
    fun set2TransactionManager(set2EntityManagerFactory: EntityManagerFactory): PlatformTransactionManager {
        val transactionManager = JpaTransactionManager()
        transactionManager.entityManagerFactory = set2EntityManagerFactory
        return transactionManager
    }

    @Bean
    fun set2BlockShardNumSelector(set2DataSource: ShardDataSourceRouter) =
        BlockShardNumSelector(set2DataSource.resolvedDataSources.size)

    @Bean
    fun set2AccountAddressShardNumSelector(set2DataSource: ShardDataSourceRouter) =
        AccountAddressShardNumSelector(set2DataSource.resolvedDataSources.size)
}
