package io.klaytn.finder.compiler.config

import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.tags.Tag
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SpringDocConfig {
    @Bean
    fun api(): OpenAPI {
        val info = Info()
            .title("Finder Compiler API Server")
            .description("Finder Compiler API Server")
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
                        Tag().name(SwaggerConstant.TAG_PUBLIC).description("APIs exposed to the outside"),
                        Tag().name(SwaggerConstant.TAG_PRIVATE).description("APIs used within the internal network"),
                        Tag().name(SwaggerConstant.TAG_PRIVATE_ADMIN).description("APIs provided for internal network admin"),
                        Tag().name(SwaggerConstant.TAG_PRIVATE_INFRA).description("APIs provided for internal network infrastructure"),
                    )
                )
    }
}
