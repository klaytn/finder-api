package io.klaytn.finder.service.signature

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import io.klaytn.commons.model.env.Phase
import io.klaytn.finder.service.caver.TestCaverChainType
import io.klaytn.finder.service.db.TestDbConstant
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource
import java.util.concurrent.atomic.AtomicInteger

class FunctionSignatureImporter {
    @Test
    fun test() {
        val dataPath = "__datapath__"
        val sources = IOUtils.readLines(ClassPathResource(dataPath).inputStream, Charsets.UTF_8)
        val counter = AtomicInteger()

        val hikariDataSource = TestDbConstant.getDatasource(Phase.prod, TestCaverChainType.CYPRESS, TestDbConstant.TestDbType.SET0101)
        hikariDataSource.connection.use {conn ->
            conn.autoCommit = false
            conn.prepareStatement(
                """
                    insert ignore into function_signatures(`4byte_id`, `bytes_signature`, `text_signature`) values(?,?,?)
                """.trimIndent()
            ).use { psmt ->
                sources.forEach {
                    val split = it.split("||")
                    val signature = Signature(split[0].toInt(), split[1], split[2])

                    psmt.setLong(1, signature.id.toLong())
                    psmt.setString(2, signature.hexSignature)
                    psmt.setString(3, signature.textSignature)
                    psmt.addBatch()

                    val currentIndex = counter.incrementAndGet()
                    if(currentIndex % 1000 == 0) {
                        println(" => $currentIndex")
                        psmt.executeBatch()
                        conn.commit()

                        psmt.clearBatch()
                    }
                }

                println(" => ${counter.get()}")
                psmt.executeBatch()
                conn.commit()
            }
        }
    }
}

data class Signature(
    val id: Int,

    @JsonProperty("hex_signature")
    @JsonAlias("hex_signature", "hex")
    val hexSignature: String,

    @JsonProperty("text_signature")
    @JsonAlias("text_signature", "text")
    val textSignature: String,
)
