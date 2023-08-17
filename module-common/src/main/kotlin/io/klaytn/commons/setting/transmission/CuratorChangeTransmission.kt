package io.klaytn.commons.setting.transmission

interface CuratorChangeTransmission {
    fun change(key: String, value: String?)
}
