package io.klaytn.finder.service.verifier

import com.klaytn.caver.utils.Utils
import com.klaytn.caver.utils.wrapper.UtilsWrapper
import java.nio.charset.StandardCharsets
import org.bouncycastle.jcajce.provider.digest.Keccak
import org.bouncycastle.util.encoders.Hex
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class KasSignMessageVerifyTest {
    private val utils = UtilsWrapper()

    @Test
    fun messageToHashedMessage() {
        val signMessage = "202208230xa3232aa0aad528cc73e25764c73c061078fad3ec"

        val hashedBytes = Keccak.Digest256().digest(signMessage.toByteArray(StandardCharsets.UTF_8))
        val hashedSignedMessage = "0x" + String(Hex.encode(hashedBytes))
        Assertions.assertEquals(
                "0x624a47bee27f59b9afe24f7e5c3e5fca1c41787b390e4906f42e6dd65fe29588",
                hashedSignedMessage
        )
    }

    /**
     * address : 0xa8fb6936bb2555910245a0de4790ceccffc2e74e message :
     * 202208230xa3232aa0aad528cc73e25764c73c061078fad3ec
     *
     * curl --location --request GET 'https://kip7-api.klaytnapi.com/v1/deployer/default' \ --header
     * 'x-chain-id: 1001' \ -u KASK5NOKJCWUSKSIL3AKFQO3:b4Vc8QLj-O0e6L8yIpidqkT6F-dHlu5PgCUZA7Cl
     *
     * curl --location --request POST 'https://kip7-api.klaytnapi.com/v1/deployer/sign' \ --header
     * 'x-chain-id: 1001' \ -u KASK5NOKJCWUSKSIL3AKFQO3:b4Vc8QLj-O0e6L8yIpidqkT6F-dHlu5PgCUZA7Cl \
     * --header 'Content-Type: application/json' \ --data-raw '{ "data":
     * "0x624a47bee27f59b9afe24f7e5c3e5fca1c41787b390e4906f42e6dd65fe29588" }'
     */
    @Test
    fun get_creator_address_from_signature_using_kas_1() {
        val ownerAddress = "0xa8fb6936bb2555910245a0de4790ceccffc2e74e"
        val signMessage = "202208230xa3232aa0aad528cc73e25764c73c061078fad3ec"
        val signatureHash =
                "0xe1450b0e980905aa0e9d207aef6e5826c78501d1f681f4b3be8b579f4850cc1511d1a8e0ddf4747d084e2e6ca18b6a855a8119533a6300d2d61eed4c15afb0d400"

        val publicKey = getRecoveredAddressForKas(signMessage, signatureHash)
        Assertions.assertEquals(ownerAddress, publicKey)
    }

    @Test
    fun get_creator_address_from_signature_using_kas_2() {
        val ownerAddress = "0xa8fb6936bb2555910245a0de4790ceccffc2e74e"
        val hashedSignedMessage =
                "0x624a47bee27f59b9afe24f7e5c3e5fca1c41787b390e4906f42e6dd65fe29588"
        val signatureHash =
                "0xe1450b0e980905aa0e9d207aef6e5826c78501d1f681f4b3be8b579f4850cc1511d1a8e0ddf4747d084e2e6ca18b6a855a8119533a6300d2d61eed4c15afb0d400"

        val publicKey = getRecoveredAddressForKas(hashedSignedMessage, signatureHash)
        Assertions.assertEquals(ownerAddress, publicKey)
    }

    private fun getRecoveredAddressForKas(message: String, signatureData: String): String {
        val hashedMessage =
                if (Utils.isHex(message)) {
                    message
                } else {
                    val hashedMessageBytes =
                            Keccak.Digest256().digest(message.toByteArray(StandardCharsets.UTF_8))
                    utils.addHexPrefix(String(Hex.encode(hashedMessageBytes)))
                }

        val signature = utils.decodeSignature(signatureData)
        val publicKey = utils.recoverPublicKey(hashedMessage, signature, true)
        return utils.publicKeyToAddress(publicKey)
    }
}
