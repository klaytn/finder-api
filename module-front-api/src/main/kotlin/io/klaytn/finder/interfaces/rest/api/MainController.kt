package io.klaytn.finder.interfaces.rest.api

import io.klaytn.finder.config.ApplicationProperty
import io.klaytn.finder.infra.ServerMode
import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.klaytn.finder.interfaces.rest.api.view.model.MainItemView
import io.klaytn.finder.config.dynamic.FinderServerDynamicConfig
import io.klaytn.finder.config.dynamic.FinderClientDynamicConfig
import io.klaytn.finder.interfaces.rest.api.view.model.MainConfigView
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Profile(ServerMode.API_MODE)
@RestController
@Tag(name= SwaggerConstant.TAG_PUBLIC)
class MainController(
    private val applicationProperty: ApplicationProperty,
    private val finderServerDynamicConfig: FinderServerDynamicConfig,
    private val finderClientDynamicConfig: FinderClientDynamicConfig,
) {
    @Operation(
        description = "Provide Basic Information for the Frontend.",
    )
    @GetMapping("/api/v1/mains")
    fun getMain() = MainItemView(
        System.currentTimeMillis(),
        applicationProperty,
        MainConfigView(
            finderServerDynamicConfig,
            finderClientDynamicConfig.configMap
        )
    )
}
