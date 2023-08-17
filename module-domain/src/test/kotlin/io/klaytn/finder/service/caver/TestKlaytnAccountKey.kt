package io.klaytn.finder.service.caver

import com.fasterxml.jackson.databind.ObjectMapper
import io.klaytn.commons.model.env.Phase
import io.klaytn.finder.infra.db.DbTableConstants
import io.klaytn.finder.service.accountkey.*
import io.klaytn.finder.service.db.TestDbConstant
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.web3j.crypto.Keys

class TestKlaytnAccountKey {
    /**
     * for account_update tx type
     * - TxTypeAccountUpdate
     * - TxTypeFeeDelegatedAccountUpdate
     * - TxTypeFeeDelegatedAccountUpdateWithRatio
     */
    @Test
    fun getAccountUpdateFromCaver() {
        val objectMapper = ObjectMapper()
        val caver = TestCaverConstant.getCaver(Phase.prod, TestCaverChainType.CYPRESS)
        val caverAccountService = CaverAccountService(caver)
        val klaytnAccountKeyService = KlaytnAccountKeyService(caverAccountService, caver, objectMapper)

        val dataSource = TestDbConstant.getDatasource(
            Phase.prod, TestCaverChainType.CYPRESS, TestDbConstant.TestDbType.SET0101, true)

        val transactionHashes = mutableListOf<String>()
        dataSource.use {
            it.connection.use { conn ->
                conn.prepareStatement(
                    """
                    select
                        transaction_hash
                    from
                        ${DbTableConstants.transactions}
                    where
                        `type`='TxTypeAccountUpdate'
                    order by
                        block_number DESC, transaction_index DESC
                    """.trimIndent()).use { psmt ->
                    psmt.executeQuery().use { resultSet ->
                        while(resultSet.next()) {
                            transactionHashes.add(resultSet.getString(1))
                        }
                    }
                }
            }
        }

        val accountKeys = mutableListOf<KlaytnTransactionAccountKey>()
        transactionHashes.forEach{
            val tx = caver.rpc.klay.getTransactionByHash(it).send().result
            val key = caver.rpc.klay.decodeAccountKey(tx.key).send()
            val accountKey = key.result.accountKey

            val klaytnAccountKey = klaytnAccountKeyService.getKlaytnAccountKey(accountKey)
            val klaytnTransactionAccountKey = KlaytnTransactionAccountKey(
                tx.blockNumber.substring(2).toLong(16),
                tx.hash,
                tx.from,
                klaytnAccountKey)
            accountKeys.add(klaytnTransactionAccountKey)
        }

        accountKeys.forEach {
            println(objectMapper.writeValueAsString(it))
        }
    }

    @Test
    fun getAccountUpdateFromJson() {
        val objectMapper = ObjectMapper()
        val caver = TestCaverConstant.getCaver(Phase.prod, TestCaverChainType.CYPRESS)
        val caverAccountService = CaverAccountService(caver)
        val klaytnAccountKeyService = KlaytnAccountKeyService(caverAccountService, caver, objectMapper)

        val jsonString =
            """
            {"type":"AccountKeyRoleBased","roles":{"RoleTransaction":{"type":"AccountKeyWeightedMultiSig","threshold":2,"weightedPublicKeys":[{"weight":1,"publicKey":"0x520aec37b778cdb3778287918f9f4b211d35ab5cf075549c5a2f5022d7e86a5e20f765fca1676c2cdeb6fb7e17ea19835a8a93d050abc082d6850eb2d590d37e"},{"weight":1,"publicKey":"0x6f855ca5dc3e2225af17accf26bfe01255895b25ad04c8d8ee8165e7e3c54cd5cebd22ee7a4b6114c005ec9801c6374f2fbbc356139e04c17e6dd455f811256b"},{"weight":1,"publicKey":"0xfa7df07830336bcd4bf1e78674c91a3c93ceb5a464e9b3f89a6715828632ba0d925b3756c6cdf33529c2b7d73bdc14a4ad0a3933822ae9279f15e4a0619762f2"}]},"RoleAccountUpdate":{"type":"AccountKeyWeightedMultiSig","threshold":2,"weightedPublicKeys":[{"weight":1,"publicKey":"0x520aec37b778cdb3778287918f9f4b211d35ab5cf075549c5a2f5022d7e86a5e20f765fca1676c2cdeb6fb7e17ea19835a8a93d050abc082d6850eb2d590d37e"},{"weight":1,"publicKey":"0x6f855ca5dc3e2225af17accf26bfe01255895b25ad04c8d8ee8165e7e3c54cd5cebd22ee7a4b6114c005ec9801c6374f2fbbc356139e04c17e6dd455f811256b"},{"weight":1,"publicKey":"0xfa7df07830336bcd4bf1e78674c91a3c93ceb5a464e9b3f89a6715828632ba0d925b3756c6cdf33529c2b7d73bdc14a4ad0a3933822ae9279f15e4a0619762f2"}]},"RoleFeePayer":{"type":"AccountKeyPublic","publicKey":"0x04b56b106344d7c50c4a57c2a3e84d52db0f8fd0ecef22a2a004cffbc24902ffc3be0fed109c6f1d0127fc1a10e1d586f6a9b37659b064a4353bee8dc00f9ad9"}}}
            """.trimIndent()

        val jsonTree = objectMapper.readTree(jsonString)
        val klaytnAccountKey = klaytnAccountKeyService.getKlaytnAccountKey(jsonTree)
        Assertions.assertEquals(jsonString, objectMapper.writeValueAsString(klaytnAccountKey))
    }

    @Test
    fun testAccountKey() {
        val objectMapper = ObjectMapper()
        val caver = TestCaverConstant.getCaver(Phase.prod, TestCaverChainType.CYPRESS)
        val caverAccountService = CaverAccountService(caver)
        val klaytnAccountKeyService = KlaytnAccountKeyService(caverAccountService, caver, objectMapper)

        val klaytnAccountKey = klaytnAccountKeyService.getKlaytnAccountKeyByAccountAddress(
            "0xd8e79dcbb44bce1418ea1c2c2e7610d0de2dd10f")
        println(klaytnAccountKey)
    }

    @Test
    fun testAddressConverter() {
        val caver = TestCaverConstant.getCaver(Phase.prod, TestCaverChainType.CYPRESS)
        val publicKey = "0x3eec964dea53900a838ccb50927502d6fa966fb5259c08bcf7d0c14f80f73f86ca650c0c03cd90a648c966c2811bc85fd2a039c403d84c9fd8ab1577d0a3a102"
        val compressedPublicKey = "0x023eec964dea53900a838ccb50927502d6fa966fb5259c08bcf7d0c14f80f73f86"
        val address = "0x32fe911609c8ad93e5da70707f5f968590e213eb"

        Assertions.assertEquals(compressedPublicKey, caver.utils.compressPublicKey(publicKey))
        Assertions.assertEquals(address, caver.utils.publicKeyToAddress(publicKey))
        Assertions.assertEquals(address, caver.utils.publicKeyToAddress(compressedPublicKey))
    }

    @Test
    fun toChecksumAddress() {
        Assertions.assertEquals("0xAD569d3479f9D3B88E73022DC70Ea6e614E7863a",
            Keys.toChecksumAddress("0xad569d3479f9d3b88e73022dc70ea6e614e7863a"))
    }
}
