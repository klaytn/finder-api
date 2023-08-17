package io.klaytn.finder.view.model.transaction

import com.klaytn.caver.transaction.type.TransactionType

enum class TransactionTypeView(
        val group: TransactionTypeGroup,
        val transactionType: TransactionType,
) {
    `Account Update`(TransactionTypeGroup.Account, TransactionType.TxTypeAccountUpdate),
    `Fee Delegated Account Update`(
            TransactionTypeGroup.Account,
            TransactionType.TxTypeFeeDelegatedAccountUpdate
    ),
    `Fee Delegated Account Update With Ratio`(
            TransactionTypeGroup.Account,
            TransactionType.TxTypeFeeDelegatedAccountUpdateWithRatio
    ),
    Cancel(TransactionTypeGroup.Cancel, TransactionType.TxTypeCancel),
    `Fee Delegated Cancel`(TransactionTypeGroup.Cancel, TransactionType.TxTypeFeeDelegatedCancel),
    `Fee Delegated Cancel With Ratio`(
            TransactionTypeGroup.Cancel,
            TransactionType.TxTypeFeeDelegatedCancelWithRatio
    ),
    `Smart Contract Deploy`(
            TransactionTypeGroup.`Contract Deploy`,
            TransactionType.TxTypeSmartContractDeploy
    ),
    `Fee Delegated Smart Contract Deploy`(
            TransactionTypeGroup.`Contract Deploy`,
            TransactionType.TxTypeFeeDelegatedSmartContractDeploy
    ),
    `Fee Delegated Smart Contract Deploy With Ratio`(
            TransactionTypeGroup.`Contract Deploy`,
            TransactionType.TxTypeFeeDelegatedSmartContractDeployWithRatio
    ),
    `Smart Contract Execution`(
            TransactionTypeGroup.`Contract Execution`,
            TransactionType.TxTypeSmartContractExecution
    ),
    `Fee Delegated Smart Contract Execution`(
            TransactionTypeGroup.`Contract Execution`,
            TransactionType.TxTypeFeeDelegatedSmartContractExecution
    ),
    `Fee Delegated Smart Contract Execution With Ratio`(
            TransactionTypeGroup.`Contract Execution`,
            TransactionType.TxTypeFeeDelegatedSmartContractExecutionWithRatio
    ),
    Legacy(TransactionTypeGroup.Legacy, TransactionType.TxTypeLegacyTransaction),
    `Chain Data Anchoring`(
            TransactionTypeGroup.`Service Chain`,
            TransactionType.TxTypeChainDataAnchoring
    ),
    `Fee Delegated Chain Data Anchoring`(
            TransactionTypeGroup.`Service Chain`,
            TransactionType.TxTypeFeeDelegatedChainDataAnchoring
    ),
    `Fee Delegated Chain Data Anchoring With Ratio`(
            TransactionTypeGroup.`Service Chain`,
            TransactionType.TxTypeFeeDelegatedChainDataAnchoringWithRatio
    ),
    `Value Transfer`(TransactionTypeGroup.`Value Transfer`, TransactionType.TxTypeValueTransfer),
    `Fee Delegated Value Transfer`(
            TransactionTypeGroup.`Value Transfer`,
            TransactionType.TxTypeFeeDelegatedValueTransfer
    ),
    `Fee Delegated Value Transfer With Ratio`(
            TransactionTypeGroup.`Value Transfer`,
            TransactionType.TxTypeFeeDelegatedValueTransferWithRatio
    ),
    `Value Transfer Memo`(
            TransactionTypeGroup.`Value Transfer`,
            TransactionType.TxTypeValueTransferMemo
    ),
    `Fee Delegated Value Transfer Memo`(
            TransactionTypeGroup.`Value Transfer`,
            TransactionType.TxTypeFeeDelegatedValueTransferMemo
    ),
    `Fee Delegated Value Transfer Memo With Ratio`(
            TransactionTypeGroup.`Value Transfer`,
            TransactionType.TxTypeFeeDelegatedValueTransferMemoWithRatio
    ),
    `Ethereum Access List`(TransactionTypeGroup.Ethereum, TransactionType.TxTypeEthereumAccessList),
    `Ethereum Dynamic Fee`(TransactionTypeGroup.Ethereum, TransactionType.TxTypeEthereumDynamicFee),
    ;

    companion object {
        fun getView(transactionType: TransactionType) =
                values().first { it.transactionType == transactionType }

        fun toMap() =
                values().groupBy { it.group }.entries.associate { entry ->
                    entry.key.name to entry.value.associate { it.name to it.transactionType }
                }
    }
}

enum class TransactionTypeGroup {
    Account,
    Cancel,
    `Contract Deploy`,
    `Contract Execution`,
    Legacy,
    `Service Chain`,
    `Value Transfer`,
    Ethereum
}
