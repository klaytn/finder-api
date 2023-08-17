package io.klaytn.finder.infra.db.shard.selector

import io.klaytn.finder.infra.db.shard.ShardNum
import io.klaytn.finder.infra.db.shard.ShardNumSelector

class BlockShardNumSelector(private val shardCount: Int) : ShardNumSelector<Long>(shardCount) {
    override fun select(shardKey: Long) =
        if(shardCount > 1) {
            val shardIndex = shardKey % shardCount
            ShardNum.of(shardIndex.toInt())
        } else {
            ShardNum.SHARD_0
        }
}