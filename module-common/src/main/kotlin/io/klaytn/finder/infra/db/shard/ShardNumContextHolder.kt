package io.klaytn.finder.infra.db.shard

class ShardNumContextHolder {
    companion object {
        private val threadLocal = ThreadLocal<ShardNum>()

        fun setDataSourceType(shardNum: ShardNum) {
            threadLocal.set(shardNum)
        }

        fun getDataSourceType(): ShardNum? = threadLocal.get()

        fun clear() {
            threadLocal.remove()
        }
    }
}
