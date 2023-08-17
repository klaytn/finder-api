package io.klaytn.commons.setting.dynamic

import org.apache.commons.lang3.StringUtils

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class DynamicSetting(
        val path: String = StringUtils.EMPTY,
        val allowNull: Boolean = false,
)
