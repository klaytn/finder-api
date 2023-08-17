package io.klaytn.finder.service.accountkey

enum class KlaytnAccountKeyType(val value: String) {
    AccountKeyLegacy("0x01"),
    AccountKeyPublic("0x02"),
    AccountKeyFail("0x03"),
    AccountKeyWeightedMultiSig("0x04"),
    AccountKeyRoleBased("0x05"),
    ;

    companion object {
        fun of(value: String) = values().first { it.value == value }
    }
}
