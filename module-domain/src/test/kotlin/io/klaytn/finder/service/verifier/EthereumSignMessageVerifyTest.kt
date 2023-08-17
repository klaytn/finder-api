package io.klaytn.finder.service.verifier

import com.klaytn.caver.utils.wrapper.UtilsWrapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.web3j.crypto.Keys
import org.web3j.crypto.Sign
import org.web3j.utils.Numeric

class EthereumSignMessageVerifyTest {
    @Test
    fun verifySignedMessageWithHashedMessage_1() {
        val messageHash = "0xa0c68c638235ee32657e8f720a23cec1bfc77c77"
        val signedHash =
                "0x4d6728835090f9dbc4cd010eb91f8be1d3d54ad45bd1a898d8c66776fc0922255086152aeaf51221a8653aec57697679244cb34aacc7a4a3df381acac018a4441c"

        val recoverAccountAddress = getAccountAddressForEthereum(messageHash, signedHash)
        Assertions.assertEquals("0xadcb876f925fcb5342639f97c12034d75deb42be", recoverAccountAddress)
    }

    /** If the 'v' value in the signature is less than 27 and equal to 1. */
    @Test
    fun verifySignedMessageWithHashedMessage_2() {
        val messageHash = "0xc568c106e1840ed412456c27a360241752ab91f66f7376e13e995bdcae99b147"
        val signedHash =
                "0x0467a5d00fe4d5b67c0279f9c884c2e9b3b90b20eafb88aaaf1ed8f14e68ee7351bfd058d580cc229b74996c9af5b8338f0244a8ab43878378ea52dbaf94092701"

        val recoverAccountAddress = getAccountAddressForEthereum(messageHash, signedHash)
        Assertions.assertEquals("0xe7e7fc27ae4a9cd1ede050978189623d730f23d7", recoverAccountAddress)
    }

    @Test
    fun verifySignedMessageWithSimpleMessage() {
        val message = "Red Kite User Signature"
        val signedHash =
                "0x954de82943d5cbe396cc7b18e37adf426c5179490c528b4cec467b71b90c7e724e9c8cc9867ec2184696f33a9ad3c04d71cb922a5181dcad69a4a3ea887421dc1c"

        val recoverAccountAddress = getAccountAddressForEthereum(message, signedHash)
        Assertions.assertEquals("0xc4614e353ded598694c8731b9ddcaa272eb9c0aa", recoverAccountAddress)
    }

    private fun getAccountAddressForEthereum(message: String, signatureData: String): String {
        val utilsWrapper = UtilsWrapper()

        val messageHashBytes =
                if (Numeric.containsHexPrefix(message)) {
                    Numeric.hexStringToByteArray(message)
                } else {
                    message.toByteArray()
                }

        val signature = utilsWrapper.decodeSignature(signatureData)
        var v = Numeric.hexStringToByteArray(signature.v)[0]
        if (v < 27) {
            v = (v + 27).toByte()
        }

        val publicKey =
                Sign.signedPrefixedMessageToKey(
                                messageHashBytes,
                                Sign.SignatureData(
                                        v,
                                        Numeric.hexStringToByteArray(signature.r),
                                        Numeric.hexStringToByteArray(signature.s)
                                )
                        )
                        .toString(16)
        return utilsWrapper.addHexPrefix(Keys.getAddress(publicKey))
    }
}
