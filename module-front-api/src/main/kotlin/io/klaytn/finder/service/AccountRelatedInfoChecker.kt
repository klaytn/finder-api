package io.klaytn.finder.service

import io.klaytn.finder.domain.common.AccountAddress
import io.klaytn.finder.domain.common.AccountType
import io.klaytn.finder.domain.common.ContractType
import io.klaytn.finder.domain.mysql.set1.Account
import io.klaytn.finder.domain.mysql.set1.AccountKeyRepository
import io.klaytn.finder.domain.mysql.set1.ContractCodeRepository
import io.klaytn.finder.domain.mysql.set1.TransactionRepository
import io.klaytn.finder.domain.mysql.set3.EventLogRepository
import io.klaytn.finder.domain.mysql.set3.nft.Nft17HolderRepository
import io.klaytn.finder.domain.mysql.set3.nft.NftInventoryRepository
import io.klaytn.finder.domain.mysql.set3.nft.NftTransferRepository
import io.klaytn.finder.domain.mysql.set3.token.TokenHolderRepository
import io.klaytn.finder.domain.mysql.set3.token.TokenTransferRepository
import io.klaytn.finder.infra.cache.CacheName
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class AccountRelatedInfoChecker(
    private val transactionRepository: TransactionRepository,
    private val internalTransactionService: InternalTransactionService,
    private val eventLogRepository: EventLogRepository,
    private val contractCodeRepository: ContractCodeRepository,
    private val tokenTransferRepository: TokenTransferRepository,
    private val nftTransferRepository: NftTransferRepository,
    private val tokenHolderRepository: TokenHolderRepository,
    private val nft17HolderRepository: Nft17HolderRepository,
    private val nftInventoryRepository: NftInventoryRepository,
    private val accountKeyRepository: AccountKeyRepository

) {
    @Cacheable(cacheNames = [CacheName.ACCOUNT_RELATED_INFOS], key = "#account.address", unless = "#result == null")
    fun get(account: Account): Map<String, Boolean> {
        val accountAddress = account.address
        val accountTabMap = mutableMapOf(
            "account-key" to accountKeyRepository.existsByAccountAddress(accountAddress),
            "transaction" to transactionRepository.existsByAccountAddress(accountAddress),
            "proposed-blocks" to (account.contractType == ContractType.CONSENSUS_NODE),
            "fee-paid" to transactionRepository.existsByFeePayer(accountAddress),
            "internal-transaction" to internalTransactionService.existsByAccountAddress(accountAddress),
            "token-transfer" to tokenTransferRepository.existsByAccountAddress(accountAddress),
            "nft-transfer" to nftTransferRepository.existsByAccountAddress(accountAddress),
            "token-balance" to tokenHolderRepository.existsByHolderAddress(AccountAddress(accountAddress)),
            "kip-17-balance" to nft17HolderRepository.existsByHolderAddress(AccountAddress(accountAddress)),
            "kip-37-balance" to nftInventoryRepository.existsByContractTypeAndHolderAddress(
                ContractType.KIP37,
                AccountAddress(accountAddress)
            )
        )

        if (account.accountType == AccountType.SCA) {
            accountTabMap["event-log"] = eventLogRepository.existsByAddress(accountAddress)
            accountTabMap["contract-code"] = contractCodeRepository.existsByContractAddress(accountAddress)
        }
        return accountTabMap
    }
}