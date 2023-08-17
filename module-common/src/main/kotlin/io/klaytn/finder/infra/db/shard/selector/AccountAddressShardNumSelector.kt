package io.klaytn.finder.infra.db.shard.selector

import io.klaytn.finder.infra.db.shard.ShardNum
import io.klaytn.finder.infra.db.shard.ShardNumSelector
import java.math.BigInteger

class AccountAddressShardNumSelector(private val shardCount: Int) : ShardNumSelector<String>(shardCount) {
    override fun select(shardKey: String) =
        if(shardCount > 1) {
            val value = BigInteger(shardKey.substring(2), 16)
            val shardIndex = value.mod(BigInteger.valueOf(shardCount.toLong()))
            ShardNum.of(shardIndex.toInt())
        } else {
            ShardNum.SHARD_0
        }
}