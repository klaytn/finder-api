package io.klaytn.finder.infra.web.model

import io.swagger.v3.oas.annotations.Parameter
import javax.validation.constraints.Min
import org.hibernate.validator.constraints.Range
import org.springdoc.api.annotations.ParameterObject
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

@ParameterObject
data class EtherscanLikeSimplePageRequest(
        @field:Parameter(
                description = "the integer page number, if pagination is enabled",
                example = "1"
        )
        @field:Min(1)
        val page: Int = 1,
        @field:Parameter(
                name = "offset",
                description = "the number of transactions displayed per page (Maximum 10,000)",
                example = "20"
        )
        @field:Range(min = 1, max = 10000)
        val offset: Int = 20,
        @field:Parameter(
                name = "sort",
                description = "asc or desc",
        )
        val sort: String? = "asc",
) {
    fun pageRequest(sort: Sort) = PageRequest.of(page - 1, offset, sort)

    fun pageRequest() = PageRequest.of(page - 1, offset)

    fun offset() = (page - 1) * offset

    fun sort(): Sort {
        return if (sort == "desc") {
            Sort.by(Sort.Order.desc("blockNumber"), Sort.Order.desc("transactionIndex"))
        } else {
            Sort.by(Sort.Order.asc("blockNumber"), Sort.Order.asc("transactionIndex"))
        }
    }
}
