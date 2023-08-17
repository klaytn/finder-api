package io.klaytn.finder.service.caver

import com.klaytn.caver.Caver
import com.klaytn.caver.abi.ABI
import io.klaytn.commons.model.env.Phase
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.web3j.ens.NameHash
import java.math.BigInteger

class TestCaverKnsService {
    private val caver: Caver = TestCaverConstant.getCaver(Phase.prod, TestCaverChainType.CYPRESS)

    private val knsProperties =
        KnsProperties(true, "0x0892ed3424851d2Bab4aC1091fA93C9851Eb5d7D", "0x87f4483E4157a6592dd1d1546f145B5EE22c790a")
    private val caverKnsService = CaverKnsService(caver, knsProperties)

    @Test
    fun kns_name_hase_to_token_id() {
        val parameter =
            ABI.decodeParameter("bytes32", "0x56c2cb22420eed7e32f91c1672f8471b3aebfc063de5c92a8accdfdca15a74e3")
        Assertions.assertEquals(
            BigInteger("39243075286831253280172202751646557957107519437056817305371722443718540293347"),
            BigInteger(parameter.value as ByteArray)
        )
    }

    @Test
    fun kns_namahash() {
        val nameHash = NameHash.nameHash("jeyeon.klay")
        Assertions.assertEquals(nameHash, NameHash.nameHash("jeYeon.klay"))
        Assertions.assertEquals(nameHash, NameHash.nameHash("JeYeon.klay"))
        Assertions.assertEquals(nameHash, NameHash.nameHash("JEYEON.klay"))
    }

    @Test
    fun kns_get_address() {
        Assertions.assertEquals("0xe046c28b3219555c02c908ed4ca293bdb47101cc", caverKnsService.getAddress("jeyeon.klay"))
        Assertions.assertEquals("0xe046c28b3219555c02c908ed4ca293bdb47101cc", caverKnsService.getAddress("jeYeon.klay"))
        Assertions.assertEquals("0xe046c28b3219555c02c908ed4ca293bdb47101cc", caverKnsService.getAddress("JeYeon.klay"))
        Assertions.assertEquals("0xe046c28b3219555c02c908ed4ca293bdb47101cc", caverKnsService.getAddress("JEYEON.klay"))
    }

    @Test
    fun kns_reverse() {
        val name = caverKnsService.getName("0x0000ac03932ff48ee30209774e3f10fb0ac522e9")
        Assertions.assertEquals(name, "kns.klay")
    }
}