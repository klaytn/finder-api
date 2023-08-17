package io.klaytn.finder.service

import com.klaytn.caver.abi.ABI
import com.klaytn.caver.abi.datatypes.DynamicArray
import com.klaytn.caver.abi.datatypes.DynamicBytes
import com.klaytn.caver.abi.datatypes.StaticStruct
import com.klaytn.caver.abi.datatypes.Uint
import com.klaytn.caver.abi.datatypes.generated.Bytes32
import com.klaytn.caver.contract.ContractIOType
import org.apache.commons.codec.binary.Hex
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class EventLogDecodeTest {
    @Test
    fun test_decode_data() {
        val data =
                "0x00000000000000000000000000000000000000000000000000000000000000400000000000000000000000000000000000000000000000000000000000000080000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000004b000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000001"
        val decoded =
                ABI.decodeLog(
                        listOf(
                                ContractIOType("operator", "address", true),
                                ContractIOType("from", "address", true),
                                ContractIOType("to", "address", true),
                                ContractIOType("tokenIds", "uint256[]", false),
                                ContractIOType("values", "uint256[]", false),
                        ),
                        data,
                        listOf(
                                "0x4a39dc06d4c0dbc64b70af90fd698a233a518aa5d07e595d983b8c0526c8f7fb",
                                "0x000000000000000000000000d465983175673079e6d97ccc155b66b0948490d7",
                                "0x0000000000000000000000000000000000000000000000000000000000000000",
                                "0x000000000000000000000000090baba9098081c0cc70ff87e81897eb4985b843"
                        )
                )
        decoded.indexedValues.forEach {
            println(it.typeAsString)
            println("=>${it.value}")
        }
        decoded.nonIndexedValues.forEach {
            // list 인것과 아닌것 구분필요
            val value =
                    if (it is DynamicArray<*> && !it.value.isNullOrEmpty()) {
                        it.value.joinToString(",") { it -> it.value.toString() }
                    } else {
                        it.toString()
                    }
            println(it.typeAsString)
            println("=>${value}")
        }
    }

    @Test
    fun aaa() {
        val eventSignature =
                "ConfirmRequest(uint256,address,uint8,bytes32,bytes32,bytes32,address[])"
        val types = functionSignatureToTypes(eventSignature)

        val topics =
                listOf(
                        "0x658055f08cf63659025f616b2baf94baf8f4d2ee9c600c2ae722b4b7716cce46",
                        "0x000000000000000000000000000000000000000000000000000000000000001e",
                        "0x00000000000000000000000039393a83980b496dde431a4c38e97ac7a6e5a942"
                )
        val data =
                "0x00000000000000000000000000000000000000000000000000000000000000060000000000000000000000002828b981cfc96ab33505b32e9b4260b84344f3e100000000000000000000000000000000000000000000000017979cfe362a0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000a0000000000000000000000000000000000000000000000000000000000000000300000000000000000000000046ea1496e9c46776d1fde390fe5635e7ffcb72e1000000000000000000000000a713e0a784e685b06ba06d7723404a9e2a04066700000000000000000000000039393a83980b496dde431a4c38e97ac7a6e5a942"

        val contractIOTypes = mutableListOf<ContractIOType>()
        types.forEachIndexed { index, s ->
            val indexed = index <= topics.size - 2
            contractIOTypes.add(ContractIOType(index.toString(), s, indexed))
        }
        val decoded = ABI.decodeLog(contractIOTypes, data, topics)
        decoded.indexedValues.forEach {
            println(it.typeAsString)
            println("=>${it.value}")
        }
        decoded.nonIndexedValues.forEach {
            println(it.typeAsString)
            println("=>${value(it.value)}")
        }
    }

    /** ConfirmRequest(uint256,address,uint8,bytes32,bytes32,bytes32,address[]) */
    @Test
    fun test_decode_data_2() {
        val data =
                "0x00000000000000000000000000000000000000000000000000000000000000060000000000000000000000002828b981cfc96ab33505b32e9b4260b84344f3e100000000000000000000000000000000000000000000000017979cfe362a0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000a0000000000000000000000000000000000000000000000000000000000000000300000000000000000000000046ea1496e9c46776d1fde390fe5635e7ffcb72e1000000000000000000000000a713e0a784e685b06ba06d7723404a9e2a04066700000000000000000000000039393a83980b496dde431a4c38e97ac7a6e5a942"
        val decoded =
                ABI.decodeLog(
                        listOf(
                                ContractIOType("1", "uint256", true),
                                ContractIOType("2", "address", true),
                                ContractIOType("3", "uint8", false),
                                ContractIOType("4", "bytes32", false),
                                ContractIOType("5", "bytes32", false),
                                ContractIOType("6", "bytes32", false),
                                ContractIOType("7", "address[]", false),
                        ),
                        data,
                        listOf(
                                "0x658055f08cf63659025f616b2baf94baf8f4d2ee9c600c2ae722b4b7716cce46",
                                "0x000000000000000000000000000000000000000000000000000000000000001e",
                                "0x00000000000000000000000039393a83980b496dde431a4c38e97ac7a6e5a942"
                        )
                )
        decoded.indexedValues.forEach {
            println(it.typeAsString)
            println("=>${it.value}")
        }
        decoded.nonIndexedValues.forEach {
            println(it.typeAsString)
            println("=>${value(it.value)}")
        }
    }

    @Test
    fun getEventSignature() {
        Assertions.assertEquals(
                "0xdb66dfa9c6b8f5226fe9aac7e51897ae8ee94ac31dc70bb6c9900b2574b707e6",
                ABI.encodeEventSignature("MasterMinterChanged(address)")
        )

        Assertions.assertEquals(
                "0x6970e71b2bda096f4eb3290c554af7a888cca0ef8b95da09c55b23c6bb30e381",
                ABI.encodeEventSignature("MinterChanged(address,address,address)")
        )
    }

    private fun value(value: Any?): String {
        if (value === null) {
            return "null"
        }

        if (value is Uint) {
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

    private fun bytesToString(value: ByteArray) = "0x" + Hex.encodeHexString(value)

    fun functionSignatureToTypes(functionSignature: String): List<String> {
        val signature = functionSignature.substringAfter("(").substringBeforeLast(")")

        val sb = StringBuilder()
        val types = mutableListOf<String>()

        var tuple = false

        signature.forEach { ch ->
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
}
