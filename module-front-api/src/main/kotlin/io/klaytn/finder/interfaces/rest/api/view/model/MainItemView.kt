package io.klaytn.finder.interfaces.rest.api.view.model

import io.klaytn.finder.config.ApplicationProperty
import io.klaytn.finder.config.dynamic.FinderServerDynamicConfig
import io.swagger.v3.oas.annotations.media.Schema

@Schema
data class MainItemView(
    val serverTimeMillis: Long,
    val application: ApplicationProperty,
    val config: MainConfigView,
)

data class MainConfigView(
    val server: FinderServerDynamicConfig,
    val client: Map<String,Any>
)