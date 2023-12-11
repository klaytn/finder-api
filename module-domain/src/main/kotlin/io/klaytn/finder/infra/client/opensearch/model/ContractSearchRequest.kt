package io.klaytn.finder.infra.client.opensearch.model

import io.klaytn.finder.domain.common.ContractType
import io.klaytn.finder.infra.client.opensearch.ContractFieldType
import io.klaytn.finder.infra.utils.DateUtils
import io.klaytn.finder.infra.utils.OpenSearchUtils
import org.opensearch.action.search.SearchRequest
import org.opensearch.index.query.QueryBuilder
import org.opensearch.index.query.QueryBuilders
import org.opensearch.index.query.RangeQueryBuilder
import org.opensearch.search.builder.SearchSourceBuilder
import org.opensearch.search.sort.FieldSortBuilder
import org.springdoc.api.annotations.ParameterObject
import org.opensearch.common.unit.TimeValue
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@ParameterObject
data class ContractSearchRequest(
    val contractType: Set<ContractType>,
    /**
     *  name or symbol
     */
    val keyword: String?,
    val verified: Boolean?,
    val fromCreatedAt: LocalDateTime?,
    val toCreatedAt: LocalDateTime?,
    val contractSearchPageRequest: ContractSearchPageRequest
) {
    companion object {
        fun ofPageForToken(
            keyword: String?,
            verified: Boolean?,
            fromDate: LocalDate?,
            toDate: LocalDate?,
            contractSearchPageRequest: ContractSearchPageRequest
        ) = ofPage(ContractType.getTokenTypes(), keyword, verified, fromDate, toDate, contractSearchPageRequest)

        fun ofPageForNft(
            keyword: String?,
            verified: Boolean?,
            fromDate: LocalDate?,
            toDate: LocalDate?,
            contractSearchPageRequest: ContractSearchPageRequest
        ) = ofPage(ContractType.getNftTypes(), keyword, verified, fromDate, toDate, contractSearchPageRequest)

        private fun ofPage(
            contractTypes: Set<ContractType>,
            keyword: String?,
            verified: Boolean?,
            fromDate: LocalDate?,
            toDate: LocalDate?,
            contractSearchPageRequest: ContractSearchPageRequest
        ) =
            ContractSearchRequest(
                contractTypes,
                keyword,
                verified,
                if (fromDate != null) LocalDateTime.of(fromDate, LocalTime.MIN) else null,
                if (toDate != null) LocalDateTime.of(toDate, LocalTime.MAX) else null,
                contractSearchPageRequest
            )
    }

    fun toSearchRequest(index: String, maxGram: Int): SearchRequest {
        val query = createQueryBuilder(maxGram)
        val sort = FieldSortBuilder(contractSearchPageRequest.sortType.fieldType.fieldName)
            .order(contractSearchPageRequest.sortOrder())

        return SearchRequest(index).source(
            SearchSourceBuilder()
                .fetchSource(false)
                .query(query)
                .from(contractSearchPageRequest.offset())
                .timeout(TimeValue.timeValueSeconds(10))
                .size(contractSearchPageRequest.size)
                .sort(sort)
        )
    }

    private fun createQueryBuilder(maxGram: Int): QueryBuilder {
        val queryBuilder = QueryBuilders.boolQuery()

        queryBuilder.filter(
            QueryBuilders.termsQuery(ContractFieldType.CONTRACT_TYPE.fieldName, contractType.map { it.value }))
        keyword?.let {
            queryBuilder.filter(
                OpenSearchUtils.boolQueryBuilderApplyNgramTokenizer(
                listOf(ContractFieldType.NAME.fieldName, ContractFieldType.SYMBOL.fieldName), keyword, maxGram)
            ) }
        verified?.let {
            queryBuilder.filter(QueryBuilders.termQuery(ContractFieldType.VERIFIED.fieldName, verified))
        }

        if(fromCreatedAt != null) {
            val from = DateUtils.localDateTimeToEpochMilli(fromCreatedAt)
            val createdRangeQueryBuilder =
                RangeQueryBuilder(ContractFieldType.CREATED_AT.fieldName).format("epoch_millis").gte(from)
            if(toCreatedAt != null) {
                val to = DateUtils.localDateTimeToEpochMilli(toCreatedAt)
                createdRangeQueryBuilder.lte(to)
            }
            queryBuilder.filter(createdRangeQueryBuilder)
        }
        return queryBuilder
    }
}