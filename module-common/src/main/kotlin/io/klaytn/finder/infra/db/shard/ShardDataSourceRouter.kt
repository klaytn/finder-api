package io.klaytn.finder.infra.db.shard

import java.io.Closeable
import org.springframework.beans.factory.DisposableBean
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource

class ShardDataSourceRouter : AbstractRoutingDataSource(), DisposableBean {
    override fun determineCurrentLookupKey(): ShardNum =
            ShardNumContextHolder.getDataSourceType() ?: ShardNum.SHARD_0

    override fun destroy() {
        resolvedDataSources.values.forEach {
            if (it is Closeable) {
                it.close()
            }
        }
    }
}
