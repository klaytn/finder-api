package io.klaytn.finder.service.papi

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import io.klaytn.finder.infra.exception.NotFoundContractException
import io.klaytn.finder.infra.utils.DateUtils
import io.klaytn.finder.infra.utils.applyDecimal
import io.klaytn.finder.infra.web.model.SimplePageRequest
import io.klaytn.finder.service.*
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class KrosslabAccountBookService(
    private val contractService: ContractService,
    private val tokenService: TokenService,
    private val transactionService: TransactionService,
    private val internalTransactionService: InternalTransactionService,
    private val blockService: BlockService
) {
    private val sizePerPage = 1000

    fun getTokenTransfers(accountAddress : String, tokenAddress: String, date: String): List<FantoTokenTransfer> {
        val contract = contractService.getContract(tokenAddress) ?: throw NotFoundContractException()

        val timeRange = getTimeRange(date)
        val blockNumberRange = getBlockNumberRange(timeRange)

        val resultTransfers = mutableListOf<FantoTokenTransfer>()
        var simplePageRequest = SimplePageRequest(1, sizePerPage)
        while(true) {
            val transfers = tokenService.getTokenTransfersByAccountAddressWithoutCounting(
                accountAddress, tokenAddress, blockNumberRange, simplePageRequest, false)
            val filteredTransfers = transfers.filter { timeRange.contains(it.timestamp.toLong()) }.map { tokenTransfer ->
                val sign = if(tokenTransfer.from.address.equals(accountAddress, ignoreCase = true)) "-" else ""
                val amountString = sign + tokenTransfer.amount.toBigDecimal().applyDecimal(contract.decimal)

                FantoTokenTransfer(
                    time = DateUtils.from(tokenTransfer.timestamp),
                    from = tokenTransfer.from.address,
                    to = tokenTransfer.to!!.address,
                    amount = amountString,
                    link = getTransactionLink(tokenTransfer.transactionHash)
                )
            }

            resultTransfers.addAll(filteredTransfers)
            if(transfers.size < sizePerPage || (resultTransfers.isNotEmpty() && filteredTransfers.isEmpty())) {
                break
            }
            simplePageRequest = SimplePageRequest(simplePageRequest.page + 1, simplePageRequest.size)
        }
        return resultTransfers
    }

    fun getTransactions(accountAddress: String, date: String): List<FantoKlayTransaction> {
        val timeRange = getTimeRange(date)
        val blockNumberRange = null
        val resultTransactions = mutableListOf<FantoKlayTransaction>()

        var simplePageRequest = SimplePageRequest(1, sizePerPage)
        while(true) {
            val transactions = transactionService.getTransactionsByAccountAddressWithoutCounting(
                accountAddress, blockNumberRange, null, simplePageRequest, false)

            val filteredTransactions = transactions.filter { timeRange.contains(it.timestamp.toLong()) } .map { transaction ->
                val send = transaction.from.address.equals(accountAddress, ignoreCase = true)
                val sign = if(send) "-" else ""
                val klay = transaction.value
                val klayAmount = if(klay == BigDecimal.ZERO) klay.toString() else sign + klay.toString()
                val fee = if(send) transactionService.getTransactionFees(transaction) else 0

                FantoKlayTransaction(
                    time = DateUtils.from(transaction.timestamp),
                    from = transaction.from.address,
                    to = transaction.to?.address ?: "",
                    klay = klayAmount,
                    fee = fee.toString(),
                    link = getTransactionLink(transaction.transactionHash)
                )
            }

            resultTransactions.addAll(filteredTransactions)
            if(transactions.size < sizePerPage || (resultTransactions.isNotEmpty() && filteredTransactions.isEmpty())) {
                break
            }
            simplePageRequest = SimplePageRequest(simplePageRequest.page + 1, simplePageRequest.size)
        }
        return resultTransactions
    }

    fun getInternalTransactions(accountAddress: String, date: String): List<FantoKlayTransaction> {
        val timeRange = getTimeRange(date)
        val blockNumberRange = null
        val resultTransactions = mutableListOf<FantoKlayTransaction>()

        var simplePageRequest = SimplePageRequest(1, sizePerPage)
        while(true) {
            val internalTransactions = internalTransactionService.getInternalTransactionsByAccountAddress(
                accountAddress, blockNumberRange, simplePageRequest)
            val blockNumberAndTransactionIndices = internalTransactions
                .map { Pair(it.blockNumber, it.transactionIndex) }
                .groupBy { it.first }.entries.associate {
                    it.key to it.value.map { it.second }
                }

            val transactions = blockNumberAndTransactionIndices
                .map { transactionService.getTransactionByBlockNumberAndTransactionIndices(it.key, it.value) }
                .flatten()
                .groupBy { it.blockNumber }.entries.associate { entry ->
                    entry.key to entry.value.associateBy { it.transactionIndex }
                }

            val filteredTransactions = internalTransactions.content.mapNotNull { internalTx ->
                val transaction = transactions[internalTx.blockNumber]?.get(internalTx.transactionIndex)
                if(transaction == null || !timeRange.contains(transaction.timestamp.toLong())) {
                    null
                } else {
                    val send = internalTx.from.address.equals(accountAddress, ignoreCase = true)
                    val sign = if(send) "-" else ""
                    val klay = internalTx.value
                    val klayAmount = if(klay == BigDecimal.ZERO) klay.toString() else sign + klay.toString()
                    val fee = if(send) transactionService.getTransactionFees(transaction) else 0

                    FantoKlayTransaction(
                        time = DateUtils.from(transaction.timestamp),
                        from = internalTx.from.address,
                        to = internalTx.to?.address ?: "",
                        klay = klayAmount,
                        fee = fee.toString(),
                        link = getTransactionLink(transaction.transactionHash)
                    )
                }
            }

            resultTransactions.addAll(filteredTransactions)
            if(transactions.size < sizePerPage || (resultTransactions.isNotEmpty() && filteredTransactions.isEmpty())) {
                break
            }
            simplePageRequest = SimplePageRequest(simplePageRequest.page + 1, simplePageRequest.size)
        }
        return resultTransactions
    }

    private fun getTimeRange(date: String): LongRange {
        val localDate = LocalDate.parse("${date}01", DateTimeFormatter.ofPattern("yyyyMMdd"))

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        sdf.timeZone = TimeZone.getTimeZone("Pacific/Majuro")

        return LongRange(
            sdf.parse("${localDate.withDayOfMonth(1)} 00:00:00").time / 1000,
            sdf.parse("${localDate.withDayOfMonth(localDate.lengthOfMonth())} 23:59:59").time / 1000 + 1
        )
    }

    private fun getBlockNumberRange(timeRange: LongRange) =
        LongRange(
            blockService.getNumberByTimestamp(timestamp = timeRange.first.toInt())!!,
            blockService.getNumberByTimestamp(timestamp = timeRange.last.toInt())!!)

    private fun getTransactionLink(transactionHash: String) =
        """=HYPERLINK("https://www.klaytnfinder.io/tx/$transactionHash";"$transactionHash")"""
}

@JsonPropertyOrder("time", "from", "to", "amount", "link")
data class FantoTokenTransfer(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Pacific/Majuro")
    val time: Date,
    val from: String,
    val to: String,
    val amount: String,
    val link: String
)


@JsonPropertyOrder("time", "from", "to", "klay", "fee")
data class FantoKlayTransaction(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Pacific/Majuro")
    val time: Date,
    val from: String,
    val to: String,
    val klay: String,
    val fee: String,
    val link: String
)

enum class KrosslabAccountAddressType(val tokenAddress: String) {
    // fanto
    FANTO_INVESTMENT("0x1CaC964013fA4e684b048C1D4344E535C1517b9B"),
    FANTO_SERVICE("0x4A2ED0d35e904a5A2f3a7058F6ADf117B9725Ca7"),
    FANTO_HOLDINGS("0x81fB511FB63848725Ee3852150cb99Fb2C7acbc8"),
    FANTO_NFT_PAYOUT("0x6f2601EBad84bC685EAdc464672589AA193a114e"),
    FANTO_AIRDROP_PAYOUT("0x05dF8BF21fC5634675c8A7Dd63CcbA4b1643A0eE"),
    FANTO_MARKETING("0x8cd5E56542a45efa16BAD2E690F227d01794325d"),
    FANTO_OPERATION("0x60E16483c8E35ACa47ea5634f75Fce7E4B5807c4"),

    // proverse
    PROVERSE_INVESTMENT("0xd44bd0BD55FA8887B67D3661281d61c6C70eceA9"),
    PROVERSE_SERVICE("0x4Ebd949c8E93B9Bb6644cb4ea17C157A3DA4729b"),

    // sokuri
    SOKURI_INVESTMENT("0x81e88c60F2a76A49b47Ee002485377610443239a"),

    // krosslab
    KROSSLAB_SG_FANTO("0x8e69bF8CD2F8b226085708155fD44Bd4134912Ee"),
    KROSSLAB_SG_PROVERSE("0xEe3cab9720542b6A3C32FC0Aa62Cec132c130169"),
    KROSSLAB_SG_SOKURI("0xf8a4d6cFD13b3CB7f84aF8B6092e5dd96c91EbEE"),
}

enum class KrosslabTokenAddressType(val tokenAddress: String) {
    FANTO_LAYV("0xe48abc45d7cb8b7551334cf65ef50b8128032b72"),
    FANTO_SGAS("0xdaff9de20f4ba826565b8c664fef69522e818377"),
    FANTO_FTN("0xab28e65341af980a67dab5400a03aaf41fef5b7e"),
    FANTO_URT("0x01839ee16e16c0c0b771b78cce6265c75c290110"),
    FANTO_0X("0xbe612a268b82b9365feee67afd34d26aaca0d6de"),
    FANTO_FGI("0xe41f4664daa237ae01747ecc7c3151280c2fc8bf"),
    FANTO_OXT("0x8e6db43ad726bb9049078b5dcc9f86ae2e6a2246"),

    PROVERSE_TKLE("0x2e5d4d152113cfdc822ece59d1c2d416010a8e82"),
}