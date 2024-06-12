package io.klaytn.finder.interfaces.rest.api.view.model.kaiauser

import io.swagger.v3.oas.annotations.media.Schema

@Schema
data class KaiaUserSignupView (
    @Schema(title="Name")
    val name: String,

    @Schema(title="Email")
    val email: String,

    @Schema(title="Password")
    val password: String,

    @Schema(title="Profile Image")
    val profileImage: String? = null,

    @Schema(title="Subscription Status")
    val isSubscribed: Boolean = false
)