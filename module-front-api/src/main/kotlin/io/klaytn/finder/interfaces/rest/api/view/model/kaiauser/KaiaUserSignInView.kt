package io.klaytn.finder.interfaces.rest.api.view.model.kaiauser

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

@Schema
data class KaiaUserSignInView (
    @Schema(title="Session ID")
    @JsonProperty("session_id") val sessionId: String,

    @Schema(title="User Name")
    @JsonProperty("user_name") val userName: String,

    @Schema(title="Password")
    val password: String
)