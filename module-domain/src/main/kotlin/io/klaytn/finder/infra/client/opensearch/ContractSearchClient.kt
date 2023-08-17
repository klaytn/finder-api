package io.klaytn.finder.infra.client.opensearch

import io.klaytn.commons.utils.logback.logger
import io.klaytn.finder.domain.mysql.set1.Contract
import io.klaytn.finder.infra.client.opensearch.model.ContractSearchRequest
import io.klaytn.finder.infra.utils.DateUtils
import io.klaytn.finder.infra.utils.PageUtils
import org.opensearch.action.update.UpdateRequest
import org.opensearch.client.RequestOptions
import org.opensearch.client.RestHighLevelClient
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl

class ContractSearchClient(
    private val restHighLevelClient: RestHighLevelClient,
    private val index: String,
    private val maxGram: Int,
){
    private val logger = logger(this::class.java)

    fun searchIds(contractSearchRequest: ContractSearchRequest): Page<String> {
        PageUtils.checkPageParameter(
            contractSearchRequest.contractSearchPageRequest.pageRequest(),
            OpenSearchTotalCountConstants.maxContractTotalCount
        )

        val searchResponse = restHighLevelClient.search(contractSearchRequest.toSearchRequest(index, maxGram), RequestOptions.DEFAULT)
        val hits = searchResponse.hits
        return PageImpl(
            hits.hits.map { it.id },
            contractSearchRequest.contractSearchPageRequest.pageRequest(),
            hits.totalHits!!.value
        )
    }

    fun updateContract(contract: Contract) {
        val updateMap = mapOf(
            ContractFieldType.CONTRACT_TYPE.fieldName to contract.contractType.value,
            ContractFieldType.NAME.fieldName to contract.name,
            ContractFieldType.SYMBOL.fieldName to contract.symbol,
            ContractFieldType.VERIFIED.fieldName to contract.verified,
            ContractFieldType.UPDATED_AT.fieldName to DateUtils.localDateTimeToEpochMilli(contract.updatedAt!!))

        val createMap = mapOf(
            ContractFieldType.ID.fieldName to contract.contractAddress,
            ContractFieldType.TOTAL_SUPPLY_ORDER.fieldName to contract.totalSupplyOrder,
            ContractFieldType.TOTAL_TRANSFER.fieldName to contract.totalTransfer,
            ContractFieldType.CREATED_AT.fieldName to DateUtils.localDateTimeToEpochMilli(contract.createdAt!!))

        try {
            val updateRequest = UpdateRequest(index, contract.contractAddress).doc(updateMap).upsert(createMap)
            restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT)
        } catch (exception: Exception) {
            logger.error("fail to update contract:${contract.contractAddress}", exception)
        }
    }
}

enum class ContractFieldType(val fieldName: String) {
    _ID("_id"),
    ID("contract_address"),
    UPDATED_AT("updated_at"),
    TOTAL_SUPPLY_ORDER("total_supply_order"),
    TOTAL_TRANSFER("total_transfer"),
    VERIFIED("verified"),
    CONTRACT_TYPE("contract_type"),
    NAME("name"),
    SYMBOL("symbol"),
    CREATED_AT("created_at"),
    ;
}

enum class ContractSearchSortType (val fieldType: ContractFieldType) {
    TIME(ContractFieldType.UPDATED_AT),
    TOTAL_SUPPLY(ContractFieldType.TOTAL_SUPPLY_ORDER),
    TOTAL_TRANSFER(ContractFieldType.TOTAL_TRANSFER)
}