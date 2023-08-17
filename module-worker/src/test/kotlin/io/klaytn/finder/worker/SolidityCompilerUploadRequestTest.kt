package io.klaytn.finder.worker

import org.junit.jupiter.api.Test

class SolidityCompilerUploadRequestTest {
    @Test
    fun test() {
        val redisConnectionFactory = TestRedisConstants.getRedisConnectionFactory()
        val redisTemplate = TestRedisConstants.getStringRedisTemplate(redisConnectionFactory)
        try {
            redisTemplate
                    .opsForList()
                    .leftPush(
                            "finder-worker/redis-consumer/solidity_compiler_uploads",
                            """{
                |"os_path":"linux-amd64",
                |"build_path":"solc-linux-amd64-v0.8.17+commit.8df45f5f"
                |}""".trimMargin()
                    )

            redisTemplate
                    .opsForList()
                    .leftPush(
                            "finder-worker/redis-consumer/solidity_compiler_uploads",
                            """{
                |"os_path":"macosx-amd64",
                |"build_path":"solc-macosx-amd64-v0.8.17+commit.8df45f5f"
                |}""".trimMargin()
                    )
        } finally {
            redisConnectionFactory.destroy()
        }
    }
}
