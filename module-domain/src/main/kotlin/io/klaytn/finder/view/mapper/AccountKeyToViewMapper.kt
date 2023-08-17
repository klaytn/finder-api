package io.klaytn.finder.view.mapper

import io.klaytn.commons.model.mapper.Mapper
import io.klaytn.finder.service.accountkey.*
import io.klaytn.finder.view.model.account.*
import org.springframework.stereotype.Component
import org.web3j.crypto.Keys

@Component
class AccountKeyToViewMapper(
): Mapper<KlaytnAccountKey, KlaytnAccountKeyView?> {
    override fun transform(source: KlaytnAccountKey) = getKlaytnAccountKeyView(source)

    private fun getKlaytnAccountKeyView(klaytnAccountKey: KlaytnAccountKey): KlaytnAccountKeyView =
        when(klaytnAccountKey.type) {
            KlaytnAccountKeyType.AccountKeyLegacy -> {
                KlaytnAccountKeyLegacyView()
            }
            KlaytnAccountKeyType.AccountKeyPublic -> {
                val accountKey = klaytnAccountKey as KlaytnAccountKeyPublic
                KlaytnAccountKeyPublicView(
                    key = KlaytnPublicKey(
                        publicKey = accountKey.publicKey,
                        compressedPublicKey = accountKey.compressedPublicKey,
                        address = Keys.toChecksumAddress(accountKey.address)
                    )
                )
            }
            KlaytnAccountKeyType.AccountKeyFail -> {
                KlaytnAccountKeyFailView()
            }
            KlaytnAccountKeyType.AccountKeyWeightedMultiSig -> {
                val accountKey = klaytnAccountKey as KlaytnAccountKeyWeightedMultiSig
                val weightedPublicKeyViews =
                    accountKey.weightedPublicKeys.map {
                        KlaytnAccountKeyWeightedMultiSigView.KlaytnAccountKeyWeightedKeyView(
                            weight = it.weight,
                            key = KlaytnPublicKey(
                                publicKey = it.publicKey,
                                compressedPublicKey = it.compressedPublicKey,
                                address = Keys.toChecksumAddress(it.address)
                            )
                        )
                    }
                KlaytnAccountKeyWeightedMultiSigView(klaytnAccountKey.threshold, weightedPublicKeyViews)
            }
            KlaytnAccountKeyType.AccountKeyRoleBased -> {
                val accountKey = klaytnAccountKey as KlaytnAccountKeyRoleBased
                val roleViews = KlaytnAccountKeyRoleType.values()
                    .associateWith { getKlaytnAccountKeyView(accountKey.roles[it]!!) }
                KlaytnAccountKeyRoleBasedView(roles = roleViews)
            }
        }
}