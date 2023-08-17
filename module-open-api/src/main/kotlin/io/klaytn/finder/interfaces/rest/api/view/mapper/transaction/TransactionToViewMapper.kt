package io.klaytn.finder.interfaces.rest.api.view.mapper.transaction

import io.klaytn.commons.model.mapper.Mapper
import io.klaytn.finder.domain.mysql.set1.Transaction
import io.klaytn.finder.infra.utils.DateUtils
import io.klaytn.finder.interfaces.rest.api.view.mapper.transaction.input.TransactionToInputDataViewMapper
import io.klaytn.finder.interfaces.rest.api.view.model.transaction.TransactionView
import io.klaytn.finder.service.TransactionService
import io.klaytn.finder.service.accountkey.KlaytnAccountKeyService
import io.klaytn.finder.view.mapper.AccountKeyToViewMapper
import io.klaytn.finder.view.mapper.transaction.status.TransactionToStatusViewMapper
import io.klaytn.finder.view.model.transaction.TransactionTypeView
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class TransactionToViewMapper(
    val transactionService: TransactionService,
    val klaytnAccountKeyService: KlaytnAccountKeyService,
    private val transactionToInputDataViewMapper: TransactionToInputDataViewMapper,
    private val transactionToStatusViewMapper: TransactionToStatusViewMapper,
    private val accountKeyToViewMapper: AccountKeyToViewMapper
) : Mapper<Transaction, TransactionView> {
    override fun transform(source: Transaction): TransactionView {
        val transactionFee = transactionService.getTransactionFees(source)
        val burntFees = transactionService.getTransactionBurntFees(source)
        val accountKey =
            transactionService.getAccountKey(source)?.let {
                klaytnAccountKeyService.getKlaytnAccountKeyWithJson(it.accountKey).run {
                    accountKeyToViewMapper.transform(this)
                }
            }

        val feeRation = if (source.feePayer != null) {
            if (source.feeRatio.isNullOrBlank()) {
                100
            } else {
                Integer.parseInt(source.feeRatio!!.substringAfter("0x"), 16)
            }
        } else {
            null
        }

        val inputDataView = source.input?.let { transactionToInputDataViewMapper.transform(it) }
        val signature = inputDataView?.decodedValue?.signature
        val methodId = source.getMethodId()

        return TransactionView(
            transactionHash = source.transactionHash,
            transactionType = TransactionTypeView.getView(source.type),
            blockId = source.blockNumber,
            from = source.from.address,
            to = source.to?.address,
            tokenTransfer = source.tokenTransferCount,
            nftTransfer = source.nftTransferCount,
            feePayer = source.feePayer,
            feeRation = feeRation,
            datetime = DateUtils.from(source.timestamp),
            nonce = source.nonce,
            amount = source.value,
            gasPrice = source.gasPrice,
            gasUsed = source.gasUsed.toBigDecimal(),
            gasLimit = source.gas.toBigDecimal(),
            transactionFee = transactionFee,
            status = transactionToStatusViewMapper.transform(source),
            createdContract = source.contractAddress,
            methodId = methodId,
            signature = signature,
            effectiveGasPrice = source.effectiveGasPrice ?: BigDecimal.ZERO,
            burntFees = burntFees,
            key = source.key,
            accountKey = accountKey
        )
    }
}