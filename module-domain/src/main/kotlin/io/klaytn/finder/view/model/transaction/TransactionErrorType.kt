package io.klaytn.finder.view.model.transaction

enum class TransactionErrorType(val value: Int, val desc: String) {
    _0x02(0x02, "VM error occurs while running smart contract"),
    _0x03(0x03, "Max call depth exceeded"),
    _0x04(0x04, "Contract address collision"),
    _0x05(0x05, "Contract creation code storage out of gas"),
    _0x06(0x06, "EVM: max code size exceeded"),
    _0x07(0x07, "Out of gas"),
    _0x08(0x08, "EVM: write protection"),
    _0x09(0x09, "EVM: execution reverted"),
    _0x0a(0x0a, "Reached the opcode computation cost limit (100000000) for TX"),
    _0x0b(0x0b, "Account already exists"),
    _0x0c(0x0c, "Not a program account (e.g., An account having code and storage)"),
    _0x0d(0x0d, "Human-readable address is not supported now"),
    _0x0e(0x0e, "Fee ratio is out of range [1, 99]"),
    _0x0f(0x0f, "AccountKeyFail is not updatable"),
    _0x10(0x10, "Different account key type"),
    _0x11(0x11, "AccountKeyNil cannot be initialized to an account"),
    _0x12(0x12, "Public key is not on curve"),
    _0x13(0x13, "Key weight is zero"),
    _0x14(0x14, "Key is not serializable"),
    _0x15(0x15, "Duplicated key"),
    _0x16(0x16, "Weighted sum overflow"),
    _0x17(0x17, "Unsatisfiable threshold. Weighted sum of keys is less than the threshold."),
    _0x18(0x18, "Length is zero"),
    _0x19(0x19, "Length too long"),
    _0x1a(0x1a, "Nested composite type"),
    _0x1b(0x1b, "A legacy transaction must be with a legacy account key"),
    _0x1c(0x1c, "Deprecated feature"),
    _0x1d(0x1d, "Not supported"),
    _0x1e(0x1e, "Smart contract code format is invalid"),
    unknown(0x00, ""),
    ;

    companion object {
        fun of(value: Int) =
                values().find { it.value == value }
                        ?: throw IllegalArgumentException("$value is invalid")
    }
}
