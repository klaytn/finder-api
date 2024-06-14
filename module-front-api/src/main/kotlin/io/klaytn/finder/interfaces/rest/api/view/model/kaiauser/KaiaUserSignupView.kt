package io.klaytn.finder.interfaces.rest.api.view.model.kaiauser

import com.fasterxml.jackson.annotation.JsonProperty
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
    @JsonProperty("profile_image") val profileImage: String?,

    @Schema(title="Subscription Status")
    @JsonProperty("is_subscribed") val isSubscribed: Boolean
)