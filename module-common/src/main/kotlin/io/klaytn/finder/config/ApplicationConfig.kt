package io.klaytn.finder.config

import com.fasterxml.jackson.annotation.JsonIgnore
import io.klaytn.commons.model.env.Phase
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ApplicationConfig {
  @Bean
  fun applicationProperty(
      @Value("\${spring.application.name}") applicationName: String,
      phase: Phase,
      chainProperties: ChainProperties?,
  ) =
      ApplicationProperty(applicationName, phase, chainProperties?.type)
}

data class ApplicationProperty(
    val name: String,
    val phase: Phase,
    val chainType: String?,

) {
    @JsonIgnore fun getApplicationCommonDynamicConfigPath(path: String) = getFullPath("common", path)
    @JsonIgnore fun getApplicationDynamicConfigPath(path: String) = getFullPath(name, path)

    private fun getFullPath(rootPath: String, path: String) =
        if(!chainType.isNullOrBlank()) "$rootPath/$chainType/$path" else "$rootPath/$path"
}