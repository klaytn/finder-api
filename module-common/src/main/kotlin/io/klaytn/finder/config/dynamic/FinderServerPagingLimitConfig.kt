package io.klaytn.finder.config.dynamic

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.klaytn.commons.setting.dynamic.DynamicSetting
import io.klaytn.commons.setting.dynamic.DynamicSettingSupport
import io.klaytn.finder.config.ApplicationProperty
import org.springframework.stereotype.Component

@Component
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy::class)
class FinderServerPagingLimitConfig(
    private val applicationProperty: ApplicationProperty,
): DynamicSettingSupport {
    override fun getRootPath() = applicationProperty.getApplicationDynamicConfigPath("server/paging/limit")

    @DynamicSetting val default: Long = 40_000
    @DynamicSetting val block: Long = 40_000L
    @DynamicSetting val transaction: Long = 40_000L
    @DynamicSetting val internalTransaction: Long = 40_000L
    @DynamicSetting val accountTransaction: Long = 100_000_000L
    @DynamicSetting val eventLog: Long = 40_000L
    @DynamicSetting val tokenHolder: Long = 500_000L
    @DynamicSetting val nftInventory: Long = 500_000L
    @DynamicSetting val nft17Holder: Long = 500_000L

  /**
   * nft37Holder depends on the value of nftInventory.
   */
    fun getNft37Holder() = nftInventory
}