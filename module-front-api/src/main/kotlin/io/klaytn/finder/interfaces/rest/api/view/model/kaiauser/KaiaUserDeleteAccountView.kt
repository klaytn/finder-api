package io.klaytn.finder.interfaces.rest.api.view.model.kaiauser

import io.swagger.v3.oas.annotations.media.Schema

@Schema
data class KaiaUserDeleteAccountView(
    @Schema(title = "User Name")
    val name: String,

    @Schema(title = "Password")
    val password: String
)