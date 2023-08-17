package io.klaytn.finder.worker

import io.ipfs.api.IPFS
import io.ipfs.multihash.Multihash
import org.junit.jupiter.api.Test

class NftTokenUriContentRefreshRequestTest {
    @Test
    fun test() {
        val redisConnectionFactory = TestRedisConstants.getRedisConnectionFactory()
        val redisTemplate = TestRedisConstants.getStringRedisTemplate(redisConnectionFactory)
        try {
            redisTemplate
                    .opsForList()
                    .leftPush(
                            "finder-worker/redis-consumer/http_nft_token_uris",
                            """{
                |"chain":"cypress",
                |"contract_address":"0x320ad2ee12bfd7455c9d23d297943ce68268af4e",
                |"token_id":"3461",
                |"token_uri":"https://media.klipwallet.com/drops/metadata/metadata_1000180014.json"}""".trimMargin()
                    )

            //        redisTemplate.opsForList().leftPush(
            //            "finder-worker/redis-consumer/ifps_nft_token_uris",
            //            """{
            //                |"chain":"cypress",
            //                |"contract_address":"0x320ad2ee12bfd7455c9d23d297943ce68268af4e",
            //                |"token_id":"3464",
            //
            // |"token_uri":"ipfs://QmYE3An4kkYFCuLELt5uNdSVJu8UMQo354QTX6FLntZTCk"}""".trimMargin())
        } finally {
            redisConnectionFactory.destroy()
        }
    }

    @Test
    fun test2() {
        val ipfs = IPFS("/ip4/127.0.0.1/tcp/8080")
        ipfs.refs.local()
        val filePointer = Multihash.fromBase58("QmYE3An4kkYFCuLELt5uNdSVJu8UMQo354QTX6FLntZTCk")
        val fileContents = ipfs.cat(filePointer)
        println(String(fileContents))
    }

    @Test
    fun test3() {
        val ipfs = IPFS("ipfs.infura.io", 5001, "/api/v0/", true)
        val filePointer = Multihash.fromBase58("QmYE3An4kkYFCuLELt5uNdSVJu8UMQo354QTX6FLntZTCk")
        val fileContents = ipfs.cat(filePointer)
        println(String(fileContents))
    }
}
