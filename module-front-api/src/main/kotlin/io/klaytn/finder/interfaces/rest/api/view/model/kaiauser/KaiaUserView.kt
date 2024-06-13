package io.klaytn.finder.interfaces.rest.api.view.model.kaiauser

import com.fasterxml.jackson.annotation.JsonProperty
import io.klaytn.finder.domain.common.KaiaUserType
import io.swagger.v3.oas.annotations.media.Schema

@Schema
data class KaiaUserView(
    @Schema(title = "User Name")
    val name: String,

    @Schema(title = "Email")
    val email: String,

    @Schema(title = "Profile Image")
    @JsonProperty("profile_image") val profileImage: String?,

    @Schema(title = "Subscription Status")
    @JsonProperty("is_subscribed") val isSubscribed: Boolean,

    @Schema(title = "User Status")
    val status: KaiaUserType,

    @Schema(title = "Registration Timestamp")
    @JsonProperty("register_timestamp") val registerTimestamp: Int
)