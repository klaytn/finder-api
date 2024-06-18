package io.klaytn.finder.interfaces.rest.api.view.model.kaiauser

import io.swagger.v3.oas.annotations.media.Schema

@Schema
data class KaiaUserAccountView (
    @Schema(title = "User Name")
    val name: String,

    @Schema(title = "Email")
    val email: String,

    @Schema(title = "Last Login")
    val lastLogin: String?
)