package io.klaytn.finder.service.accountkey

data class KlaytnTransactionAccountKey(
        val blockNumber: Long,
        val transactionHash: String,
        val accountAddress: String,
        val accountKey: KlaytnAccountKey?
)

open class KlaytnAccountKey(
        val type: KlaytnAccountKeyType,
)

class KlaytnAccountKeyLegacy : KlaytnAccountKey(KlaytnAccountKeyType.AccountKeyLegacy)

data class KlaytnAccountKeyPublic(
        val publicKey: String,
        val compressedPublicKey: String,
        val address: String,
) : KlaytnAccountKey(KlaytnAccountKeyType.AccountKeyPublic)

class KlaytnAccountKeyFail : KlaytnAccountKey(KlaytnAccountKeyType.AccountKeyFail)

data class KlaytnAccountKeyWeightedMultiSig(
        val threshold: Long,
        val weightedPublicKeys: List<KlaytnAccountKeyWeightedKey>
) : KlaytnAccountKey(KlaytnAccountKeyType.AccountKeyWeightedMultiSig) {
    data class KlaytnAccountKeyWeightedKey(
            val weight: Long,
            val publicKey: String,
            val compressedPublicKey: String,
            val address: String,
    )
}

data class KlaytnAccountKeyRoleBased(val roles: Map<KlaytnAccountKeyRoleType, KlaytnAccountKey?>) :
        KlaytnAccountKey(KlaytnAccountKeyType.AccountKeyRoleBased)
