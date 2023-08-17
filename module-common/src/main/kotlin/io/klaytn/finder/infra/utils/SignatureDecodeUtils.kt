package io.klaytn.finder.infra.utils

import com.klaytn.caver.abi.datatypes.*
import com.klaytn.caver.abi.datatypes.Array
import com.klaytn.caver.abi.datatypes.Int
import com.klaytn.caver.abi.datatypes.generated.Bytes32
import org.apache.commons.codec.binary.Hex

class SignatureDecodeUtils {
    companion object {
        fun getTypeOfSignature(signature: String): List<String> {
            val parameters = signature.substringAfter("(").substringBeforeLast(")")

            val sb = StringBuilder()
            val types = mutableListOf<String>()
            var tuple = false

            parameters.forEach { ch ->
                if (ch == '(') {
                    sb.append("tuple")
                    tuple = true
                }

                if (ch == ')') {
                    tuple = false
                }

                if (ch == ',') {
                    if (tuple) {
                        sb.append(ch)
                    } else {
                        types.add(sb.toString())
                        sb.clear()
                    }
                } else {
                    sb.append(ch)
                }
            }
            types.add(sb.toString())
            return types
        }

        fun value(value: Any?): String {
            if (value === null) {
                return "null"
            }

            if (value is Uint) {
                return value.value.toString()
            }

            if (value is Int) {
                return value.value.toString()
            }

            if (value is ByteArray) {
                return bytesToString(value)
            }

            if (value is Bytes32) {
                return bytesToString(value.value)
            }

            if (value is DynamicBytes) {
                return bytesToString(value.value)
            }

            if (value is Collection<*>) {
                return "[" + value.joinToString(", ") { value(it) } + "]"
            }

            if (value is StaticStruct) {
                return "[" + value.value.joinToString(", ") { value(it) } + "]"
            }

            return value.toString()
        }

        fun type(type: Type<*>): String {
            if (type is Array<*>) {
                val value = type.value
                return if (value.isNullOrEmpty()) AbiTypes.getTypeAString(type.componentType) + "[]"
                else type.typeAsString
            }
            return type.typeAsString
        }

        private fun bytesToString(value: ByteArray) = "0x" + Hex.encodeHexString(value)
    }
}
