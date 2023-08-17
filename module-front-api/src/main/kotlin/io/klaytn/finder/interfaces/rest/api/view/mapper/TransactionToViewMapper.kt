package io.klaytn.finder.interfaces.rest.api.view.mapper

import com.klaytn.caver.abi.ABI
import com.klaytn.caver.abi.datatypes.*
import io.klaytn.commons.model.mapper.ListMapper
import io.klaytn.commons.model.mapper.Mapper
import io.klaytn.commons.utils.logback.logger
import io.klaytn.finder.domain.common.AccountAddress
import io.klaytn.finder.domain.mysql.set1.Transaction
import io.klaytn.finder.infra.utils.DateUtils
import io.klaytn.finder.infra.utils.SignatureDecodeUtils
import io.klaytn.finder.interfaces.rest.api.view.model.transaction.*
import io.klaytn.finder.interfaces.rest.api.view.model.transaction.input.DecodedParam
import io.klaytn.finder.interfaces.rest.api.view.model.transaction.input.DecodedValue
import io.klaytn.finder.interfaces.rest.api.view.model.transaction.input.InputDataView
import io.klaytn.finder.service.*
import io.klaytn.finder.service.accountkey.KlaytnAccountKeyService
import io.klaytn.finder.view.mapper.AccountAddressToViewMapper
import io.klaytn.finder.view.mapper.AccountKeyToViewMapper
import io.klaytn.finder.view.model.transaction.TransactionErrorType
import io.klaytn.finder.view.model.transaction.TransactionStatus
import io.klaytn.finder.view.model.transaction.TransactionTypeView
import org.apache.commons.codec.binary.Hex
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class TransactionToListViewMapper(
    val transactionService: TransactionService,
    val accountAddressToViewMapper: AccountAddressToViewMapper,
    private val transactionToInputDataViewMapper: TransactionToInputDataViewMapper,
) : ListMapper<Transaction, TransactionListView> {
    override fun transform(source: List<Transaction>): List<TransactionListView> {
        return source.map { transaction ->
            val transactionFee = transactionService.getTransactionFees(transaction)
            val burntFees = transactionService.getTransactionBurntFees(transaction)

            val inputDataView = transaction.input?.let { transactionToInputDataViewMapper.transform(it) }
            val signature = inputDataView?.decodedValue?.signature
            val methodId = transaction.getMethodId()

            TransactionListView(
                transactionHash = transaction.transactionHash,
                blockId = transaction.blockNumber,
                datetime = DateUtils.from(transaction.timestamp),
                from = accountAddressToViewMapper.transform(transaction.from)!!,
                to = accountAddressToViewMapper.transform(transaction.to),
                transactionType = TransactionTypeView.getView(transaction.type),
                amount = transaction.value,
                transactionFee = transactionFee,
                success = transaction.status == 1,
                failMessage = transaction.txError?.let {
                    val error = TransactionErrorType.of(it)
                    error.desc + " - uint($it)"
                },
                methodId = methodId,
                signature = signature,
                effectiveGasPrice = transaction.effectiveGasPrice ?: BigDecimal.ZERO,
                burntFees = burntFees
            )
        }
    }
}

@Component
class TransactionToItemViewMapper(
    val transactionService: TransactionService,
    val accountAddressService: AccountAddressService,
    val klaytnAccountKeyService: KlaytnAccountKeyService,
    val accountAddressToViewMapper: AccountAddressToViewMapper,
    val accountKeyToViewMapper: AccountKeyToViewMapper,
    private val transactionToInputDataViewMapper: TransactionToInputDataViewMapper,
) : Mapper<Transaction, TransactionItemView> {
    override fun transform(source: Transaction): TransactionItemView {
        val transactionFee = transactionService.getTransactionFees(source)
        val burntFees = transactionService.getTransactionBurntFees(source)
        val accountKey =
            transactionService.getAccountKey(source)?.let {
                klaytnAccountKeyService.getKlaytnAccountKeyWithJson(it.accountKey).run {
                    accountKeyToViewMapper.transform(this)
                }
            }

        val accountAddresses = mutableSetOf<AccountAddress>()
        source.feePayer?.let { accountAddresses.add(AccountAddress(it)) }
        source.contractAddress?.let { accountAddresses.add(AccountAddress(it)) }
        accountAddressService.fillAccountAddress(accountAddresses.toList())
        val accountAddressViewMap = accountAddresses.associate { it.address to accountAddressToViewMapper.transform(it) }

        val createdContractAccount = source.contractAddress?.let { accountAddressViewMap[it] }
        val feePayerAccount = source.feePayer?.let { accountAddressViewMap[it] }
        val feeRation = if(feePayerAccount != null) {
            if(source.feeRatio.isNullOrBlank()) {
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

        return TransactionItemView(
            transactionHash = source.transactionHash,
            transactionType = TransactionTypeView.getView(source.type),
            blockId = source.blockNumber,
            from = accountAddressToViewMapper.transform(source.from)!!,
            to = accountAddressToViewMapper.transform(source.to),
            tokenTransfer = source.tokenTransferCount,
            nftTransfer = source.nftTransferCount,
            feePayer = source.feePayer,
            feePayerAccount = feePayerAccount,
            feeRation = feeRation,
            datetime = DateUtils.from(source.timestamp),
            nonce = source.nonce,
            amount = source.value,
            gasPrice = source.gasPrice,
            gasUsed = source.gasUsed.toBigDecimal(),
            gasLimit = source.gas.toBigDecimal(),
            transactionFee = transactionFee,
            status = if (source.status == 1) TransactionStatus.Success else TransactionStatus.Fail,
            failMessage = source.txError?.let {
                val error = TransactionErrorType.of(it)
                error.desc + " - uint($it)"
            },
            createdContractAccount = createdContractAccount,
            methodId = methodId,
            signature = signature,
            effectiveGasPrice = source.effectiveGasPrice ?: BigDecimal.ZERO,
            burntFees = burntFees,
            key = source.key,
            accountKey = accountKey
        )
    }
}

@Component
class TransactionToInputDataViewMapper(
    private val signatureService: SignatureService,
) : Mapper<Transaction, InputDataView> {

    private val logger = logger(this::class.java)

    override fun transform(source: Transaction) = transform(source.input!!)

    fun transform(input: String): InputDataView {
        if (input.length < 10) {
            return InputDataView(input, null, "")
        }

        val bytes = input.substring(0, 10)
        val encoded = input.substring(10)
        var functionSignatures = signatureService.getFunctionSignatures(bytes)

        // If the parameter is empty, extract only the text signature with no arguments.
        if(encoded.isEmpty()) {
            functionSignatures = functionSignatures.filter { it.textSignature.endsWith("()") }
        }

        val decoded = functionSignatures.firstNotNullOfOrNull {
            runCatching {
                val functionSignature = SignatureDecodeUtils.getTypeOfSignature(it.textSignature)

                if (encoded.isEmpty() && functionSignature.size == 1 && functionSignature[0].isEmpty()) Pair(
                    it.textSignature,
                    emptyList<Type<Any>>()
                ) else Pair(it.textSignature, ABI.decodeParameters(functionSignature, encoded))
            }.getOrNull()
        }

        val utf8Value = runCatching {
            Hex.decodeHex(input.substring(2)).decodeToString()
        }.getOrElse { "" }

        val decodedValue = if(decoded != null) {
            DecodedValue(
                methodId = bytes,
                signature = decoded.first,
                parameters = decoded.second.map { type ->
                    DecodedParam(
                        type = SignatureDecodeUtils.type(type),
                        name = null,
                        value = SignatureDecodeUtils.value(type.value)
                    )
                }
            )
        } else {
            DecodedValue(bytes, "", emptyList())
        }

        return InputDataView(
            originalValue = input,
            decodedValue = decodedValue,
            utf8Value = utf8Value
        )
    }
}