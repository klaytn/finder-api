package io.klaytn.finder.infra.client.opensearch

import io.klaytn.commons.utils.logback.logger
import io.klaytn.finder.domain.mysql.set1.Account
import io.klaytn.finder.infra.utils.DateUtils
import io.klaytn.finder.infra.utils.OpenSearchUtils
import io.klaytn.finder.infra.utils.PageUtils
import io.klaytn.finder.infra.web.model.SimplePageRequest
import org.opensearch.action.search.SearchRequest
import org.opensearch.action.update.UpdateRequest
import org.opensearch.client.RequestOptions
import org.opensearch.client.RestHighLevelClient
import org.opensearch.index.query.QueryBuilder
import org.opensearch.index.query.QueryBuilders
import org.opensearch.search.builder.SearchSourceBuilder
import org.opensearch.search.sort.FieldSortBuilder
import org.opensearch.search.sort.SortOrder
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl

class AccountSearchClient(
    private val restHighLevelClient: RestHighLevelClient,
    private val index: String,
    private val maxGram: Int,
){
    private val logger = logger(this::class.java)

    fun updateAddressLabel(account: Account, addressLabel: String?) {
        val accountAddress = account.address
        try {
            val updateRequest = UpdateRequest(index, accountAddress)
            updateRequest.upsert(getAccountCreateSource(account))
            updateRequest.doc(AccountFieldType.ADDRESS_LABEL.fieldName, addressLabel)
            restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT)
        } catch (exception: Exception) {
            logger.error("fail to update account-label:${accountAddress}", exception)
        }
    }

    fun updateKnsDomain(account: Account, knsDomain: String?) {
        val accountAddress = account.address
        try {
            val updateRequest = UpdateRequest(index, accountAddress)
            updateRequest.upsert(getAccountCreateSource(account))
            updateRequest.doc(AccountFieldType.KNS_DOMAIN.fieldName, knsDomain)
            restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT)
        } catch (exception: Exception) {
            logger.error("fail to update account-label:${accountAddress}", exception)
        }
    }

    fun updateTags(account: Account, tags: List<String>) {
        val accountAddress = account.address
        try {
            val updateRequest = UpdateRequest(index, accountAddress)
            updateRequest.upsert(getAccountCreateSource(account))
            updateRequest.doc(AccountFieldType.TAGS.fieldName, tags)
            restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT)
        } catch (exception: Exception) {
            logger.error("fail to update account-tags:${accountAddress}", exception)
        }
    }

    fun searchIds(accountSearchType: AccountSearchType, keyword: String, simplePageRequest: SimplePageRequest): Page<String> {
        PageUtils.checkPageParameter(simplePageRequest, OpenSearchTotalCountConstants.maxAccountTotalCount)

        val keywordQuery =
            OpenSearchUtils.boolQueryBuilderApplyNgramTokenizer(
                listOf(AccountFieldType.ADDRESS_LABEL.fieldName, AccountFieldType.KNS_DOMAIN.fieldName),
                keyword, maxGram)
        val tagQuery = QueryBuilders.termsQuery(AccountFieldType.TAGS.fieldName, keyword)

        val queryBuilder = QueryBuilders.boolQuery()
        when (accountSearchType) {
            AccountSearchType.ALL -> {
                queryBuilder.filter(QueryBuilders.boolQuery().should(keywordQuery).should(tagQuery))
            }
            AccountSearchType.TAG -> {
                queryBuilder.filter(tagQuery)
            }
            else -> {
                queryBuilder.filter(keywordQuery)
            }
        }
        return getIds(queryBuilder, simplePageRequest)
    }

    private fun getIds(queryBuilder: QueryBuilder, simplePageRequest: SimplePageRequest): Page<String> {
        val searchRequest = SearchRequest(index).source(
            SearchSourceBuilder()
                .fetchSource(false)
                .query(queryBuilder)
                .from(simplePageRequest.offset())
                .size(simplePageRequest.size)
                .sort(FieldSortBuilder(AccountFieldType.UPDATED_AT.fieldName).order(SortOrder.DESC))
        )

        val hits = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT).hits
        return PageImpl(
            hits.hits.map { it.id },
            simplePageRequest.pageRequest(),
            hits.totalHits!!.value
        )
    }

    private fun getAccountCreateSource(account: Account) =
        mapOf(
            AccountFieldType.ID.fieldName to account.address,
            AccountFieldType.TYPE.fieldName to account.accountType.value,
            AccountFieldType.CONTRACT_TYPE.fieldName to account.contractType.value,
            AccountFieldType.CONTRACT_CREATOR_ADDRESS.fieldName to account.contractCreatorAddress,
            AccountFieldType.CONTRACT_CREATOR_TX_HASH.fieldName to account.contractCreatorTransactionHash,
            AccountFieldType.ADDRESS_LABEL.fieldName to account.addressLabel,
            AccountFieldType.KNS_DOMAIN.fieldName to account.knsDomain,
            AccountFieldType.TAGS.fieldName to account.tags,
            AccountFieldType.UPDATED_AT.fieldName to DateUtils.localDateTimeToEpochMilli(account.updatedAt!!)
        )
}

enum class AccountSearchType {
    ALL,
    TAG,
    KEYWORD,
}

enum class AccountFieldType(val fieldName: String) {
    ID("address"),
    CONTRACT_TYPE("contract_type"),
    CONTRACT_CREATOR_TX_HASH("contract_creator_tx_hash"),
    CONTRACT_CREATOR_ADDRESS("contract_creator_address"),
    TYPE("type"),
    TAGS("tags"),
    ADDRESS_LABEL("address_label"),
    KNS_DOMAIN("kns_domain"),
    UPDATED_AT("updated_at")
    ;
}