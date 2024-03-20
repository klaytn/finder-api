package io.klaytn.finder.infra.web.model

import io.swagger.v3.oas.annotations.Parameter
import javax.validation.constraints.Min
import org.hibernate.validator.constraints.Range
import org.springdoc.api.annotations.ParameterObject
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

@ParameterObject
data class SimplePageRequest(
        @field:Parameter(description = "Results page you want to retrieve (1..N)", example = "1")
        @field:Min(1)
        val page: Int = 1,
        @field:Parameter(description = "Number of records per page", example = "20")
        @field:Range(min = 1, max = 2000)
        val size: Int = 20,
) {
    fun pageRequest(sort: Sort) = PageRequest.of(page - 1, size, sort)

    fun pageRequest() = PageRequest.of(page - 1, size)

    fun offset() = (page - 1) * size
}
