package io.klaytn.finder.view.mapper

import io.klaytn.commons.model.mapper.Mapper
import io.klaytn.finder.domain.common.AccountAddress
import io.klaytn.finder.view.model.account.AccountAddressView
import org.springframework.stereotype.Component

@Component
class AccountAddressToViewMapper : Mapper<AccountAddress?, AccountAddressView?> {
    override fun transform(source: AccountAddress?): AccountAddressView? {
        return source?.let {
            AccountAddressView(
                address = it.address,
                accountType = it.accountType,
                contractType = it.contractType,
                symbol = it.contract?.symbol,
                icon = it.contract?.icon,
                name = it.contract?.name,
                verified = it.contract?.verified,
                knsDomain = it.knsDomain,
                addressLabel = it.addressLabel
            )
        }
    }
}