package io.klaytn.finder.config.jpa

import com.zaxxer.hikari.HikariDataSource
import io.klaytn.finder.infra.db.DbConstants
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import javax.persistence.EntityManagerFactory
import javax.sql.DataSource

@Configuration
@EnableJpaRepositories(
    basePackages = [DbConstants.baseDomainPackage, DbConstants.set3DomainPackage],
    entityManagerFactoryRef = DbConstants.set3EntityManagerFactory,
    transactionManagerRef = DbConstants.set3TransactionManager
)
@EnableConfigurationProperties(FinderDbProperties::class)
class FinderDbSet3Config {
    @Bean
    fun set3DataSource(finderDbProperties: FinderDbProperties): HikariDataSource =
        HikariDataSource(finderDbProperties.set03.dataSource)

    @Bean
    fun set3EntityManagerFactory(
        finderDbProperties: FinderDbProperties,
        set3DataSource: DataSource,
    ): LocalContainerEntityManagerFactoryBean {
        val enableJpaRepositories = this.javaClass.getAnnotation(EnableJpaRepositories::class.java)

        val dataSource = LazyConnectionDataSourceProxy(set3DataSource)
        val vendorAdapter = HibernateJpaVendorAdapter()

        val dbProperty = finderDbProperties.set03
        val vendorProperties = dbProperty.hibernate.determineHibernateProperties(
            java.util.LinkedHashMap(dbProperty.jpa.properties), HibernateSettings()
        )

        val localContainerEntityManagerFactoryBean = LocalContainerEntityManagerFactoryBean()
        localContainerEntityManagerFactoryBean.setPackagesToScan(*enableJpaRepositories.basePackages)
        localContainerEntityManagerFactoryBean.persistenceUnitName = DbConstants.set3EntityManager
        localContainerEntityManagerFactoryBean.dataSource = dataSource
        localContainerEntityManagerFactoryBean.jpaVendorAdapter = vendorAdapter
        localContainerEntityManagerFactoryBean.jpaPropertyMap.putAll(vendorProperties)
        return localContainerEntityManagerFactoryBean
    }

    @Bean
    fun set3TransactionManager(set3EntityManagerFactory: EntityManagerFactory): PlatformTransactionManager {
        val transactionManager = JpaTransactionManager()
        transactionManager.entityManagerFactory = set3EntityManagerFactory
        return transactionManager
    }
}
