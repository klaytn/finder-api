package io.klaytn.finder.service.caver

import io.klaytn.commons.utils.Jackson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.jupiter.api.Test

class TestCaverInternalTransaction {
    private val cypress_arch = "http://10.2.40.246:8551"

    @Test
    fun getInternalTransaction() {
        val txHash = "0x406ca1ceb3d77c8630a0d320dff70ce0ff91eebfbb8668c4576a6d74f6785598"
        val client = OkHttpClient()
        val body = """
            {"jsonrpc":"2.0","method":"debug_traceTransaction","params":["$txHash", {"tracer": "callTracer"}],"id":1}
            """.trimIndent().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(cypress_arch)
            .post(body)
            .addHeader("accept", "application/json")
            .addHeader("content-type", "application/json")
            .build()

        val response = client.newCall(request).execute()
        val result = Jackson.mapper().readValue(response.body?.bytes(), InternalTransactionContentResp::class.java)
        val internalTransactionContent = result.result
    }
}

data class InternalTransactionContentResp(
    val jsonrpc: String,
    val id: Int,
    val result: InternalTransactionContent
)

data class InternalTransactionContent(
    val type: String,
    val from: String?,
    val to: String?,
    val value: String?,
    val gas: String?,
    val gasUsed: String?,
    val input: String?,
    val output: String?,
    val time: String?,
    val calls: List<InternalTransactionCalls>?,
    val error: String?,
    val reverted: Reverted?,
)

data class InternalTransactionCalls(
    val callId: Int?,
    val parentCallId: Int?,
    val type: String,
    val from: String?,
    val to: String?,
    val gas: String?,
    val gasUsed: String?,
    val input: String?,
    val output: String?,
    val value: String?,
    val error: String?,
    val calls: List<InternalTransactionCalls>?)

data class Reverted(
    val contract: String, 
    val message: String?)