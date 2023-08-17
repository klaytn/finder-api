package io.klaytn.finder.infra.web.model

import io.klaytn.commons.model.mapper.ListMapper
import io.klaytn.commons.model.mapper.Mapper
import org.springframework.data.domain.Page
import kotlin.math.ceil

data class ScopePage<T>(
    val results: List<T>,
    val paging: ScopePaging,
    val property: Map<String, Any>? = null
) {
    companion object {
        fun <I, O> of(page: Page<I>, mapper: Mapper<I, O>, propertyMap: Map<String, Any>? = null): ScopePage<O> {
            val results = page.content.map { mapper.transform(it) }
            return ScopePage(results, ScopePaging.getPaging(page), propertyMap)
        }

        fun <I, O> of(page: Page<I>, mapper: ListMapper<I, O>, propertyMap: Map<String, Any>? = null): ScopePage<O> {
            val results = mapper.transform(page.content)
            return ScopePage(results, ScopePaging.getPaging(page), propertyMap)
        }

        fun <T> of(page: Page<T>) = ScopePage(page.content, ScopePaging.getPaging(page))
    }
}

data class ScopePaging(
    val totalCount: Long,
    val currentPage: Int,
    val last: Boolean,
    private val pageSize: Int,
) {
    val totalPage: Int
        get() = if (pageSize == 0) 1 else ceil(totalCount / pageSize.toDouble()).toInt()

    companion object {
        fun <T> getPaging(page: Page<T>) =
            ScopePaging(
                totalCount = page.totalElements,
                currentPage = page.pageable.pageNumber + 1,
                last = page.isLast,
                pageSize = page.pageable.pageSize,
            )
    }
}
