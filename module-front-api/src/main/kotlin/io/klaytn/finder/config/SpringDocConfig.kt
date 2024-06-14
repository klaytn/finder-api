package io.klaytn.finder.config

import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.tags.Tag
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class SpringDocConfig {
    @Bean
    fun api(chainProperties: ChainProperties): OpenAPI {
        val info = Info()
            .title("[${chainProperties.type}] Finder Front API Server")
            .description("[${chainProperties.type}] Finder Front API Server")
            .version("0.0.1")

        val components = Components().addSecuritySchemes(
            "Authorization",
            SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT"))

        return OpenAPI()
            .info(info)
            .components(components)
            .security(
                listOf(SecurityRequirement().addList("Authorization")))
            .tags(
                listOf(
                    Tag().name(SwaggerConstant.TAG_PUBLIC).description("APIs exposed to the public"),
                    Tag().name(SwaggerConstant.TAG_PRIVATE).description("APIs for internal use"),
                    Tag().name(SwaggerConstant.TAG_PRIVATE_ADMIN).description("APIs for internal admin use"),
                    Tag().name(SwaggerConstant.TAG_PRIVATE_INFRA).description("APIs for internal infrastructure use"),
                )
            )
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}
