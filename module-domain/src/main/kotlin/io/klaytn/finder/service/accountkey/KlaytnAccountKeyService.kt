package io.klaytn.finder.service.accountkey

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.NullNode
import com.klaytn.caver.Caver
import com.klaytn.caver.account.AccountKeyFail
import com.klaytn.caver.account.AccountKeyLegacy
import com.klaytn.caver.account.AccountKeyPublic
import com.klaytn.caver.account.AccountKeyRoleBased
import com.klaytn.caver.account.AccountKeyWeightedMultiSig
import com.klaytn.caver.account.IAccountKey
import io.klaytn.commons.utils.logback.logger
import io.klaytn.finder.service.caver.CaverAccountService
import org.springframework.stereotype.Service

@Service
class KlaytnAccountKeyService(
    private val caverAccountService: CaverAccountService,
    private val caver: Caver,
    private val objectMapper: ObjectMapper,
) {
    private val logger = logger(this::class.java)

    fun getKlaytnAccountKeyByAccountAddress(accountAddress: String) =
        caverAccountService.getAccountKey(accountAddress)?.let { getKlaytnAccountKey(it) }

    fun getKlaytnAccountKeyWithJson(accountKey: String) =
        getKlaytnAccountKey(objectMapper.readTree(accountKey))

    fun getKlaytnAccountKey(accountKey: IAccountKey): KlaytnAccountKey? =
        when (accountKey) {
            is AccountKeyLegacy -> {
                KlaytnAccountKeyLegacy()
            }
            is AccountKeyPublic -> {
                KlaytnAccountKeyPublic(
                    publicKey = accountKey.publicKey,
                    compressedPublicKey = caver.utils.compressPublicKey(accountKey.publicKey),
                    address = caver.utils.publicKeyToAddress(accountKey.publicKey)
                )
            }
            is AccountKeyFail -> {
                KlaytnAccountKeyFail()
            }
            is AccountKeyWeightedMultiSig -> {
                val weightedPublicKeys = accountKey.weightedPublicKeys.map { weightedPublicKey ->
                    KlaytnAccountKeyWeightedMultiSig.KlaytnAccountKeyWeightedKey(
                        weight = weightedPublicKey.weight.toLong(),
                        publicKey = weightedPublicKey.publicKey,
                        compressedPublicKey = caver.utils.compressPublicKey(weightedPublicKey.publicKey),
                        address = caver.utils.publicKeyToAddress(weightedPublicKey.publicKey)
                    )
                }

                KlaytnAccountKeyWeightedMultiSig(
                    threshold = accountKey.threshold.toLong(),
                    weightedPublicKeys = weightedPublicKeys
                )
            }
            is AccountKeyRoleBased -> {
                val roleTransaction = getKlaytnAccountKey(accountKey.roleTransactionKey)
                KlaytnAccountKeyRoleBased(
                    roles = mapOf(
                        KlaytnAccountKeyRoleType.RoleTransaction to roleTransaction,
                        KlaytnAccountKeyRoleType.RoleAccountUpdate to
                                getAccountKeyRole(
                                    accountKey.accountKeys,
                                    KlaytnAccountKeyRoleType.RoleAccountUpdate,
                                    roleTransaction),
                        KlaytnAccountKeyRoleType.RoleFeePayer to
                                getAccountKeyRole(
                                    accountKey.accountKeys,
                                    KlaytnAccountKeyRoleType.RoleFeePayer,
                                    roleTransaction),
                    )
                )
            }
            else -> {
                logger.warn("unknown account-key: ${accountKey.javaClass.name}")
                null
            }
        }

    fun getKlaytnAccountKey(jsonNode: JsonNode): KlaytnAccountKey =
        when(KlaytnAccountKeyType.valueOf(jsonNode.get("type").asText())) {
            KlaytnAccountKeyType.AccountKeyLegacy -> {
                KlaytnAccountKeyLegacy()
            }
            KlaytnAccountKeyType.AccountKeyPublic -> {
                val publicKey = jsonNode.get("publicKey").asText()
                KlaytnAccountKeyPublic(
                    publicKey = publicKey,
                    compressedPublicKey = caver.utils.compressPublicKey(publicKey),
                    address = caver.utils.publicKeyToAddress(publicKey)
                )
            }
            KlaytnAccountKeyType.AccountKeyFail -> {
                KlaytnAccountKeyFail()
            }
            KlaytnAccountKeyType.AccountKeyWeightedMultiSig -> {
                val threshold = jsonNode.get("threshold").asLong()
                val weightedPublicKeys = mutableListOf<KlaytnAccountKeyWeightedMultiSig.KlaytnAccountKeyWeightedKey>()
                jsonNode.get("weightedPublicKeys").iterator().forEach {
                    val weight = it.get("weight").asLong()
                    val publicKey = it.get("publicKey").asText()
                    weightedPublicKeys.add(
                        KlaytnAccountKeyWeightedMultiSig.KlaytnAccountKeyWeightedKey(
                            weight = weight,
                            publicKey = publicKey,
                            compressedPublicKey = caver.utils.compressPublicKey(publicKey),
                            address = caver.utils.publicKeyToAddress(publicKey)
                        )
                    )
                }
                KlaytnAccountKeyWeightedMultiSig(
                    threshold = threshold,
                    weightedPublicKeys = weightedPublicKeys
                )
            }
            KlaytnAccountKeyType.AccountKeyRoleBased -> {
                val rolesNode = jsonNode.get("roles")
                val roleTransaction = getAccountKeyRole(
                    rolesNode, KlaytnAccountKeyRoleType.RoleTransaction, null)
                KlaytnAccountKeyRoleBased(
                    roles = mapOf(
                        KlaytnAccountKeyRoleType.RoleTransaction to roleTransaction,
                        KlaytnAccountKeyRoleType.RoleAccountUpdate to
                                getAccountKeyRole(
                                    rolesNode, KlaytnAccountKeyRoleType.RoleAccountUpdate, roleTransaction),
                        KlaytnAccountKeyRoleType.RoleFeePayer to
                                getAccountKeyRole(
                                    rolesNode, KlaytnAccountKeyRoleType.RoleFeePayer, roleTransaction)
                ))
            }
        }

    private fun getAccountKeyRole(
        accountKeys: List<IAccountKey>, roleType: KlaytnAccountKeyRoleType, default: KlaytnAccountKey?
    ): KlaytnAccountKey? {
        val accountKeySize = accountKeys.size
        return if(accountKeySize > roleType.roleGroupIndex) {
            getKlaytnAccountKey(accountKeys[roleType.roleGroupIndex])
        } else {
            default
        }
    }

    private fun getAccountKeyRole(
        rolesNode: JsonNode, roleType: KlaytnAccountKeyRoleType, default: KlaytnAccountKey?
    ): KlaytnAccountKey? {
        val roleNode = rolesNode.get(roleType.name)
        return if(roleNode != null && roleNode !is NullNode) {
            getKlaytnAccountKey(roleNode)
        } else {
            default
        }
    }
}
