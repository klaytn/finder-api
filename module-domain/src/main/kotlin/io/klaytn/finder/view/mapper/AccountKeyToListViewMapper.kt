package io.klaytn.finder.view.mapper

import io.klaytn.commons.model.mapper.ListMapper
import io.klaytn.finder.domain.mysql.set1.AccountKey
import io.klaytn.finder.infra.utils.DateUtils
import io.klaytn.finder.service.TransactionService
import io.klaytn.finder.service.accountkey.KlaytnAccountKeyService
import io.klaytn.finder.view.model.account.AccountKeyListView
import io.klaytn.finder.view.model.transaction.TransactionTypeView
import org.springframework.stereotype.Component

@Component
class AccountKeyToListViewMapper(
    private val transactionService: TransactionService,
    private val klaytnAccountKeyService: KlaytnAccountKeyService,
    private val accountKeyToViewMapper: AccountKeyToViewMapper,
): ListMapper<AccountKey, AccountKeyListView> {
    override fun transform(source: List<AccountKey>): List<AccountKeyListView> {
        val transactionHashes = source.map { it.transactionHash }.toList()
        val transactionMap =
            transactionService.getTransactionByHashes(transactionHashes).associateBy { it.transactionHash }
        return source.map { accountKey ->
            val transaction = transactionMap[accountKey.transactionHash]!!
            val klaytnAccountKey = klaytnAccountKeyService.getKlaytnAccountKeyWithJson(
                accountKey.accountKey).run { accountKeyToViewMapper.transform(this) }

            AccountKeyListView(
                blockNumber = accountKey.blockNumber,
                transactionHash = accountKey.transactionHash,
                transactionType = TransactionTypeView.getView(transaction.type),
                accountAddress = accountKey.accountAddress,
                key = transaction.key,
                accountKeyType = klaytnAccountKey.type,
                accountKey = klaytnAccountKey,
                datetime = DateUtils.from(transaction.timestamp)
            )
        }
    }
}