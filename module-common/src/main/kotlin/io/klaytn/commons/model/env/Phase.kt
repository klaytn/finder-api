package io.klaytn.commons.model.env

enum class Phase {
    local,
    dev,
    stag,
    prod;

    companion object {
        private val stringMap = values().associateBy { it.name }

        @JvmStatic
        fun fromProfiles(profiles: Array<String>): Phase {
            if (profiles.isEmpty()) {
                throw IllegalArgumentException("Unknown profiles")
            }

            profiles.forEach {
                if (stringMap.containsKey(it.lowercase())) {
                    return stringMap[it.lowercase()]!!
                }
            }

            throw IllegalArgumentException("Unknown profiles")
        }
    }
}
