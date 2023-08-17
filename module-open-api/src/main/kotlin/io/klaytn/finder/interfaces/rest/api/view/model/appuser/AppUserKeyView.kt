package io.klaytn.finder.interfaces.rest.api.view.model.appuser

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema
data class AppUserKeyView(
        @Schema(title = "App User ID") val appUserId: Long,
        @Schema(title = "App Key") val accessKey: String,
        @Schema(title = "App Key Name") val name: String,
        @Schema(title = "AppKey Description") val description: String?,
        @Schema(title = "Activation Date of App Key") val activatedAt: LocalDateTime?,
        @Schema(title = "Deactivation Date of App Key") val deactivatedAt: LocalDateTime?,
)
