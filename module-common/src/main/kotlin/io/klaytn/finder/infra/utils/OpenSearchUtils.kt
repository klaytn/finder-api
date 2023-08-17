package io.klaytn.finder.infra.utils

import org.opensearch.index.query.BoolQueryBuilder
import org.opensearch.index.query.QueryBuilder
import org.opensearch.index.query.QueryBuilders

class OpenSearchUtils {
    companion object {
        fun boolQueryBuilderApplyNgramTokenizer(
                shouldFields: List<String>,
                value: String,
                maxGram: Int
        ): BoolQueryBuilder {
            val boolQuery = QueryBuilders.boolQuery()
            shouldFields.forEach {
                boolQuery.should(spanNearQueryApplyNgramTokenizer(it, value.lowercase(), maxGram))
            }
            return boolQuery
        }

        private fun spanNearQueryApplyNgramTokenizer(
                field: String,
                value: String,
                maxGram: Int
        ): QueryBuilder {
            if (value.length <= maxGram) {
                return QueryBuilders.spanNearQuery(
                        QueryBuilders.spanTermQuery(field, value),
                        value.length
                )
            }

            val spanOrQuery =
                    QueryBuilders.spanOrQuery(
                            QueryBuilders.spanTermQuery(field, value.substring(0, maxGram))
                    )
            for (i in maxGram + 1..value.length) {
                spanOrQuery.addClause(
                        QueryBuilders.spanTermQuery(field, value.substring(i - maxGram, i))
                )
            }
            return spanOrQuery
        }
    }
}
