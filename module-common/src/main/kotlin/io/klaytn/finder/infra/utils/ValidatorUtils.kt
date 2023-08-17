package io.klaytn.finder.infra.utils

class ValidatorUtils {
    companion object {
        private val emailRegEx = Regex("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$")

        fun isValidEmail(email: String): Boolean {
            return emailRegEx.matches(email)
        }

        fun isValidWebSiteAddress(webSiteAddress: String): Boolean {
            return webSiteAddress.startsWith("https://")
        }
    }
}
