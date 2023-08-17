package io.klaytn.finder.worker

import io.klaytn.finder.domain.redis.NftTokenUriRefreshRequest
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.Test
import java.io.FileInputStream

class NftTokenUriRefreshRequestTest {
    @Test
    fun test_with_json_string() {
        val redisConnectionFactory = TestRedisConstants.getRedisConnectionFactory()
        val redisTemplate = TestRedisConstants.getStringRedisTemplate(redisConnectionFactory)
        try {
            redisTemplate.opsForList().leftPush(
                "finder-worker/redis-consumer/nft_token_uri_refresh_requests",
                """{
                |"chain":"cypress",
                |"contract_address":"0x72e0eca792ad1a37d81f208b946250dbe879e10d",
                |"token_id":"20220622063"
                }""".trimMargin()
            )
        } finally {
            redisConnectionFactory.destroy()
        }
    }

    @Test
    fun test_with_object() {
        val redisConnectionFactory = TestRedisConstants.getRedisConnectionFactory()
        val redisTemplate = TestRedisConstants.getGenericRedisTemplate(
            redisConnectionFactory, NftTokenUriRefreshRequest::class.java
        )

        try {
            redisTemplate.opsForList().leftPush(
                "finder-worker/redis-consumer/nft_token_uri_refresh_requests",
                NftTokenUriRefreshRequest(
                    chain = "cypress",
                    contractAddress = "0x72e0eca792ad1a37d81f208b946250dbe879e10d",
                    tokenId = "20220622063"
                )
            )
        } finally {
            redisConnectionFactory.destroy()
        }
    }

    /**
        SELECT contract_address, token_id  FROM nft_inventories
        where contract_address='0x0892ed3424851d2bab4ac1091fa93c9851eb5d7d'
        INTO OUTFILE S3 's3://AWS_S3_PRIVATE_BUCKET/finder/cypress/nft_inventories.cvs'
        FORMAT CSV OVERWRITE ON;
     */
//    @Test
    fun refresh_multiple_tokenIds() {
        val redisConnectionFactory = TestRedisConstants.getRedisConnectionFactory()
        val redisTemplate = TestRedisConstants.getStringRedisTemplate(redisConnectionFactory)
        try {
            val chain = "cypress"
            val dataPath = "__path__"
            val sources = IOUtils.readLines(FileInputStream(dataPath), Charsets.UTF_8)
            sources.forEach {
                val split = it.split(",")

                val contractAddress = split[0].replace("\"", "")
                val tokenId = split[1].replace("\"", "")

                redisTemplate.opsForList().leftPush(
                    "finder-worker/redis-consumer/nft_token_uri_refresh_requests",
                    """{
                    |"chain":"$chain",
                    |"contract_address":"$contractAddress",
                    |"token_id":"$tokenId"
                    }""".trimMargin()
                )
            }
        } finally {
            redisConnectionFactory.destroy()
        }
    }
}