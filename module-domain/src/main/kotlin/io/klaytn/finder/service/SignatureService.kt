package io.klaytn.finder.service

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import io.klaytn.finder.service.signature.EventSignatureService
import io.klaytn.finder.service.signature.FunctionSignatureService
import org.springframework.stereotype.Service

@Service
class SignatureService(
    private val functionSignatureService: FunctionSignatureService,
    private val evenSignatureService: EventSignatureService
) {
    fun getFunctionSignatures(bytes: String) =
        functionSignatureService.getFunctionSignatureFilterWithPrimary(bytes)
            .map { Signature(it.id.toInt(), it.bytesSignature, it.textSignature) }
            .sortedByDescending {
                it.textSignature.count { ch -> ch == ',' }
            }
    fun getFunctionSignatures(bytes: List<String>): Map<String, List<Signature>> =
        functionSignatureService.getFunctionSignaturesFilterWithPrimary(bytes)
            .mapValues { (_, value) ->
                value.map { Signature(it.id.toInt(), it.bytesSignature, it.textSignature) }
                    .sortedByDescending {
                        it.textSignature.count { ch -> ch == ',' }
                    }
            }
    fun getEventSignatures(bytes: String) =
        evenSignatureService.getEventSignatureFilterWithPrimary(bytes)
            .map { Signature(it.id.toInt(), it.hexSignature, it.textSignature) }
            .sortedByDescending {
                it.textSignature.count { ch -> ch == ',' }
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

enum class SignatureType(val key: String) {
    FUNCTION("function-signatures"),
    EVENT("event-signatures")
}