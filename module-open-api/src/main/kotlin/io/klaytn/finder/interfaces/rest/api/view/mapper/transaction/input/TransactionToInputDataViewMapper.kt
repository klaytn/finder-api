package io.klaytn.finder.interfaces.rest.api.view.mapper.transaction.input

import com.klaytn.caver.abi.ABI
import com.klaytn.caver.abi.datatypes.Type
import io.klaytn.commons.model.mapper.Mapper
import io.klaytn.commons.utils.logback.logger
import io.klaytn.finder.domain.mysql.set1.Transaction
import io.klaytn.finder.infra.utils.SignatureDecodeUtils
import io.klaytn.finder.interfaces.rest.api.view.model.transaction.DecodedParam
import io.klaytn.finder.interfaces.rest.api.view.model.transaction.DecodedValue
import io.klaytn.finder.interfaces.rest.api.view.model.transaction.TransactionInputDataView
import io.klaytn.finder.service.SignatureService
import org.apache.commons.codec.binary.Hex
import org.springframework.stereotype.Component

@Component
class TransactionToInputDataViewMapper(
    private val signatureService: SignatureService,
) : Mapper<Transaction, TransactionInputDataView> {

    private val logger = logger(this::class.java)

    override fun transform(source: Transaction) = transform(source.input!!)

    fun transform(input: String): TransactionInputDataView {
        if (input.length < 10) {
            return TransactionInputDataView(input, null, "")
        }

        val bytes = input.substring(0, 10)
        val encoded = input.substring(10)
        var functionSignatures = signatureService.getFunctionSignatures(bytes)

        // If the parameters are empty, compare in reverse order.
        if(encoded.isEmpty()) {
            functionSignatures = functionSignatures.filter { it.textSignature.endsWith("()") }
        }

        val decoded = functionSignatures.firstNotNullOfOrNull {
            runCatching {
                val functionSignature = SignatureDecodeUtils.getTypeOfSignature(it.textSignature)

                if (encoded.isEmpty() && functionSignature.size == 1 && functionSignature[0].isEmpty()) Pair(
                    it.textSignature,
                    emptyList<Type<Any>>()
                ) else Pair(it.textSignature, ABI.decodeParameters(functionSignature, encoded))
            }.getOrNull()
        }

        val utf8Value = runCatching {
            Hex.decodeHex(input.substring(2)).decodeToString()
        }.getOrElse { "" }

        return TransactionInputDataView(
            originalValue = input,
            decodedValue = decoded?.let {
                DecodedValue(
                    methodId = bytes,
                    signature = it.first,
                    parameters = it.second.map { type ->
                        DecodedParam(
                            type = SignatureDecodeUtils.type(type),
                            name = null,
                            value = SignatureDecodeUtils.value(type.value)
                        )
                    }
                )
            },
            utf8Value = utf8Value
        )
    }
}