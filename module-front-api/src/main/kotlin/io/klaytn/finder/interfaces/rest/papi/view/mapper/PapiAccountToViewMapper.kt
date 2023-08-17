package io.klaytn.finder.interfaces.rest.papi.view.mapper

import io.klaytn.commons.model.mapper.Mapper
import io.klaytn.finder.domain.mysql.set1.Account
import io.klaytn.finder.infra.utils.applyDecimal
import io.klaytn.finder.interfaces.rest.api.view.model.contract.ContractSummary
import io.klaytn.finder.interfaces.rest.papi.view.PapiAccountItemView
import io.klaytn.finder.service.ContractService
import io.klaytn.finder.service.caver.CaverAccountService
import org.springframework.stereotype.Component

@Component
class PapiAccountToItemViewMapper(
    val contractService: ContractService,
    val caverAccountService: CaverAccountService,
) : Mapper<Account, PapiAccountItemView> {
    override fun transform(source: Account): PapiAccountItemView {
        val balance = caverAccountService.getAccountBalance(source.address).applyDecimal(18)
        val contract = contractService.getContract(source.address)

        return PapiAccountItemView(
            address = source.address,
            accountType = source.accountType,
            balance = balance,
            totalTransactionCount = source.totalTransactionCount,
            contractType = source.contractType,
            info = ContractSummary.of(contract),
            contractCreatorAddress = source.contractCreatorAddress,
            contractCreatorTransactionHash = source.contractCreatorTransactionHash,
            addressLabel = source.addressLabel,
            knsDomain = source.knsDomain,
            tags = source.tags,
        )
    }
}
