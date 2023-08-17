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
    basePackages = [DbConstants.baseDomainPackage, DbConstants.set1DomainPackage],
    entityManagerFactoryRef = DbConstants.set1EntityManagerFactory,
    transactionManagerRef = DbConstants.set1TransactionManager
)
@EnableConfigurationProperties(FinderDbProperties::class)
class FinderDbSet1Config {
    @Bean
    fun set1DataSource(finderDbProperties: FinderDbProperties): HikariDataSource =
        HikariDataSource(finderDbProperties.set01.dataSource)

    @Bean
    fun set1EntityManagerFactory(
        finderDbProperties: FinderDbProperties,
        set1DataSource: DataSource,
    ): LocalContainerEntityManagerFactoryBean {
        val enableJpaRepositories = this.javaClass.getAnnotation(EnableJpaRepositories::class.java)

        val dataSource = LazyConnectionDataSourceProxy(set1DataSource)
        val vendorAdapter = HibernateJpaVendorAdapter()

        val dbProperty = finderDbProperties.set01
        val vendorProperties = dbProperty.hibernate.determineHibernateProperties(
            java.util.LinkedHashMap(dbProperty.jpa.properties), HibernateSettings()
        )

        val localContainerEntityManagerFactoryBean = LocalContainerEntityManagerFactoryBean()
        localContainerEntityManagerFactoryBean.setPackagesToScan(*enableJpaRepositories.basePackages)
        localContainerEntityManagerFactoryBean.persistenceUnitName = DbConstants.set1EntityManager
        localContainerEntityManagerFactoryBean.dataSource = dataSource
        localContainerEntityManagerFactoryBean.jpaVendorAdapter = vendorAdapter
        localContainerEntityManagerFactoryBean.jpaPropertyMap.putAll(vendorProperties)
        return localContainerEntityManagerFactoryBean
    }

    @Bean
    fun set1TransactionManager(set1EntityManagerFactory: EntityManagerFactory): PlatformTransactionManager {
        val transactionManager = JpaTransactionManager()
        transactionManager.entityManagerFactory = set1EntityManagerFactory
        return transactionManager
    }
}
