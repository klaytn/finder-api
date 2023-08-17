package io.klaytn.finder.view.model.account

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import io.klaytn.finder.service.accountkey.KlaytnAccountKeyRoleType
import io.klaytn.finder.service.accountkey.KlaytnAccountKeyType

open class KlaytnAccountKeyView(
    val type: KlaytnAccountKeyType,
)

class KlaytnAccountKeyLegacyView: KlaytnAccountKeyView(KlaytnAccountKeyType.AccountKeyLegacy)

@JsonPropertyOrder(value = ["type", "key"])
class KlaytnAccountKeyPublicView(
    val key: KlaytnPublicKey,
): KlaytnAccountKeyView(KlaytnAccountKeyType.AccountKeyPublic)

class KlaytnAccountKeyFailView: KlaytnAccountKeyView(KlaytnAccountKeyType.AccountKeyFail)

@JsonPropertyOrder(value = ["type", "threshold", "weightedPublicKeys"])
class KlaytnAccountKeyWeightedMultiSigView(
    val threshold: Long,
    val weightedPublicKeys: List<KlaytnAccountKeyWeightedKeyView>
): KlaytnAccountKeyView(KlaytnAccountKeyType.AccountKeyWeightedMultiSig) {
    data class KlaytnAccountKeyWeightedKeyView(
        val weight: Long,
        val key: KlaytnPublicKey,
    )
}

@JsonPropertyOrder(value = ["type", "roles"])
class KlaytnAccountKeyRoleBasedView(
    val roles: Map<KlaytnAccountKeyRoleType, KlaytnAccountKeyView?>
): KlaytnAccountKeyView(KlaytnAccountKeyType.AccountKeyRoleBased)

class KlaytnPublicKey(
    val publicKey: String,
    val compressedPublicKey: String,
    val address: String
)