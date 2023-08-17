package io.klaytn.finder.service

import io.klaytn.commons.curator.CuratorTemplate
import io.klaytn.commons.model.env.Phase
import io.klaytn.finder.service.caver.TestCaverChainType
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.ExponentialBackoffRetry
import org.junit.jupiter.api.Test

class SettingManagerOperatorTest {
    private val zookeeperHostMap = mapOf(
        Phase.local to "ZOOKEEPER_ENDPOINT",
        Phase.stag to "ZOOKEEPER_ENDPOINT",
        Phase.prod to "ZOOKEEPER_ENDPOINT"
    )

    private val rootPath = "/klaytn/klaytnfinder"
    private val phase = Phase.local

    companion object {
        private val value_true = getValue(true)
        private val value_false = getValue(false)
        private val value_maintenance_reason = getValue(""""Sorry, we're down for scheduled maintenance."""")

        fun getValue(value: String) = value.toByteArray()
        fun getValue(value: Long) = getValue(value.toString())
        fun getValue(value: Boolean) = getValue(value.toString())
    }

    @Test
    fun all() {
        api()
        openapi()
    }

    @Test
    fun api() {
        val retryPolicy = ExponentialBackoffRetry(1000, 3)
        val client = CuratorFrameworkFactory.newClient(zookeeperHostMap[phase], retryPolicy)

        val project = "finder-api"
        client.use { curatorFramework ->
            curatorFramework.start()
            val curatorTemplate = CuratorTemplate(curatorFramework, "$rootPath/$phase")
            TestCaverChainType.values().map { chainType -> chainType.name.lowercase() }.forEach { chain ->
                val clientFeaturePath = "$project/$chain/client/features"
                val serverPath = "$project/$chain/server"

                // client
                curatorTemplate.createOrUpdate("$clientFeaturePath/contractCode", value_true)
                curatorTemplate.createOrUpdate("$clientFeaturePath/downloadProposedBlockList", value_true)
                curatorTemplate.createOrUpdate("$clientFeaturePath/blockRewards", value_true)
                curatorTemplate.createOrUpdate("$clientFeaturePath/showBtcPrice", value_true)
                curatorTemplate.createOrUpdate("$clientFeaturePath/showGasPrice", value_true)
                curatorTemplate.createOrUpdate("$clientFeaturePath/showTokenDescription", value_true)
                curatorTemplate.createOrUpdate("$clientFeaturePath/tokenSearchFilter", value_true)
                curatorTemplate.createOrUpdate("$clientFeaturePath/accountSearch", value_true)
                curatorTemplate.createOrUpdate("$clientFeaturePath/transactionFilter", value_true)
                curatorTemplate.createOrUpdate("$clientFeaturePath/baseFee", value_true)
                curatorTemplate.createOrUpdate("$clientFeaturePath/walletConnect", value_true)
                curatorTemplate.createOrUpdate("$clientFeaturePath/myPage", value_true)
                curatorTemplate.createOrUpdate("$clientFeaturePath/showBlockBurnt", value_true)
                curatorTemplate.createOrUpdate("$clientFeaturePath/accountKey", value_true)
                curatorTemplate.createOrUpdate("$clientFeaturePath/blockRewardDetail", value_true)

                // server
                curatorTemplate.createOrUpdate("$serverPath/nft/refresh/tokenUriRefreshLockTime", getValue(60))

                // -- features
                val governanceCouncil = chain.equals("cypress", true)
                curatorTemplate.createOrUpdate("$serverPath/features/governanceCouncil", getValue(governanceCouncil))
                curatorTemplate.createOrUpdate("$serverPath/features/estimatedEventLog", getValue(true))
                curatorTemplate.createOrUpdate("$serverPath/features/contractSubmissionConstructorArguments", getValue(true))
                curatorTemplate.createOrUpdate("$serverPath/features/accountTransferContractWithJoin", getValue(true))

                curatorTemplate.createOrUpdate("$serverPath/maintenance/reason", value_maintenance_reason)
                curatorTemplate.createOrUpdate("$serverPath/maintenance/status", value_false)

                curatorTemplate.createOrUpdate("$serverPath/paging/blockRangeActive/block", value_false)
                curatorTemplate.createOrUpdate("$serverPath/paging/blockRangeActive/transaction", value_false)
                curatorTemplate.createOrUpdate("$serverPath/paging/blockRangeActive/internalTransaction", value_false)
                curatorTemplate.createOrUpdate("$serverPath/paging/blockRangeActive/nftTransfer", value_false)
                curatorTemplate.createOrUpdate("$serverPath/paging/blockRangeActive/tokenTransfer", value_false)
                curatorTemplate.createOrUpdate("$serverPath/paging/blockRangeActive/tokenBurn", value_false)
                curatorTemplate.createOrUpdate("$serverPath/paging/blockRangeActive/eventLog", value_false)

                curatorTemplate.createOrUpdate("$serverPath/paging/interval/block", getValue(40_000))
                curatorTemplate.createOrUpdate("$serverPath/paging/interval/transaction", getValue(1_000L))
                curatorTemplate.createOrUpdate("$serverPath/paging/interval/internalTransaction", getValue(1_000L))
                curatorTemplate.createOrUpdate("$serverPath/paging/interval/nftTransfer", getValue(1_000L))
                curatorTemplate.createOrUpdate("$serverPath/paging/interval/tokenTransfer", getValue(1_000L))
                curatorTemplate.createOrUpdate("$serverPath/paging/interval/tokenBurn", getValue(1_000L))
                curatorTemplate.createOrUpdate("$serverPath/paging/interval/eventLog", getValue(1_000L))

                curatorTemplate.createOrUpdate("$serverPath/paging/limit/default", getValue(40_000))
                curatorTemplate.createOrUpdate("$serverPath/paging/limit/block", getValue(40_000))
                curatorTemplate.createOrUpdate("$serverPath/paging/limit/transaction", getValue(40_000))
                curatorTemplate.createOrUpdate("$serverPath/paging/limit/internalTransaction", getValue(40_000))
                curatorTemplate.createOrUpdate("$serverPath/paging/limit/eventLog", getValue(40_000))
                curatorTemplate.createOrUpdate("$serverPath/paging/limit/tokenHolder", getValue(100_000))
                curatorTemplate.createOrUpdate("$serverPath/paging/limit/nftInventory", getValue(100_000))
                curatorTemplate.createOrUpdate("$serverPath/paging/limit/nft17Holder", getValue(100_000))
                curatorTemplate.createOrUpdate("$serverPath/paging/limit/nft37Holder", getValue(100_000))
            }
        }
    }

    @Test
    fun openapi() {
        val retryPolicy = ExponentialBackoffRetry(1000, 3)
        val client = CuratorFrameworkFactory.newClient(zookeeperHostMap[phase], retryPolicy);

        val project = "finder-oapi"
        client.use {
            it.start()
            val curatorTemplate = CuratorTemplate(it, "$rootPath/$phase")
            TestCaverChainType.values().map { chainType -> chainType.name.lowercase() }.forEach { chain ->
                val serverPath = "$project/$chain/server"

                // server
                curatorTemplate.createOrUpdate("$serverPath/nft/refresh/tokenUriRefreshLockTime", getValue(60))

                // -- features
                curatorTemplate.createOrUpdate("$serverPath/features/governanceCouncil", getValue(false))
                curatorTemplate.createOrUpdate("$serverPath/features/estimatedEventLog", getValue(true))
                curatorTemplate.createOrUpdate("$serverPath/features/contractSubmissionConstructorArguments", getValue(true))
                curatorTemplate.createOrUpdate("$serverPath/features/accountTransferContractWithJoin", getValue(true))

                curatorTemplate.createOrUpdate("$serverPath/maintenance/reason", value_maintenance_reason)
                curatorTemplate.createOrUpdate("$serverPath/maintenance/status", value_false)

                curatorTemplate.createOrUpdate("$serverPath/paging/blockRangeActive/block", value_false)
                curatorTemplate.createOrUpdate("$serverPath/paging/blockRangeActive/transaction", value_false)
                curatorTemplate.createOrUpdate("$serverPath/paging/blockRangeActive/internalTransaction", value_false)
                curatorTemplate.createOrUpdate("$serverPath/paging/blockRangeActive/nftTransfer", value_false)
                curatorTemplate.createOrUpdate("$serverPath/paging/blockRangeActive/tokenTransfer", value_false)
                curatorTemplate.createOrUpdate("$serverPath/paging/blockRangeActive/tokenBurn", value_false)
                curatorTemplate.createOrUpdate("$serverPath/paging/blockRangeActive/eventLog", value_false)

                curatorTemplate.createOrUpdate("$serverPath/paging/interval/block", getValue(40_000))
                curatorTemplate.createOrUpdate("$serverPath/paging/interval/transaction", getValue(1_000L))
                curatorTemplate.createOrUpdate("$serverPath/paging/interval/internalTransaction", getValue(1_000L))
                curatorTemplate.createOrUpdate("$serverPath/paging/interval/nftTransfer", getValue(1_000L))
                curatorTemplate.createOrUpdate("$serverPath/paging/interval/tokenTransfer", getValue(1_000L))
                curatorTemplate.createOrUpdate("$serverPath/paging/interval/tokenBurn", getValue(1_000L))
                curatorTemplate.createOrUpdate("$serverPath/paging/interval/eventLog", getValue(1_000L))

                curatorTemplate.createOrUpdate("$serverPath/paging/limit/default", getValue(40_000))
                curatorTemplate.createOrUpdate("$serverPath/paging/limit/block", getValue(40_000))
                curatorTemplate.createOrUpdate("$serverPath/paging/limit/transaction", getValue(40_000))
                curatorTemplate.createOrUpdate("$serverPath/paging/limit/internalTransaction", getValue(40_000))
                curatorTemplate.createOrUpdate("$serverPath/paging/limit/eventLog", getValue(40_000))
                curatorTemplate.createOrUpdate("$serverPath/paging/limit/tokenHolder", getValue(100_000))
                curatorTemplate.createOrUpdate("$serverPath/paging/limit/nftInventory", getValue(100_000))
                curatorTemplate.createOrUpdate("$serverPath/paging/limit/nft17Holder", getValue(100_000))
                curatorTemplate.createOrUpdate("$serverPath/paging/limit/nft37Holder", getValue(100_000))
            }
        }
    }

    @Test
    fun worker() {
        val retryPolicy = ExponentialBackoffRetry(1000, 3)
        val client = CuratorFrameworkFactory.newClient(zookeeperHostMap[phase], retryPolicy);

        val project = "finder-worker"
        client.use {
            it.start()
            val curatorTemplate = CuratorTemplate(it, "$rootPath/$phase")
            curatorTemplate.createOrUpdate("$project/sample/test", getValue(40_000))
        }
    }
}