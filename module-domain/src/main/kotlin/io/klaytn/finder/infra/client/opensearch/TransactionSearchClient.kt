package io.klaytn.finder.infra.client.opensearch

import io.klaytn.finder.infra.client.opensearch.model.TransactionSearchRequestByAccountAddress
import io.klaytn.finder.infra.client.opensearch.model.TransactionSearchPageRequest
import io.klaytn.finder.infra.client.opensearch.model.TransactionSearchRequest
import io.klaytn.finder.infra.utils.PageUtils
import org.opensearch.action.search.SearchRequest
import org.opensearch.client.RequestOptions
import org.opensearch.client.RestHighLevelClient
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl

class TransactionSearchClient(
    private val restHighLevelClient: RestHighLevelClient,
    private val index: String,
) {
    fun searchIds(
        transactionSearchRequest: TransactionSearchRequest,
        transactionSearchPageRequest: TransactionSearchPageRequest
    ): Page<String> {
        PageUtils.checkPageParameter(
            transactionSearchPageRequest.pageRequest(),
            OpenSearchTotalCountConstants.maxTransactionTotalCount
        )

        return search(
            transactionSearchRequest.toSearchRequest(index, transactionSearchPageRequest),
            transactionSearchPageRequest)
    }

    /**
     * for testing.
     */
    fun searchIds(
        accountAddress: String,
        transactionSearchRequestByAccountAddress: TransactionSearchRequestByAccountAddress,
        transactionSearchPageRequest: TransactionSearchPageRequest
    ): Page<String> {
        PageUtils.checkPageParameter(
            transactionSearchPageRequest.pageRequest(),
            OpenSearchTotalCountConstants.maxTransactionTotalCount
        )

        return search(
            transactionSearchRequestByAccountAddress.toSearchRequest(
                index, accountAddress, transactionSearchPageRequest),
            transactionSearchPageRequest)
    }

    private fun search(
        searchRequest: SearchRequest, transactionSearchPageRequest: TransactionSearchPageRequest
    ): Page<String> {
        val hits = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT).hits
        return PageImpl(
            hits.hits.map { it.id },
            transactionSearchPageRequest.pageRequest(),
            hits.totalHits!!.value
        )
    }
}

enum class TransactionFieldType(val fieldName: String) {
    ID("transaction_hash"),
    TYPE("type_int"),
    BLOCK_NUMBER("block_number"),
    BLOCK_HASH("block_hash"),
    FROM("from"),
    TO("to"),
    FEE_PAYER("fee_payer"),
    TRANSACTION_INDEX("transaction_index"),
    TIMESTAMP("timestamp"),
    STATUS("status")
    ;
}

enum class TransactionSortType(val field: TransactionFieldType) {
    TIME(TransactionFieldType.TIMESTAMP),
    BLOCK_NUMBER(TransactionFieldType.BLOCK_NUMBER)
}