package io.klaytn.finder.interfaces.rest.api

import io.klaytn.finder.domain.common.ContractType
import io.klaytn.finder.infra.web.model.EtherscanLikeBlockRangeRequest
import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.klaytn.finder.service.EtherscanLikeTransactionService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid
import io.klaytn.finder.infra.security.auth.Auth
import io.klaytn.finder.infra.web.model.EtherscanLikeSimplePageRequest

enum class EtherscanModule {
    account
}

enum class EtherscanAction {
    txlist,
    tokentx,
    tokennfttx,
    token1155tx,
    txlistinternal
}


@RestController
@Tag(name = SwaggerConstant.TAG_PUBLIC)
@Auth(userRequired = false, requestLimit = false, requestLimitPerIp = true, requestLimitPerIpPerSecond = 10L)
class EtherscanLikeTransactionController(
    private val etherscanLikeTransactionService: EtherscanLikeTransactionService,
) {
    @GetMapping("/api")
    fun getEtherscanLikeApi(
        @RequestParam(name = "module", required = true) module: String,
        @RequestParam(name = "action", required = true) action: String,
        @RequestParam(name = "address", required = true) address: String,
        @RequestParam(name = "contractaddress", required = false) contractAddress: String? = null,
        @Valid etherscanLikeBlockRangeRequest: EtherscanLikeBlockRangeRequest? = null,
        @Valid etherscanLikeSimplePageRequest: EtherscanLikeSimplePageRequest,
        // {"status":"0","message":"NOTOK","result":"Max rate limit reached"}
    ): Map<String, Any> {
        val blockRange = etherscanLikeBlockRangeRequest?.toLongRange()
        fun etherscanLikeApiResultWrapper(status: String, message: String, result: List<Any>): Map<String, Any> {
            val response = mutableMapOf<String, Any>()
            response["status"] = "1"
            response["message"] = "OK"
            response["result"] = result
            return response
        }
        try {
            val result:List<Any>
            when (module) {
                EtherscanModule.account.name -> {
                    when (action) {
                        EtherscanAction.txlist.name -> {
                            result = etherscanLikeTransactionService.getTransactionsByAddress(
                                address,
                                blockRange,
                                etherscanLikeSimplePageRequest,
                            )
                        }

                        EtherscanAction.tokentx.name -> {
                            result= etherscanLikeTransactionService.getTokenTransactionsByAddress(
                                address,
                                contractAddress,
                                blockRange,
                                etherscanLikeSimplePageRequest
                            )
                        }

                        EtherscanAction.tokennfttx.name -> {
                            val nftTypes = arrayOf(ContractType.ERC721, ContractType.KIP17)
                            result = etherscanLikeTransactionService.getTokenNftTransactionsByAddress(
                                address,
                                nftTypes,
                                contractAddress,
                                blockRange,
                                etherscanLikeSimplePageRequest
                            )
                        }

                        EtherscanAction.token1155tx.name -> {
                            val nftTypes = arrayOf(ContractType.ERC1155, ContractType.KIP37)
                            result = etherscanLikeTransactionService.getTokenNftTransactionsByAddress(
                                address,
                                nftTypes,
                                contractAddress,
                                blockRange,
                                etherscanLikeSimplePageRequest
                            )
                        }

                        EtherscanAction.txlistinternal.name -> {
                            result = etherscanLikeTransactionService.getInternalTransactionsByAddress(
                                address,
                                blockRange,
                                etherscanLikeSimplePageRequest
                            )
                        }

                        else -> {
                            throw IllegalArgumentException("Invalid action: $action")
                        }
                    }
                    return etherscanLikeApiResultWrapper("1", "OK", result)

                }
                else -> {
                    throw IllegalArgumentException("Invalid module: $module")
                }
            }
        } catch (e: Exception) {
            return etherscanLikeApiResultWrapper("0", e.message ?: "Unknown error", listOf())
        }
    }
}
