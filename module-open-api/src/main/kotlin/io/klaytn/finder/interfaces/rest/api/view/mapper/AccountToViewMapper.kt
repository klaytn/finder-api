package io.klaytn.finder.interfaces.rest.api.view.mapper

import io.klaytn.commons.model.mapper.Mapper
import io.klaytn.finder.domain.mysql.set1.Account
import io.klaytn.finder.infra.utils.applyDecimal
import io.klaytn.finder.interfaces.rest.api.view.model.ContractSummary
import io.klaytn.finder.interfaces.rest.api.view.model.account.AccountView
import io.klaytn.finder.service.ContractService
import io.klaytn.finder.service.accountkey.KlaytnAccountKeyService
import io.klaytn.finder.service.caver.CaverAccountService
import io.klaytn.finder.view.mapper.AccountKeyToViewMapper
import org.springframework.stereotype.Component

@Component
class AccountToViewMapper(
    val contractService: ContractService,
    val caverAccountService: CaverAccountService,
    val klaytnAccountKeyService: KlaytnAccountKeyService,
    val accountKeyToViewMapper: AccountKeyToViewMapper,
) : Mapper<Account, AccountView> {
    override fun transform(source: Account): AccountView {
        val balance = caverAccountService.getAccountBalance(source.address).applyDecimal(18)
        val totalTransactionCount = caverAccountService.getTransactionCount(source.address)
        val accountKey = klaytnAccountKeyService.getKlaytnAccountKeyByAccountAddress(
            source.address)?.let { accountKeyToViewMapper.transform(it) }
        val contract = contractService.getContract(source.address)

        return AccountView(
            address = source.address,
            accountType = source.accountType,
            balance = balance,
            totalTransactionCount = totalTransactionCount.toLong(),
            contract = ContractSummary.of(contract),
            contractCreatorAddress = source.contractCreatorAddress,
            contractCreatorTransactionHash = source.contractCreatorTransactionHash,
            contractCreated = if (contract?.txError == true) false else null,
            accountKey = accountKey
        )
    }
}