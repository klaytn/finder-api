package io.klaytn.finder.service.verifier

import com.klaytn.caver.utils.Utils
import com.klaytn.caver.utils.wrapper.UtilsWrapper
import com.klaytn.caver.wallet.keyring.SignatureData
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.web3j.utils.Numeric

class KlipSignMessageVerifyTest {
    @Test
    fun verifySignedMessageWithSimpleMessage() {
        val message = "original message"
        val signedHash =
                "0x1dc98165c3fc523bcdbdf18eadba12b004cd30b232e5e65fdd6424412cbf0dab2d131dda838cd249a7d00414ae53abe5ba6fa7bf8446f28c328bc60443c1545d07f5"

        val recoverAccountAddress = getAccountAddress(message, signedHash)
        Assertions.assertEquals(
                "0x220AD25E31BBF7c19D95Be0e47d4cdc0Ad8f8FEa".lowercase(),
                recoverAccountAddress
        )
    }

    private fun getAccountAddress(message: String, signatureData: String): String {
        val utilsWrapper = UtilsWrapper()

        val noPrefixSigData = Utils.stripHexPrefix(signatureData)
        val r: String = noPrefixSigData.substring(0, 64)
        val s: String = noPrefixSigData.substring(64, 128)
        val v: String = noPrefixSigData.substring(128)

        val versionNumber = Numeric.toBigInt(v).toInt()
        val newVersion =
                if (versionNumber < 2) {
                    versionNumber
                } else {
                    1 - versionNumber % 2
                }

        val signature = SignatureData(Integer.toHexString(newVersion), r, s)
        val publicKey = utilsWrapper.recoverPublicKey(message, signature)
        return utilsWrapper.publicKeyToAddress(publicKey)
    }
}
