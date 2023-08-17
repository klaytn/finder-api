package io.klaytn.finder.infra.client.opensearch.model

import io.klaytn.finder.infra.client.opensearch.ContractSearchSortType
import io.klaytn.finder.infra.web.model.SimplePageRequest
import io.klaytn.finder.infra.web.model.SortDirection
import io.swagger.v3.oas.annotations.Parameter
import org.hibernate.validator.constraints.Range
import org.opensearch.search.sort.SortOrder
import org.springdoc.api.annotations.ParameterObject
import org.springframework.data.domain.PageRequest
import javax.validation.constraints.Min

@ParameterObject
data class ContractSearchPageRequest(
    @field:Parameter(
        description = "Results page you want to retrieve (1..N)",
        example = "1"
    )
    @field:Min(1)
    val page: Int = 1,

    @field:Parameter(
        description = "Number of records per page",
        example = "20"
    )
    @field:Range(min = 1, max = 20)
    val size: Int = 20,

    @field:Parameter(
        description = "Type of sorting",
        example = "TIME"
    )
    val sortType: ContractSearchSortType = ContractSearchSortType.TIME,

    @field:Parameter(
        description = "Direction of sorting",
        example = "DESC"
    )
    val sortDirection: SortDirection = SortDirection.DESC,
){
    companion object{
        fun of(page: Int, size: Int) =
            ContractSearchPageRequest(page, size, ContractSearchSortType.TIME, SortDirection.DESC)

        fun of(simplePageRequest: SimplePageRequest) =
            ContractSearchPageRequest(simplePageRequest.page, simplePageRequest.size, ContractSearchSortType.TIME, SortDirection.DESC)
    }

    fun offset() = (page-1) * size

    fun sortOrder() =
        if(sortDirection == SortDirection.ASC) {
            SortOrder.ASC
        } else {
            SortOrder.DESC
        }

    fun pageRequest() = PageRequest.of(page - 1, size)

    fun simplePageRequest() = SimplePageRequest(page, size)
}
