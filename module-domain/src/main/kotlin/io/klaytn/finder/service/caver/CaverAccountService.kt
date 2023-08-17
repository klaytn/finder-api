package io.klaytn.finder.service.caver

import com.klaytn.caver.Caver
import com.klaytn.caver.account.IAccountKey
import com.klaytn.caver.methods.response.IAccountType
import com.klaytn.caver.utils.Utils
import com.klaytn.caver.wallet.keyring.SignatureData
import io.klaytn.commons.utils.logback.logger
import io.klaytn.finder.domain.common.AccountType
import io.klaytn.finder.domain.common.WalletType
import io.klaytn.finder.infra.exception.InvalidRequestException
import org.bouncycastle.jcajce.provider.digest.Keccak
import org.bouncycastle.util.encoders.Hex
import org.springframework.stereotype.Service
import org.web3j.crypto.Keys
import org.web3j.crypto.Sign
import org.web3j.utils.Numeric
import java.math.BigDecimal
import java.nio.charset.StandardCharsets

@Service
class CaverAccountService(
    private val caver: Caver,
) {
    private val logger = logger(this::class.java)

    fun isAccountAddress(accountAddress: String) =
        accountAddress.startsWith("0x") && accountAddress.length == (40 + 2)

    fun getAccountKey(accountAddress: String): IAccountKey? {
        return try {
            val accountKeyResponse = caver.rpc.klay.getAccountKey(accountAddress).send()
            if(!accountKeyResponse.hasError()) {
                return accountKeyResponse.result?.accountKey
            } else {
                null
            }
        } catch (exception: Exception) {
            logger.warn(exception.message, exception)
            null
        }
    }

    fun getAccountType(accountAddress: String): AccountType {
        if (!isAccountAddress(accountAddress)) {
            throw InvalidRequestException()
        }

        return try {
            val accountResponse = caver.rpc.klay.getAccount(accountAddress).send()
            if (!accountResponse.hasError()) {
                val accType = accountResponse.result?.let {
                    IAccountType.AccType.getType(accountResponse.result.accType)
                }
                if (accType == IAccountType.AccType.SCA) AccountType.SCA else AccountType.EOA
            } else {
                with(accountResponse.error) {
                    logger.warn("fail to get account type for address($accountAddress). (code:${this.code}, message:${this.message})")
                }
                AccountType.EOA
            }

        } catch (exception: Exception) {
            logger.warn(exception.message, exception)
            AccountType.EOA
        }
    }

    fun getAccountBalance(accountAddress: String): BigDecimal {
        return try {
            val balanceResponse = caver.rpc.klay.getBalance(accountAddress).send()
            if(!balanceResponse.hasError()) {
                balanceResponse.value.toBigDecimal()
            } else {
                BigDecimal.ZERO
            }
        } catch (exception: Exception) {
            logger.warn(exception.message, exception)
            BigDecimal.ZERO
        }
    }

    fun getTransactionCount(accountAddress: String): BigDecimal {
        return try {
            val transactionCountResponse = caver.rpc.klay.getTransactionCount(accountAddress).send()
            if(!transactionCountResponse.hasError()) {
                transactionCountResponse.value.toBigDecimal()
            } else {
                BigDecimal.ZERO
            }
        } catch (exception: Exception) {
            logger.warn(exception.message, exception)
            BigDecimal.ZERO
        }
    }

    fun getRecoveredAccountAddressForKaikas(message: String, signatureData: String): String =
        caver.utils.recover(message, caver.utils.decodeSignature(signatureData))

    fun getRecoveredAccountAddressForMetamask(message: String, signatureData: String): String {
        val messageHashBytes =
            if (Numeric.containsHexPrefix(message)) {
                Numeric.hexStringToByteArray(message)
            } else {
                message.toByteArray()
            }

        val signature = caver.utils.decodeSignature(signatureData)
        var v = Numeric.hexStringToByteArray(signature.v)[0]
        if (v < 27) {
            v = (v + 27).toByte()
        }

        val publicKey = Sign.signedPrefixedMessageToKey(
            messageHashBytes,
            Sign.SignatureData(
                v,
                Numeric.hexStringToByteArray(signature.r),
                Numeric.hexStringToByteArray(signature.s)
            )
        ).toString(16)
        return caver.utils.addHexPrefix(Keys.getAddress(publicKey))
    }

    fun getRecoveredAccountAddressForKlip(message: String, signatureData: String): String {
        val noPrefixSigData = Utils.stripHexPrefix(signatureData)
        val r: String = noPrefixSigData.substring(0, 64)
        val s: String = noPrefixSigData.substring(64, 128)
        val v: String = noPrefixSigData.substring(128)

        val versionNumber = Numeric.toBigInt(v).toInt()
        val newVersion = if(versionNumber < 2) {
            versionNumber
        } else {
            1 - versionNumber % 2
        }

        val signature = SignatureData(Integer.toHexString(newVersion), r, s)
        val publicKey = caver.utils.recoverPublicKey(message, signature)
        return caver.utils.publicKeyToAddress(publicKey)
    }

    fun getRecoveredAccountAddressForKas(message: String, signatureData: String): String {
        val hashedMessage =
            if(Utils.isHex(message)) {
                message
            } else {
                val hashedMessageBytes = Keccak.Digest256().digest(message.toByteArray(StandardCharsets.UTF_8))
                caver.utils.addHexPrefix(String(Hex.encode(hashedMessageBytes)))
            }

        val signature = caver.utils.decodeSignature(signatureData)
        val publicKey = caver.utils.recoverPublicKey(hashedMessage, signature, true)
        return caver.utils.publicKeyToAddress(publicKey)
    }

    fun getRecoveredAccountAddress(walletType: WalletType, message: String, signatureData: String) =
        when (walletType) {
            WalletType.KAIKAS -> {
                getRecoveredAccountAddressForKaikas(message, signatureData)
            }
            WalletType.METAMASK -> {
                getRecoveredAccountAddressForMetamask(message, signatureData)
            }
            WalletType.KLIP -> {
                getRecoveredAccountAddressForKlip(message, signatureData)
            }
            WalletType.KAS -> {
                getRecoveredAccountAddressForKas(message, signatureData)
            }
        }

    fun verifySignature(walletType: WalletType, address: String, message: String, signatureData: String): Boolean {
        val recoveredAccountAddressFor = getRecoveredAccountAddress(walletType, message, signatureData)
        return recoveredAccountAddressFor.equals(address, true)
    }
}
