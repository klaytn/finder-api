package io.klaytn.finder.infra.db.shard

abstract class ShardNumSelector<T>(private val shardCount: Int) {
    fun getShardType(shardKey: T) =
            if (shardCount == 0) {
                ShardNum.SHARD_0
            } else {
                select(shardKey)
            }

    abstract fun select(shardKey: T): ShardNum
}
