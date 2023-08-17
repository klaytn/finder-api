package io.klaytn.finder.infra.client.opensearch.model

import com.klaytn.caver.transaction.type.TransactionType
import io.klaytn.finder.infra.client.opensearch.OpenSearchTotalCountConstants
import io.klaytn.finder.infra.client.opensearch.TransactionFieldType
import io.klaytn.finder.infra.client.opensearch.TransactionSortType
import io.swagger.v3.oas.annotations.media.Schema
import org.opensearch.action.search.SearchRequest
import org.opensearch.action.support.IndicesOptions
import org.opensearch.index.query.QueryBuilder
import org.opensearch.index.query.QueryBuilders
import org.opensearch.search.builder.SearchSourceBuilder
import org.opensearch.search.sort.FieldSortBuilder
import org.springdoc.api.annotations.ParameterObject
import javax.validation.constraints.NotBlank

@ParameterObject
data class TransactionSearchRequestByAccountAddress(
    @field:NotBlank
    @field:Schema(
        required = true,
        title = "target account address",
    )
    val targetAccountAddress: String,
    val types: Set<TransactionType>?,
    val status: Boolean?,
) {
    fun toSearchRequest(
        indexPrefix: String,
        accountAddress: String,
        transactionSearchPageRequest: TransactionSearchPageRequest
    ): SearchRequest {
        val sortOrder = transactionSearchPageRequest.sortOrder()
        val sortFields =
            if(transactionSearchPageRequest.sortType == TransactionSortType.BLOCK_NUMBER) {
                listOf(
                    FieldSortBuilder(transactionSearchPageRequest.sortType.field.fieldName).order(sortOrder),
                    FieldSortBuilder(TransactionFieldType.TRANSACTION_INDEX.fieldName).order(sortOrder))
            } else {
                listOf(FieldSortBuilder(transactionSearchPageRequest.sortType.field.fieldName).order(sortOrder))
            }

        val query = createQueryBuilder(accountAddress)
        val searchSourceBuilder = SearchSourceBuilder()
            .trackTotalHitsUpTo(OpenSearchTotalCountConstants.maxTransactionTotalCount.toInt())
            .fetchSource(false)
            .query(query)
            .from(transactionSearchPageRequest.offset())
            .size(transactionSearchPageRequest.size)
        sortFields.map { searchSourceBuilder.sort(it) }

        return SearchRequest("${indexPrefix}-*")
            .source(searchSourceBuilder)
            .indicesOptions(IndicesOptions.lenientExpandOpen())
    }

    private fun createQueryBuilder(accountAddress: String): QueryBuilder {
        val query = QueryBuilders.boolQuery()
        if(!types.isNullOrEmpty()) {
            query.filter(QueryBuilders.termsQuery(TransactionFieldType.TYPE.fieldName, types.map { it.type }))
        }
        status?.let { query.filter(QueryBuilders.termQuery(TransactionFieldType.STATUS.fieldName, it)) }
        query.filter(
            QueryBuilders.boolQuery()
                .should(
                    QueryBuilders.boolQuery()
                        .filter(QueryBuilders.termQuery(TransactionFieldType.FROM.fieldName, accountAddress))
                        .filter(QueryBuilders.termQuery(TransactionFieldType.TO.fieldName, targetAccountAddress))
                )
                .should(
                    QueryBuilders.boolQuery()
                        .filter(QueryBuilders.termQuery(TransactionFieldType.TO.fieldName, accountAddress))
                        .filter(QueryBuilders.termQuery(TransactionFieldType.FROM.fieldName, targetAccountAddress))
                )
        )
        return query
    }
}