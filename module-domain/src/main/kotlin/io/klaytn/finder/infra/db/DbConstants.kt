package io.klaytn.finder.infra.db

class DbConstants {
    companion object {
        const val baseDomainPackage = "io.klaytn.finder.domain.common"

        const val set1DomainPackage = "io.klaytn.finder.domain.mysql.set1"
        const val set2DomainPackage = "io.klaytn.finder.domain.mysql.set2"
        const val set3DomainPackage = "io.klaytn.finder.domain.mysql.set3"
        const val set4DomainPackage = "io.klaytn.finder.domain.mysql.set4"

        const val set1EntityManagerFactory = "set1EntityManagerFactory"
        const val set2EntityManagerFactory = "set2EntityManagerFactory"
        const val set3EntityManagerFactory = "set3EntityManagerFactory"
        const val set4EntityManagerFactory = "set4EntityManagerFactory"

        const val set1EntityManager = "set1EntityManager"
        const val set2EntityManager = "set2EntityManager"
        const val set3EntityManager = "set3EntityManager"
        const val set4EntityManager = "set4EntityManager"

        const val set1TransactionManager = "set1TransactionManager"
        const val set2TransactionManager = "set2TransactionManager"
        const val set3TransactionManager = "set3TransactionManager"
        const val set4TransactionManager = "set4TransactionManager"
    }
}