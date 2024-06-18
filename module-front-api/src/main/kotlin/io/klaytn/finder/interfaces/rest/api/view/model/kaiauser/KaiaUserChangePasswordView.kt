package io.klaytn.finder.interfaces.rest.api.view.model.kaiauser

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

@Schema
data class KaiaUserChangePasswordView (
    @Schema(title = "User Name")
    val name: String,

    @Schema(title = "Old Password")
    @JsonProperty("old_password") val oldPassword: String,

    @Schema(title = "New Password")
    @JsonProperty("new_password") val newPassword: String,

    @Schema(title = "Confirm Password")
    @JsonProperty("confirm_password") val confirmPassword: String
)