package io.klaytn.commons.setting

interface SettingManager {
    operator fun set(name: String, value: String?): Boolean
    operator fun get(name: String): String?
    fun getBoolean(name: String, defaultValue: Boolean): Boolean
    fun getInteger(name: String, defaultValue: Int): Int?
    fun getLong(name: String, defaultValue: Long): Long?
}
