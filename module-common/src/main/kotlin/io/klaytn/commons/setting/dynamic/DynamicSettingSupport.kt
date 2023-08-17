package io.klaytn.commons.setting.dynamic

import com.fasterxml.jackson.annotation.JsonIgnore

/**
 * Implement DynamicSettingSupport, specify the ZooKeeper node path for the field using DynamicSetting#path,
 * and register it as a Spring bean.
 *
 * Objects registered as Spring beans are loaded by the [io.klaytn.commons.setting.transmission.DynamicSettingTransmission] class,
 * and the values changed in ZooKeeper due to the behavior set on fields or methods are processed.
 *
 * 1) Mapping to a Specific Field
 * If you set @DynamicSetting on a specific field, the value at the configured path is bound to that field.
 * Note that if DynamicSetting#path is not specified, it defaults to the field name.
 *
 * 2) Method Triggering
 * When the value at the path configured in @DynamicSettingHandler changes, the corresponding method is called.
 * The path() configured in @DynamicSettingPath is passed as the key value of the method.
 * If you want to receive it as a different value, you can set the replace() option.
 *
 * example)
 * <pre>
 * @Component
 * class TmsSetting: DynamicSettingSupport {
 *      override fun getRootPath() = "test/"
 *
 *      @DynamicSetting
 *      var failedRetryMaxLoopCount = 30
 *
 *      @DynamicSetting(path = "failed_retry/partition")
 *      var failedRetryPartition: Int = 1000
 *
 *      @DynamicSetting(path = "reservation_revise/partition")
 *      var reservationRevisePartition: Int = 1000
 *
 *      @DynamicSetting
 *      var overnight: TmsOvernightHour = TmsOvernightHour(21, 8)
 *
 *      @DynamicSetting(path = "switch/bzm")
 *      var isOnBz: Boolean = false
 *
 *      @DynamicSettingHandler(
 *          dynamicSettingPath = [
 *              DynamicSettingPath(path = "test", replace = "replaceKey")
 *          ]
 *      )
 *      fun handler(key: String, value: String?) {
 *          loggger.info("{} is {}", key, value);
 *      }
 * }
 *</pre>
 *
 * @see io.klaytn.commons.setting.transmission.DynamicSettingTransmission
 */
interface DynamicSettingSupport {
    @JsonIgnore
    fun getRootPath(): String
}