package io.klaytn.finder.infra.utils

import io.klaytn.finder.infra.exception.InvalidRequestException
import io.klaytn.finder.infra.web.model.SimplePageRequest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import kotlin.math.ceil

class PageUtils {
    companion object {
        fun <T> getPage(contents: List<T>, simplePageRequest: SimplePageRequest, totalCount: Long) =
            PageImpl(contents, simplePageRequest.pageRequest(), totalCount)

        fun checkPageParameter(pageable: Pageable, totalCount: Long) {
            if (totalCount == 0L && pageable.pageNumber == 0) {
                return
            }

            if (ceil(totalCount.toDouble() / pageable.pageSize) <= pageable.pageNumber) {
                throw InvalidRequestException("totalPage is over.")
            }
        }

        fun checkPageParameter(simplePageRequest: SimplePageRequest, totalCount: Long) {
            checkPageParameter(simplePageRequest.pageRequest(), totalCount)
        }

        fun <T> emptyPage(simplePageRequest: SimplePageRequest) =
            PageImpl<T>(emptyList(), simplePageRequest.pageRequest(), 0)
    }
}