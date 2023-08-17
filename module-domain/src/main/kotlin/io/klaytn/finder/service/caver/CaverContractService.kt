package io.klaytn.finder.service.caver

import com.klaytn.caver.Caver
import com.klaytn.caver.kct.kip17.KIP17
import com.klaytn.caver.kct.kip37.KIP37
import com.klaytn.caver.kct.kip7.KIP7
import io.klaytn.commons.utils.logback.logger
import io.klaytn.finder.domain.common.ContractType
import io.klaytn.finder.infra.cache.CacheName
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class CaverContractService(
    private val caver: Caver,
) {
    private val logger = logger(this::class.java)

    fun kip7(contractAddress: String) = KIP7.create(caver, contractAddress)
    fun kip17(contractAddress: String) = KIP17.create(caver, contractAddress)
    fun kip37(contractAddress: String) = KIP37.create(caver, contractAddress)

    fun getContractCode(contractAddress: String): String =
        try {
            val codeResult = caver.rpc.klay.getCode(contractAddress).send()
            if(!codeResult.hasError()) {
                codeResult.result.toString()
            } else {
                ""
            }
        } catch (exception: Exception) {
            logger.warn(exception.message, exception)
            ""
        }

    fun getTotalSupply(contractType: ContractType, contractAddress: String): BigInteger? =
        try {
            when (contractType) {
                ContractType.KIP7, ContractType.ERC20 -> {
                    val kip7 = kip7(contractAddress)
                    kip7.totalSupply()
                }
                ContractType.KIP17, ContractType.ERC721 -> {
                    val kip17 = kip17(contractAddress)
                    kip17.totalSupply()
                }
                else -> {
                    null
                }
            }
        } catch (exception: Exception) {
            logger.warn(exception.message, exception)
            null
        }

    @Cacheable(cacheNames = [CacheName.CAVER_NFT_TOKEN_ITEM_TOTAL_SUPPLY],
        key = "{#contractAddress, #tokenId}",
        unless = "#result == null")
    fun getTotalSupplyOfTokenId(contractAddress: String, tokenId: String): BigInteger =
        try {
            val kip37 = kip37(contractAddress)
            kip37.totalSupply(BigInteger(tokenId))
        } catch (exception: Exception) {
            logger.warn(exception.message, exception)
            BigInteger.ZERO
        }

    fun getTokenUri(contractType: ContractType, contractAddress: String, tokenId: String): String? =
        try {
            when (contractType) {
                ContractType.KIP17, ContractType.ERC721 -> {
                    val kip17 = kip17(contractAddress)
                    kip17.tokenURI(BigInteger(tokenId))
                }
                ContractType.KIP37, ContractType.ERC1155 -> {
                    val kip37 = kip37(contractAddress)
                    kip37.uri(BigInteger(tokenId))
                }
                else -> {
                    null
                }
            }
        } catch (exception: Exception) {
            logger.warn(exception.message, exception)
            null
        }

    /**
     * Returns the balance for kip7(erc20) and kip17(erc721) types.
     */
    fun getBalance(contractType: ContractType, contractAddress: String, accountAddress: String): BigInteger? =
        try {
            when (contractType) {
                ContractType.KIP7, ContractType.ERC20 -> {
                    val kip7 = kip7(contractAddress)
                    kip7.balanceOf(accountAddress)
                }
                ContractType.KIP17, ContractType.ERC721 -> {
                    val kip17 = kip17(contractAddress)
                    kip17.balanceOf(accountAddress)
                }
                else -> {
                    null
                }
            }
        } catch (exception: Exception) {
            logger.warn(exception.message, exception)
            null
        }

    /**
     * Returns the balance for KIP37(ERC1155) type.
     */
    fun getBalanceOfTokenId(
        contractAddress: String, accountAddress: String, tokenId: String): BigInteger? =
        try {
            val kip37 = kip37(contractAddress)
            kip37.balanceOf(accountAddress, BigInteger(tokenId))
        } catch (exception: Exception) {
            logger.warn(exception.message, exception)
            null
        }
}