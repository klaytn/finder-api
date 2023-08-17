package io.klaytn.finder.service.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.klaytn.commons.model.env.Phase
import io.klaytn.finder.service.caver.TestCaverChainType

class TestDbConstant {
    enum class TestDbType {
        SET0101,
        SET0201, SET0202, SET0203, SET0204, SET0205, SET0206, SET0207, SET0208, SET0209, SET0210,
        SET3,
        SET4
    }

    class TestMbxDbProperty(
        val jdbcMap: Map<TestCaverChainType, Map<TestDbType, String>>
    )

    companion object {
        private val dbWriterMap = mapOf(
            Phase.prod to TestMbxDbProperty(
                jdbcMap = mapOf(
                    TestCaverChainType.BAOBAB to
                    mapOf(
                        TestDbType.SET0101 to "MYSQL_BAOBAB_01_ENDPOINT:3306/finder",
                        TestDbType.SET0201 to "MYSQL_BAOBAB_02_ENDPOINT:3306/finder",
                        TestDbType.SET0202 to "MYSQL_BAOBAB_02_ENDPOINT:3306/finder",
                        TestDbType.SET0203 to "MYSQL_BAOBAB_02_ENDPOINT:3306/finder",
                        TestDbType.SET0204 to "MYSQL_BAOBAB_02_ENDPOINT:3306/finder",
                        TestDbType.SET0205 to "MYSQL_BAOBAB_02_ENDPOINT:3306/finder",
                        TestDbType.SET0206 to "MYSQL_BAOBAB_02_ENDPOINT:3306/finder",
                        TestDbType.SET0207 to "MYSQL_BAOBAB_02_ENDPOINT:3306/finder",
                        TestDbType.SET0208 to "MYSQL_BAOBAB_02_ENDPOINT:3306/finder",
                        TestDbType.SET0209 to "MYSQL_BAOBAB_02_ENDPOINT:3306/finder",
                        TestDbType.SET0210 to "MYSQL_BAOBAB_02_ENDPOINT:3306/finder",
                        TestDbType.SET3 to "MYSQL_BAOBAB_03_ENDPOINT:3306/finder",
                        TestDbType.SET4 to "MYSQL_COMMON_ENDPOINT:3306/finder_common",
                    ),

                    TestCaverChainType.CYPRESS to
                    mapOf(
                        TestDbType.SET0101 to "MYSQL_CYPRESS_01_ENDPOINT:3306/finder",
                        TestDbType.SET0201 to "MYSQL_CYPRESS_0201_ENDPOINT:3306/finder01",
                        TestDbType.SET0202 to "MYSQL_CYPRESS_0202_ENDPOINT:3306/finder02",
                        TestDbType.SET0203 to "MYSQL_CYPRESS_0203_ENDPOINT:3306/finder03",
                        TestDbType.SET0204 to "MYSQL_CYPRESS_0204_ENDPOINT:3306/finder04",
                        TestDbType.SET0205 to "MYSQL_CYPRESS_0205_ENDPOINT:3306/finder05",
                        TestDbType.SET0206 to "MYSQL_CYPRESS_0201_ENDPOINT:3306/finder06",
                        TestDbType.SET0207 to "MYSQL_CYPRESS_0202_ENDPOINT:3306/finder07",
                        TestDbType.SET0208 to "MYSQL_CYPRESS_0203_ENDPOINT:3306/finder08",
                        TestDbType.SET0209 to "MYSQL_CYPRESS_0204_ENDPOINT:3306/finder09",
                        TestDbType.SET0210 to "MYSQL_CYPRESS_0205_ENDPOINT:3306/finder10",
                        TestDbType.SET3 to "MYSQL_CYPRESS_03_ENDPOINT:3306/finder",
                        TestDbType.SET4 to "MYSQL_COMMON_ENDPOINT:3306/finder_common",
                    )
                )
            )
        )

        private val dbReaderMap = mapOf(
            Phase.prod to TestMbxDbProperty(
                jdbcMap = mapOf(
                    TestCaverChainType.BAOBAB to
                            mapOf(
                                TestDbType.SET0101 to "MYSQL_BAOBAB_01_ENDPOINT:3306/finder",
                                TestDbType.SET0201 to "MYSQL_BAOBAB_02_ENDPOINT:3306/finder",
                                TestDbType.SET0202 to "MYSQL_BAOBAB_02_ENDPOINT:3306/finder",
                                TestDbType.SET0203 to "MYSQL_BAOBAB_02_ENDPOINT:3306/finder",
                                TestDbType.SET0204 to "MYSQL_BAOBAB_02_ENDPOINT:3306/finder",
                                TestDbType.SET0205 to "MYSQL_BAOBAB_02_ENDPOINT:3306/finder",
                                TestDbType.SET0206 to "MYSQL_BAOBAB_02_ENDPOINT:3306/finder",
                                TestDbType.SET0207 to "MYSQL_BAOBAB_02_ENDPOINT:3306/finder",
                                TestDbType.SET0208 to "MYSQL_BAOBAB_02_ENDPOINT:3306/finder",
                                TestDbType.SET0209 to "MYSQL_BAOBAB_02_ENDPOINT:3306/finder",
                                TestDbType.SET0210 to "MYSQL_BAOBAB_02_ENDPOINT:3306/finder",
                                TestDbType.SET3 to "MYSQL_BAOBAB_03_ENDPOINT:3306/finder",
                                TestDbType.SET4 to "MYSQL_COMMON_ENDPOINT:3306/finder_common",
                            ),

                    TestCaverChainType.CYPRESS to
                            mapOf(
                                TestDbType.SET0101 to "MYSQL_CYPRESS_01_ENDPOINT:3306/finder",
                                TestDbType.SET0201 to "MYSQL_CYPRESS_0201_ENDPOINT:3306/finder01",
                                TestDbType.SET0202 to "MYSQL_CYPRESS_0202_ENDPOINT:3306/finder02",
                                TestDbType.SET0203 to "MYSQL_CYPRESS_0203_ENDPOINT:3306/finder03",
                                TestDbType.SET0204 to "MYSQL_CYPRESS_0204_ENDPOINT:3306/finder04",
                                TestDbType.SET0205 to "MYSQL_CYPRESS_0205_ENDPOINT:3306/finder05",
                                TestDbType.SET0206 to "MYSQL_CYPRESS_0201_ENDPOINT:3306/finder06",
                                TestDbType.SET0207 to "MYSQL_CYPRESS_0202_ENDPOINT:3306/finder07",
                                TestDbType.SET0208 to "MYSQL_CYPRESS_0203_ENDPOINT:3306/finder08",
                                TestDbType.SET0209 to "MYSQL_CYPRESS_0204_ENDPOINT:3306/finder09",
                                TestDbType.SET0210 to "MYSQL_CYPRESS_0205_ENDPOINT:3306/finder10",
                                TestDbType.SET3 to "MYSQL_CYPRESS_03_ENDPOINT:3306/finder",
                                TestDbType.SET4 to "MYSQL_COMMON_ENDPOINT:3306/finder_common",
                            )
                )
            )
        )

        fun getDatasource(phase: Phase, chainType: TestCaverChainType, dbType: TestDbType, reader: Boolean = false): HikariDataSource {
            val username = ""
            val password = ""
            val dbMap = if(reader) dbReaderMap else dbWriterMap
            val jdbcUrl = dbMap[phase]!!.jdbcMap[chainType]!![dbType]

            val hikariConfig = HikariConfig()
            hikariConfig.driverClassName = "com.mysql.cj.jdbc.Driver"
            hikariConfig.jdbcUrl = "jdbc:mysql://$jdbcUrl?serverTimezone=UTC&rewriteBatchedStatements=true"
            hikariConfig.username = username
            hikariConfig.password = password
            hikariConfig.maximumPoolSize = 1
            return HikariDataSource(hikariConfig)
        }
    }
}