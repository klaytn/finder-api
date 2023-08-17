package io.klaytn.finder.interfaces.rest.api.view.mapper

import com.klaytn.caver.abi.ABI
import io.klaytn.finder.infra.utils.SignatureDecodeUtils
import org.junit.jupiter.api.Test

class TransactionToInputDataViewMapperTest {
    @Test
    fun test() {
        val input =
            "0xa9059cbb000000000000000000000000534d89fc84c28ebcdb51c1c8510718cbbca0e0270000000000000000000000000000000000000000000000000de0b6b3a7640000"
        println(input.substring(0, 10))

        val encoded = input.substring(10)

        val signatures = listOf(
            "transfer(address,uint256)",
            "many_msg_babbage(bytes1)",
            "transfer(bytes4[9],bytes5[6],int48[11])",
            "func_2093253501(bytes)",
            "settle(address[],uint256[],(uint256,uint256,address,uint256,uint256,uint32,bytes32,uint256,uint256,uint256,bytes)[],(address,uint256,bytes)[][3])"
        )

        val signature = signatures.sortedByDescending { it.count { ch -> ch == ',' } }
            .firstNotNullOfOrNull { runCatching {
                ABI.decodeParameters(SignatureDecodeUtils.getTypeOfSignature(it), encoded)
            }.getOrNull() }

        println(signature)
    }

    /**
     * SMEMBERS finder/common/signature/function-signatures:0x095ea7b3
     *
     * - SMISMEMBER finder/common/signature/function-signatures:0x095ea7b3 "{\"id\":165138,\"hex_signature\":\"0x095ea7b3\",\"text_signature\":\"sign_szabo_bytecode(bytes16,uint128)\"}"
     * - SISMEMBER finder/common/signature/function-signatures:0x095ea7b3 "{\"id\":165138,\"hex_signature\":\"0x095ea7b3\",\"text_signature\":\"sign_szabo_bytecode(bytes16,uint128)\"}"
     */
    @Test
    fun test2() {
        val input =
            "0x095ea7b3000000000000000000000000b9cda58b025248082df801db735bdd1dbd5a0b0f00000000000000000000000000000000000000000000152d02c7e14af6800000"
        println(input.substring(0, 10))

        val encoded = input.substring(10)

        val signatures = listOf(
            "approve(address,uint256)",
            "sign_szabo_bytecode(bytes16,uint128)",
        )

        val signature = signatures.sortedByDescending { it.count { ch -> ch == ',' } }
            .firstNotNullOfOrNull { runCatching {
                ABI.decodeParameters(SignatureDecodeUtils.getTypeOfSignature(it), encoded)
            }.getOrNull() }

        println(signature)
    }
}
