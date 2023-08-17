package io.klaytn.finder.service.governancecouncil

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import io.klaytn.commons.model.env.Phase
import io.klaytn.commons.model.request.SimpleRequest
import io.klaytn.commons.utils.Jackson
import io.klaytn.commons.utils.logback.logger
import io.klaytn.commons.utils.okhttp.OkHttpClientBuilder
import io.klaytn.commons.utils.retrofit2.Retrofit2Creator
import io.klaytn.commons.utils.retrofit2.orElseThrow
import io.klaytn.finder.domain.common.GovernanceCouncilContractType
import io.klaytn.finder.infra.cache.CacheName
import io.klaytn.finder.infra.client.KlaytnSquareClient
import io.klaytn.finder.service.caver.TestCaverChainType
import io.klaytn.finder.service.db.TestDbConstant
import io.klaytn.finder.service.redis.TestRedisConstants
import org.junit.jupiter.api.Test
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import java.sql.Timestamp
import java.time.Duration

data class GovernanceCouncilTestConfig(
    val phase: Phase,
    val chainType: TestCaverChainType,
    val dbType: TestDbConstant.TestDbType
)

class GovernanceCouncilTest {
    private val gcTestConfig =
        mapOf(
            TestCaverChainType.CYPRESS to
                    GovernanceCouncilTestConfig(
                        Phase.prod,
                        TestCaverChainType.CYPRESS,
                        TestDbConstant.TestDbType.SET0101),
    )

    @Test
    fun checkAndCreateOrUpdate() {
        val dryRun = false
        val testConfig = gcTestConfig[TestCaverChainType.CYPRESS]!!

        val objectMapper = jacksonMapperBuilder().build()
        val squareApiUrl = "https://square-api.klaytn.foundation"
        val squareWebUrl = "https://square.klaytn.foundation"

        val klaytnSquareClient =
            Retrofit2Creator(
                OkHttpClientBuilder().build(),
                squareApiUrl,
                Jackson.mapper(),
                KlaytnSquareClient::class).create()

        val governanceCouncils = klaytnSquareClient.getGovernanceCouncils().orElseThrow { IllegalStateException() }
        val governanceCouncilMap =
            governanceCouncils.result.map {
                klaytnSquareClient.getGovernanceCouncil(it.id).orElseThrow { IllegalStateException() }
            }.associate { it.result.id to it.result }

        if(dryRun) {
            governanceCouncils.result.forEach {
                println("[square] id:${it.id}, name:${it.name}, joinedAt:${it.joinedAt}, thumbnail:${it.thumbnail}")

                val governanceCouncil = governanceCouncilMap[it.id]!!
                governanceCouncil.contracts.forEach { gcContract ->
                    println(" [${it.name}-contract] address:${gcContract.address}, type:${gcContract.type}")
                }
            }
            return
        }

        val redisConnectionFactory = TestRedisConstants.getRedisConnectionFactory()
        val redisTemplate = TestRedisConstants.getStringRedisTemplate(redisConnectionFactory)

        val hikariDataSource = TestDbConstant.getDatasource(testConfig.phase, testConfig.chainType, testConfig.dbType)
        try {
            println("\n\n[[[ Governance Councils ]]]")
            hikariDataSource.connection.use { conn ->
                conn.autoCommit = false
                conn.prepareStatement(
                    """
                    insert into
                        governance_councils(`square_id`, `square_link`, `name`, `thumbnail`, `website`, `joined_at`, `activated_at`)
                    values
                        (?,?,?,?,?,?,?)
                    ON DUPLICATE KEY UPDATE
                        square_link=?,
                        `name`=?,
                        `thumbnail`=?,
                        `website`=?,
                        `joined_at`=?,
                        `activated_at`=?
                """.trimIndent()
                ).use { preparedStatement ->
                    governanceCouncils.result.forEach {
//                        val klaytnSquareGovernanceCouncilConfig =
//                            if(!it.gcConfigBySite.isNullOrBlank()) {
//                                objectMapper.readValue(
//                                    it.gcConfigBySite!!.toByteArray(),
//                                    object : TypeReference<KlaytnSquareGovernanceCouncilConfig>() {})
//                            } else {
//                                null
//                            }
//                        val activatedAt =
//                            if(klaytnSquareGovernanceCouncilConfig?.status.equals("onBoarding", true)) {
//                                null
//                            } else {
//                                it.joinedAt
//                            }
                        val activatedAt = it.joinedAt

                        val squareDetailWebUrl = "$squareWebUrl/GC/Detail?id=${it.id}"
                        println("[square] id:${it.id}, name:${it.name}, link:${squareDetailWebUrl}, joinedAt:${it.joinedAt}")

                        val thumbnail = it.thumbnail

                        val website =
                            with(it.website) {
                                if(this.startsWith("[")) {
                                    val sites = objectMapper.readValue(
                                        this.toByteArray(), object : TypeReference<List<String>>() {})
                                        .filter { site -> site.isNotBlank() }
                                    objectMapper.writeValueAsString(sites)
                                } else {
                                    objectMapper.writeValueAsString(listOf(this))
                                }
                            }

                        preparedStatement.setLong(1, it.id)
                        preparedStatement.setString(2, squareDetailWebUrl)
                        preparedStatement.setString(3, it.name)
                        preparedStatement.setString(4, thumbnail)
                        preparedStatement.setString(5, website)
                        preparedStatement.setTimestamp(6, Timestamp(it.joinedAt.time))
                        preparedStatement.setTimestamp(7, Timestamp(activatedAt.time))
                        preparedStatement.setString(8, squareDetailWebUrl)
                        preparedStatement.setString(9, it.name)
                        preparedStatement.setString(10, thumbnail)
                        preparedStatement.setString(11, website)
                        preparedStatement.setTimestamp(12, Timestamp(it.joinedAt.time))
                        preparedStatement.setTimestamp(13, Timestamp(activatedAt.time))
                        preparedStatement.execute()
                        preparedStatement.clearParameters()
                    }
                    conn.commit()
                }

                println("\n\n[[[ Governance Council Contracts ]]]")
                conn.prepareStatement(
                    """
                    insert ignore into
                        governance_council_contracts(`square_id`, `address`, `address_type`)
                    values
                        (?,?,?)
                """.trimIndent()
                ).use { preparedStatement ->
                    governanceCouncils.result.forEach {
                        val governanceCouncil = governanceCouncilMap[it.id]!!
                        println("[square] id:${it.id}, name:${it.name}")
                        governanceCouncil.contracts.forEach { gcContract ->
                            println(" [${it.name}-contract] address:${gcContract.address}, type:${gcContract.type}")

                            preparedStatement.setLong(1, it.id)
                            preparedStatement.setString(2, gcContract.address)
                            preparedStatement.setInt(3, GovernanceCouncilContractType.of(gcContract.type).value)
                            preparedStatement.execute()

                            preparedStatement.clearParameters()
                        }
                    }
                    conn.commit()
                }
            }

            println("\n\n[[[ Cache Flush ]]]")
            val cachePrefixKey = "finder/${testConfig.chainType.name.lowercase()}/cache"
            governanceCouncils.result.forEach {
                redisTemplate.delete(
                    "${cachePrefixKey}/${CacheName.GOVERNANCE_COUNCIL_BY_SQUARE_ID}::${it.id}")
                redisTemplate.delete(
                    "${cachePrefixKey}/${CacheName.GOVERNANCE_COUNCIL_CONTRACT_IDS_BY_SQUARE_ID}::${it.id}")

                val governanceCouncil = governanceCouncilMap[it.id]!!
                governanceCouncil.contracts.forEach { gcContract ->
                    redisTemplate.delete(
                        "${cachePrefixKey}/${CacheName.GOVERNANCE_COUNCIL_CONTRACT_ID_BY_ADDRESS}::${gcContract.address}")
                }
            }

//            println("\n\n[[[ update tags ]]]")
//            val finderPrivateApiClient = Retrofit2Creator(OkHttpClientBuilder().build(),
//                "https://stag-cypress-api.klaytnfinder.io",
//                Jackson.mapper(), FinderPrivateApiClient::class).create()
//            governanceCouncils.result.forEach {
//                val governanceCouncil = governanceCouncilMap[it.id]!!
//                governanceCouncil.contracts.forEach { gcContract ->
//                    println(" [${it.name}-contract] address:${gcContract.address}, type:${gcContract.type}")
//                    val tagName =
//                        when(GovernanceCouncilContractType.of(gcContract.type)) {
//                            GovernanceCouncilContractType.NODE -> "gc_node"
//                            GovernanceCouncilContractType.STAKING -> "gc_staking"
//                            GovernanceCouncilContractType.REWARD -> "gc_reward"
//                            else -> {
//                                ""
//                            }
//                        }
//
//                    finderPrivateApiClient.updateAccountTags(gcContract.address, SimpleRequest(listOf(tagName)))
//                        .execute()
//                        .body()?.let { response ->
//                            println("update tags : ${response.address} -> ${response.tags}")
//                        }
//                }
//            }
        } finally {
            redisConnectionFactory.destroy()
            hikariDataSource.close()
        }
    }

    @Test
    fun deactivateGC() {
        val testConfig = gcTestConfig[TestCaverChainType.CYPRESS]!!
        val squareId = Long.MAX_VALUE

        val redisConnectionFactory = TestRedisConstants.getRedisConnectionFactory()
        val redisTemplate = TestRedisConstants.getStringRedisTemplate(redisConnectionFactory)

        val hikariDataSource = TestDbConstant.getDatasource(testConfig.phase, testConfig.chainType, testConfig.dbType)
        try {
            hikariDataSource.connection.use { conn ->
                conn.autoCommit = false
                conn.prepareStatement(
                    """
                        update 
                            governance_councils 
                        set 
                            activated_at=?, 
                            deactivated_at=? 
                        where 
                            square_id=?
                    """.trimIndent()
                ).use { preparedStatement ->
                    preparedStatement.setTimestamp(1, null)
                    preparedStatement.setTimestamp(2, Timestamp(System.currentTimeMillis()))
                    preparedStatement.setLong(3, squareId)
                    preparedStatement.execute()
                }
                conn.commit()
            }

            val cachePrefixKey = "finder/${testConfig.chainType.name.lowercase()}/cache"
            redisTemplate.delete(
                "${cachePrefixKey}/${CacheName.GOVERNANCE_COUNCIL_BY_SQUARE_ID}::${squareId}"
            )
            redisTemplate.delete(
                "${cachePrefixKey}/${CacheName.GOVERNANCE_COUNCIL_CONTRACT_IDS_BY_SQUARE_ID}::${squareId}"
            )
        } finally {
            redisConnectionFactory.destroy()
            hikariDataSource.close()
        }
    }
}

interface FinderPrivateApiClient {
    @PUT("/papi/v1/accounts/{address}/tags")
    fun updateAccountTags(
        @Path("address") address: String,
        @Body simpleRequest: SimpleRequest<List<String>>
    ): Call<AccountTagUpdateResponse>
}

data class AccountTagUpdateResponse(
    @JsonProperty("address")
    val address: String,
    @JsonProperty("tags")
    val tags: List<String>,
)
