package io.klaytn.finder.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.klaytn.finder.infra.error.DefaultHandlerExceptionResolver
import io.klaytn.finder.infra.error.ExceptionHandler
import io.klaytn.finder.infra.web.interceptor.KaiaUserSessionInterceptor
import io.klaytn.finder.infra.web.interceptor.MaintenanceInterceptor
import io.klaytn.finder.infra.web.interceptor.UserSessionInterceptor
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.CacheControl
import org.springframework.web.servlet.HandlerExceptionResolver
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor
import org.springframework.web.servlet.mvc.WebContentInterceptor

@Configuration
class WebMvcConfig(
    val localeChangeInterceptor: LocaleChangeInterceptor,
    val maintenanceInterceptor: MaintenanceInterceptor,
    val userSessionInterceptor: UserSessionInterceptor,
    val kaiaUserSessionInterceptor: KaiaUserSessionInterceptor,
    val finderWebConfig: FinderWebConfig
) : WebMvcConfigurer {
    lateinit var handlerExceptionResolver: DefaultHandlerExceptionResolver

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOriginPatterns(*finderWebConfig.allowedOriginPatterns.toTypedArray())
            .allowedMethods(*finderWebConfig.allowedMethods.toTypedArray())
            .allowedHeaders(*finderWebConfig.allowedHeaders.toTypedArray())
            .allowCredentials(finderWebConfig.allowCredentials)
            .maxAge(finderWebConfig.maxAge)
    }

    @Bean
    fun exceptionHandler(messageSource: MessageSource, objectMapper: ObjectMapper) =
        ExceptionHandler(messageSource, objectMapper)

    @Bean
    fun defaultHandlerExceptionResolver(exceptionHandler: ExceptionHandler) =
        DefaultHandlerExceptionResolver(exceptionHandler).also { handlerExceptionResolver = it }

    override fun configureHandlerExceptionResolvers(resolvers: MutableList<HandlerExceptionResolver>) {
        resolvers.add(handlerExceptionResolver)
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        super.addInterceptors(registry)

        registry.apply {
            addInterceptor(maintenanceInterceptor).excludePathPatterns("/actuator/**")
            addInterceptor(userSessionInterceptor).addPathPatterns("/api/**")
            addInterceptor(kaiaUserSessionInterceptor).addPathPatterns("/api/v1/kaia/users/**")
                .excludePathPatterns("/api/v1/kaia/users/sign-in")
                .excludePathPatterns("/api/v1/kaia/users/sign-up")
                .excludePathPatterns("/api/v1/kaia/users/verify-email")
            addInterceptor(localeChangeInterceptor)

            val noCacheInterceptor = WebContentInterceptor()
            noCacheInterceptor.cacheControl = CacheControl.noCache().mustRevalidate()
            addInterceptor(noCacheInterceptor)
        }
    }
}

@ConstructorBinding
@ConfigurationProperties(prefix = "finder.web.cors")
data class FinderWebConfig(
    val allowedOriginPatterns: List<String>,
    val allowedMethods: List<String>,
    val allowedHeaders: List<String>,
    val allowCredentials: Boolean,
    val maxAge: Long
)
