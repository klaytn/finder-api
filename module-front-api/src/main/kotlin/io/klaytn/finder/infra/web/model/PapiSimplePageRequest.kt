package io.klaytn.finder.infra.web.model

import io.swagger.v3.oas.annotations.Parameter
import javax.validation.constraints.Min
import org.hibernate.validator.constraints.Range
import org.springdoc.api.annotations.ParameterObject

@ParameterObject
data class PapiSimplePageRequest(
        @field:Parameter(description = "Results page you want to retrieve (1..N)", example = "1")
        @field:Min(1)
        val page: Int = 1,
        @field:Parameter(description = "Number of records per page", example = "20")
        @field:Range(min = 1, max = 1000)
        val size: Int = 20,
) {
    fun toSimplePageRequest() = SimplePageRequest(page, size)
}
