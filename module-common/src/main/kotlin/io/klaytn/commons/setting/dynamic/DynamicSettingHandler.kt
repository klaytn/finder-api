package io.klaytn.commons.setting.dynamic

@Target(
        AnnotationTarget.FUNCTION,
        AnnotationTarget.PROPERTY_GETTER,
        AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
annotation class DynamicSettingHandler(val dynamicSettingPath: Array<DynamicSettingPath>)
