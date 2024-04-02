package io.klaytn.finder.interfaces.rest.api.view.mapper.transaction.input

import com.klaytn.caver.abi.ABI
import com.klaytn.caver.abi.datatypes.Type
import io.klaytn.commons.model.mapper.Mapper
import io.klaytn.commons.model.mapper.ListMapper
import io.klaytn.commons.utils.logback.logger
import io.klaytn.finder.domain.mysql.set1.Transaction
import io.klaytn.finder.infra.utils.SignatureDecodeUtils
import io.klaytn.finder.interfaces.rest.api.view.model.transaction.DecodedParam
import io.klaytn.finder.interfaces.rest.api.view.model.transaction.DecodedValue
import io.klaytn.finder.interfaces.rest.api.view.model.transaction.TransactionInputDataView
import io.klaytn.finder.service.SignatureService
import org.apache.commons.codec.binary.Hex
import org.springframework.stereotype.Component
import io.klaytn.finder.service.Signature

@Component
class TransactionToInputDataViewMapper(
    private val signatureService: SignatureService,
) : Mapper<Transaction, TransactionInputDataView>, ListMapper<Transaction, TransactionInputDataView>{

    private val logger = logger(this::class.java)

    override fun transform(source: Transaction) = transformSingle(source.input!!)

    fun transformSingle(input: String): TransactionInputDataView {
        if (input.length < 10) {
            return TransactionInputDataView(input, null, "")
        }
        val bytes = toBytes(input)
        var functionSignatures = signatureService.getFunctionSignatures(bytes)
        val encoded = toEncodedValue(input)

        // If the parameters are empty, compare in reverse order.
        if(encoded.isEmpty()) {
            functionSignatures = functionSignatures.filter { it.textSignature.endsWith("()") }
        }
        val decoded = toDecodedValue(functionSignatures, encoded)
        val utf8Value = toUtf8Value(input)
        return toTransactionInputDataView(input, bytes, decoded, utf8Value)
    }

    fun transform_deprecated(input: String): TransactionInputDataView {
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

    fun toBytes(input: String): String {
        return input.substring(0, 10)
    }

    fun toEncodedValue(input: String): String {
        return input.substring(10)
    }

    fun toDecodedValue(signatures: List<Signature>, encoded: String): Pair<String, List<Type<Any>>>? {
        return signatures.firstNotNullOfOrNull {
            runCatching {
                val functionSignature = SignatureDecodeUtils.getTypeOfSignature(it.textSignature)

                if (encoded.isEmpty() && functionSignature.size == 1 && functionSignature[0].isEmpty()) {
                    Pair(it.textSignature, emptyList<Type<Any>>())
                } else {
                    Pair(it.textSignature, ABI.decodeParameters(functionSignature, encoded))
                }
            }.getOrNull()
        }
    }

    fun toUtf8Value(input: String): String {
        return try {
            Hex.decodeHex(input.substring(2)).decodeToString()
        } catch (e: Exception) {
            ""
        }
    }

    fun toTransactionInputDataView(input: String, bytes: String, decoded: Pair<String, List<Type<Any>>>?, utf8Value: String): TransactionInputDataView {
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

    override fun transform(source: List<Transaction>) = transformList(source.mapNotNull {it.input})

    fun transformList(inputList: List<String>): List<TransactionInputDataView> {
        val bytes0x = inputList.filter { it.length < 10 }.map { TransactionInputDataView(it, null, "") }
        val _inputList = inputList.filter { it.length >= 10 }
        val bytesList = _inputList.filter{ it.length >= 10}.map{ toBytes(it) }
        val functionSignaturesMap = signatureService.getFunctionSignatures(bytesList)
        val encodedList = _inputList.map{ toEncodedValue(it) }
        val decodedList = encodedList.mapIndexed { index, encoded ->
            val signatures = functionSignaturesMap.getOrDefault(bytesList[index], emptyList())
            toDecodedValue(signatures, encoded)
        }
        val utf8List = _inputList.map{ toUtf8Value(it) }
        return _inputList.mapIndexed { index, input ->
            toTransactionInputDataView(input, bytesList[index], decodedList[index], utf8List[index])
        } + bytes0x
    }
}