package io.klaytn.finder.service.accountkey

import com.klaytn.caver.account.AccountKeyRoleBased

enum class KlaytnAccountKeyRoleType(val roleGroupIndex: Int) {
    RoleTransaction(AccountKeyRoleBased.RoleGroup.TRANSACTION.index),
    RoleAccountUpdate(AccountKeyRoleBased.RoleGroup.ACCOUNT_UPDATE.index),
    RoleFeePayer(AccountKeyRoleBased.RoleGroup.FEE_PAYER.index);

    companion object {
        fun getOrderedValues() = values().sortedBy { it.roleGroupIndex }
    }
}
