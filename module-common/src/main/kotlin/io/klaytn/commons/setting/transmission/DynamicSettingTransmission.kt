package io.klaytn.commons.setting.transmission

import com.google.common.collect.Maps
import io.klaytn.commons.curator.CuratorConstants
import io.klaytn.commons.lifecycle.ApplicationContextInitializedListener
import io.klaytn.commons.setting.dynamic.DynamicSetting
import io.klaytn.commons.setting.dynamic.DynamicSettingHandler
import io.klaytn.commons.setting.dynamic.DynamicSettingSupport
import io.klaytn.commons.utils.Jackson
import io.klaytn.commons.utils.logback.logger
import org.apache.commons.lang3.ObjectUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.reflect.FieldUtils
import org.apache.commons.lang3.reflect.MethodUtils
import org.apache.commons.lang3.tuple.Triple
import org.springframework.context.ApplicationContext
import org.springframework.data.util.Pair
import org.springframework.util.ReflectionUtils
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.*
import java.util.stream.Collectors

class DynamicSettingTransmission(
    applicationContext: ApplicationContext
) : ApplicationContextInitializedListener(applicationContext), CuratorChangeTransmission {
    private val logger = logger(this::class.java)

    private val jackson = Jackson.mapper()

    private val dynamicPropertyFieldMap: MutableMap<String, Pair<DynamicSettingSupport, Field>> =
        Maps.newConcurrentMap()

    private val dynamicPropertyMethodMap: MutableMap<String, Triple<DynamicSettingSupport, Method, String>> =
        Maps.newConcurrentMap()

    override fun change(key: String, value: String?) {
        val fieldPair: Pair<DynamicSettingSupport, Field>? = dynamicPropertyFieldMap[key]
        fieldPair?.let { processDynamicFiled(key, value, it) }

        val methodTriple: Triple<DynamicSettingSupport, Method, String>? = dynamicPropertyMethodMap[key]
        methodTriple?.let { processDynamicMethod(key, value, it) }
    }

    override fun onApplicationContextInitialized(applicationContext: ApplicationContext) {
        val dynamicSettingSupportMap: Map<String, DynamicSettingSupport> = applicationContext.getBeansOfType(
            DynamicSettingSupport::class.java
        )

        dynamicSettingSupportMap.forEach{ (name: String, dynamicSettingSupport: DynamicSettingSupport) ->
            try {
                var rootPath: String = dynamicSettingSupport.getRootPath()
                if (!rootPath.endsWith(CuratorConstants.PATH_SEPARATOR)) {
                    rootPath += CuratorConstants.PATH_SEPARATOR
                }

                // extract fields
                val fields = FieldUtils.getFieldsListWithAnnotation(
                    dynamicSettingSupport.javaClass,
                    DynamicSetting::class.java
                )
                for (field in fields) {
                    val dynamicSetting: DynamicSetting = field.getAnnotation(DynamicSetting::class.java)
                    ReflectionUtils.makeAccessible(field)
                    val subPath: String = StringUtils.defaultIfBlank(dynamicSetting.path, field.name)
                    val fullPath = getFullPath(rootPath, subPath)
                    dynamicPropertyFieldMap[fullPath] = Pair.of(dynamicSettingSupport, field)
                    logger.info(
                        "DynamicPropertyField of Path('{}') is {}#{}.", fullPath,
                        dynamicSettingSupport.javaClass.name, field.name
                    )
                }

                // extract methods
                val methods = MethodUtils.getMethodsListWithAnnotation(
                    dynamicSettingSupport.javaClass,
                    DynamicSettingHandler::class.java
                )
                for (method in methods) {
                    val dynamicSettingHandler: DynamicSettingHandler =
                        method.getAnnotation(DynamicSettingHandler::class.java)
                    for (dynamicSettingPath in dynamicSettingHandler.dynamicSettingPath) {
                        val fullPath = getFullPath(rootPath, dynamicSettingPath.path)
                        dynamicPropertyMethodMap[fullPath] =
                            Triple.of(dynamicSettingSupport, method, dynamicSettingPath.replace)
                        logger.info(
                            "DynamicPropertyMethod of Path('{}') is {}#{}.", fullPath,
                            dynamicSettingSupport.javaClass.name, method.name
                        )
                    }
                }
            } catch (exception: Exception) {
                logger.error(exception.message, exception)
            }
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // -- private
    // -----------------------------------------------------------------------------------------------------------------

    private fun getFullPath(rootPath: String, subPath: String): String {
        val fullPath = rootPath + subPath
        return Arrays.stream(
            fullPath.split(CuratorConstants.PATH_SEPARATOR.toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray())
            .map(StringUtils::trimToNull)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.joining(CuratorConstants.PATH_SEPARATOR))
    }

    private fun processDynamicFiled(key: String, value: String?, pair: Pair<DynamicSettingSupport, Field>) {
        val dynamicSettingSupport: DynamicSettingSupport = pair.first
        val targetField = pair.second
        try {
            val dynamicSetting: DynamicSetting = targetField.getAnnotation(DynamicSetting::class.java)
            val originalValue = targetField[dynamicSettingSupport]
            val newValue = if(value != null) jackson.readValue(value, targetField.type) else null
            if(newValue == null && !dynamicSetting.allowNull) {
                logger.warn(
                    "Target Field('{}') of Path('{}') don't allow null. So, Target Value('{}') don't changed. If you have to assign null, use class type(ex. Boolean, Integer, Long, etc).".trimIndent(),
                    targetField.name, key, originalValue
                )
                return
            }

            ReflectionUtils.setField(targetField, dynamicSettingSupport, newValue)
            val changedValue = targetField[dynamicSettingSupport]
            if (ObjectUtils.notEqual(originalValue, changedValue)) {
                logger.warn(
                    "Target Field('{}') of Path('{}') is changed. {} => {}", targetField.name, key,
                    originalValue, changedValue
                )
            }
        } catch (exception: Exception) {
            logger.error(exception.message, exception)
        }
    }

    private fun processDynamicMethod(
        key: String,
        value: String?,
        triple: Triple<DynamicSettingSupport, Method, String>
    ) {
        val dynamicSettingSupport: DynamicSettingSupport = triple.left
        val targetMethod = triple.middle
        val replaceKey = if (StringUtils.isNotBlank(triple.right)) triple.right else key
        try {
            targetMethod.invoke(dynamicSettingSupport, replaceKey, value)
        } catch (exception: Exception) {
            logger.error(exception.message, exception)
        }
    }
}