package io.klaytn.finder.config

import dev.akkinoc.util.YamlResourceBundle
import java.util.*
import org.springframework.boot.autoconfigure.context.MessageSourceProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor
import org.springframework.web.servlet.i18n.SessionLocaleResolver

@Configuration
class MessageConfig {
    @Bean fun localeResolver() = SessionLocaleResolver().apply { setDefaultLocale(Locale.ENGLISH) }

    @Bean fun localeChangeInterceptor() = LocaleChangeInterceptor().apply { paramName = "lang" }

    @Bean
    @ConfigurationProperties(prefix = "spring.messages")
    fun messageSourceProperties() = MessageSourceProperties()

    @Bean
    fun messageSource(messageSourceProperties: MessageSourceProperties) =
            YamlMessageSource().apply {
                setBasenames(
                        *messageSourceProperties
                                .basename
                                .split(",")
                                .map { it.trim() }
                                .toTypedArray()
                )
                setDefaultEncoding(messageSourceProperties.encoding.displayName())
                setAlwaysUseMessageFormat(true)
                setUseCodeAsDefaultMessage(true)
                setFallbackToSystemLocale(true)
            }

    class YamlMessageSource : ResourceBundleMessageSource() {
        override fun doGetBundle(basename: String, locale: Locale): ResourceBundle =
                ResourceBundle.getBundle(basename, locale, YamlResourceBundle.Control)
    }
}
