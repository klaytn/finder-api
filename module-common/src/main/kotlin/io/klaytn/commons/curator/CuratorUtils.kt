package io.klaytn.commons.curator

import java.nio.charset.StandardCharsets

class CuratorUtils {
    companion object {
        fun serialize(value: String?): ByteArray? {
            return value?.toByteArray(StandardCharsets.UTF_8)
        }

        fun deserialize(value: ByteArray?): String? {
            return if (value == null || value.isEmpty()) null
            else String(value, StandardCharsets.UTF_8)
        }
    }
}
