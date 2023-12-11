package io.klaytn.finder.infra.client.opensearch.model

import com.klaytn.caver.transaction.type.TransactionType
import io.klaytn.finder.infra.client.opensearch.OpenSearchTotalCountConstants
import io.klaytn.finder.infra.client.opensearch.TransactionFieldType
import io.klaytn.finder.infra.client.opensearch.TransactionSortType
import io.klaytn.finder.infra.exception.InvalidRequestException
import io.klaytn.finder.infra.utils.DateUtils
import io.swagger.v3.oas.annotations.Parameter
import org.opensearch.action.search.SearchRequest
import org.opensearch.action.support.IndicesOptions
import org.opensearch.index.query.QueryBuilder
import org.opensearch.index.query.QueryBuilders
import org.opensearch.index.query.RangeQueryBuilder
import org.opensearch.search.builder.SearchSourceBuilder
import org.opensearch.search.sort.FieldSortBuilder
import org.springdoc.api.annotations.ParameterObject
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.format.annotation.DateTimeFormat.ISO
import org.opensearch.common.unit.TimeValue
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.validation.constraints.NotBlank

@ParameterObject
data class TransactionSearchRequest(
    @NotBlank
    @DateTimeFormat(iso = ISO.DATE_TIME)
    @field:Parameter(
        required = true,
        description = "Search Start Date (ex. 2022-07-02T00:00:00)",
        example = "2022-07-02T00:00:00"
    )
    val fromAt: LocalDateTime?,
    @NotBlank
    @DateTimeFormat(iso = ISO.DATE_TIME)
    @field:Parameter(
        required = false,
        description = "Search End Date (ex. 2022-07-07T23:59:59). default is current time.",
        example = "2022-07-07T23:59:59",
    )
    val toAt: LocalDateTime? = LocalDateTime.now().withNano(0),
    val from: Set<String>?,
    val to: Set<String>?,
    val feePayer: Set<String>?,
    val blockNumberStart: Long?,
    val blockNumberEnd: Long?,
    val blockHash: String?,
    val types: Set<TransactionType>?,
    val status: Boolean?,
) {
    companion object {
        private const val datePattern = "yyyyMM"
        private val maxSearchRange: Duration = Duration.ofDays(31)
        private val maxSearchPeriod: Duration = Duration.ofDays(365)
    }

    fun toSearchRequest(indexPrefix: String, transactionSearchPageRequest: TransactionSearchPageRequest): SearchRequest {
        if(fromAt == null || toAt == null) {
            throw InvalidRequestException("The search range is required.")
        }

        val now = LocalDateTime.now().withNano(0)
        if(fromAt.isAfter(now)) {
            throw InvalidRequestException("The date of start must be before now.")
        }
        if(fromAt.isAfter(toAt)) {
            throw InvalidRequestException("The date of start must be before the last date.")
        }
        if(fromAt.isBefore(now.minusDays(maxSearchPeriod.toDays()))) {
            throw InvalidRequestException("The search start date must be within ${maxSearchPeriod.toDays()} days.")
        }
        if(Duration.between(fromAt, toAt).toSeconds() > maxSearchRange.toSeconds()) {
            throw InvalidRequestException("The search period ${maxSearchRange.toDays()} days exceeded.")
        }
        if(from.isNullOrEmpty() && to.isNullOrEmpty() && feePayer.isNullOrEmpty()) {
            throw InvalidRequestException("The account is required.")
        }

        val dateformat = DateTimeFormatter.ofPattern(datePattern)
        val searchPeriodToAt = if(toAt.isAfter(now)) now else toAt
        val fromUTCDate = DateUtils.toUTCDateString(fromAt, dateformat)
        val toUTCDate = DateUtils.toUTCDateString(searchPeriodToAt, dateformat)
        val indices = setOf("$indexPrefix-$fromUTCDate", "$indexPrefix-$toUTCDate")

        // sort
        val sortOrder = transactionSearchPageRequest.sortOrder()
        val sortFields =
            if(transactionSearchPageRequest.sortType == TransactionSortType.BLOCK_NUMBER) {
                listOf(
                    FieldSortBuilder(transactionSearchPageRequest.sortType.field.fieldName).order(sortOrder),
                    FieldSortBuilder(TransactionFieldType.TRANSACTION_INDEX.fieldName).order(sortOrder))
            } else {
                listOf(FieldSortBuilder(transactionSearchPageRequest.sortType.field.fieldName).order(sortOrder))
            }

        val query = createQueryBuilder(searchPeriodToAt)
        val searchSourceBuilder = SearchSourceBuilder()
            .trackTotalHitsUpTo(OpenSearchTotalCountConstants.maxTransactionTotalCount.toInt())
            .fetchSource(false)
            .query(query)
            .from(transactionSearchPageRequest.offset())
            .timeout(TimeValue.timeValueSeconds(5))
            .size(transactionSearchPageRequest.size)
        sortFields.map { searchSourceBuilder.sort(it) }

        // When there is no index, add an option to prevent raising an error.
        // - IndicesOptions.lenientExpandOpen()
        return SearchRequest(*indices.toTypedArray())
            .source(searchSourceBuilder)
            .indicesOptions(IndicesOptions.lenientExpandOpen())
    }

    private fun createQueryBuilder(searchPeriodToAt: LocalDateTime): QueryBuilder {
        val query = QueryBuilders.boolQuery()
        if(!from.isNullOrEmpty()) {
            query.filter(QueryBuilders.termsQuery(TransactionFieldType.FROM.fieldName, from.map { it.lowercase() }))
        }
        if(!to.isNullOrEmpty()) {
            query.filter(QueryBuilders.termsQuery(TransactionFieldType.TO.fieldName, to.map { it.lowercase() }))
        }
        if(!feePayer.isNullOrEmpty()) {
            query.filter(QueryBuilders.termsQuery(TransactionFieldType.FEE_PAYER.fieldName, feePayer.map { it.lowercase() }))
        }
        status?.let { query.filter(QueryBuilders.termQuery(TransactionFieldType.STATUS.fieldName, it)) }
        if(!types.isNullOrEmpty()) {
            query.filter(QueryBuilders.termsQuery(TransactionFieldType.TYPE.fieldName, types.map { it.type }))
        }
        blockHash?.let { query.filter(QueryBuilders.termQuery(TransactionFieldType.BLOCK_HASH.fieldName, it)) }

        if(blockNumberStart != null) {
            val blockNumberQuery =
                QueryBuilders.rangeQuery(TransactionFieldType.BLOCK_NUMBER.fieldName).gte(blockNumberStart)
            if(blockNumberEnd != null) {
                blockNumberQuery.lte(blockNumberEnd)
            }
            query.filter(blockNumberQuery)
        }

        query.filter(
            RangeQueryBuilder(TransactionFieldType.TIMESTAMP.fieldName)
                .gte(DateUtils.localDateTimeToTimestamp(fromAt!!))
                .lte(DateUtils.localDateTimeToTimestamp(searchPeriodToAt))
        )
        return query
    }
}