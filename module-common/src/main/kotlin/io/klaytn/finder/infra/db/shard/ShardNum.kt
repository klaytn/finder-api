package io.klaytn.finder.infra.db.shard

enum class ShardNum(val value: Int) {
    SHARD_0(0),
    SHARD_1(1),
    SHARD_2(2),
    SHARD_3(3),
    SHARD_4(4),
    SHARD_5(5),
    SHARD_6(6),
    SHARD_7(7),
    SHARD_8(8),
    SHARD_9(9),
    ;

    companion object {
        fun of(value: Int) = values().first { it.value == value }
    }
}
