package io.klaytn.commons.setting.dynamic

import org.apache.commons.lang3.StringUtils

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class DynamicSettingPath(val path: String, val replace: String = StringUtils.EMPTY)
