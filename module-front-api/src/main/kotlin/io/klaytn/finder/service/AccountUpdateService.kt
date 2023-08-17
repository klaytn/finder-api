package io.klaytn.finder.service

import io.klaytn.commons.utils.logback.logger
import io.klaytn.finder.infra.client.opensearch.AccountSearchClient
import io.klaytn.finder.infra.exception.InvalidRequestException
import io.klaytn.finder.service.caver.CaverAccountService
import org.springframework.stereotype.Service

@Service
class AccountUpdateService(
    private val accountService: AccountService,
    private val caverAccountService: CaverAccountService,
    private val accountSearchClient: AccountSearchClient
) {
    private val logger = logger(this::class.java)

    fun updateAddressLabel(accountAddress: String, addressLabel: String?) {
        if(caverAccountService.isAccountAddress(accountAddress)) {
            accountService.updateAddressLabel(accountAddress, addressLabel).also { account ->
                if(account != null) {
                    accountSearchClient.updateAddressLabel(account, addressLabel)
                } else {
                    throw InvalidRequestException("$accountAddress does not exists!. so, can't update").also { ex ->
                        logger.warn(ex.message)
                    }
                }
            }
        } else {
            throw InvalidRequestException("$accountAddress is invalid address")
        }
    }

    fun updateKnsDomain(accountAddress: String, knsDomain: String?) {
        if(caverAccountService.isAccountAddress(accountAddress)) {
            accountService.updateKnsDomain(accountAddress, knsDomain).also { account ->
                if(account != null) {
                    accountSearchClient.updateKnsDomain(account, knsDomain)
                } else {
                    throw InvalidRequestException("$accountAddress does not exists!. so, can't update").also { ex ->
                        logger.warn(ex.message)
                    }
                }
            }
        } else {
            throw InvalidRequestException("$accountAddress is invalid address")
        }
    }

    fun updateTags(accountAddress: String, tags: List<String>) {
        if(caverAccountService.isAccountAddress(accountAddress)) {
            accountService.updateTags(accountAddress, tags).also { account ->
                if(account != null) {
                    accountSearchClient.updateTags(account, tags)
                } else {
                    throw InvalidRequestException("$accountAddress does not exists!. so, can't update").also { ex ->
                        logger.warn(ex.message)
                    }
                }
            }
        } else {
            throw InvalidRequestException("$accountAddress is invalid address")
        }
    }

    fun addTags(accountAddress: String, newTags: List<String>) {
        if(caverAccountService.isAccountAddress(accountAddress)) {
            accountService.addTags(accountAddress, newTags).also { account ->
                if(account != null) {
                    accountSearchClient.updateTags(account, account.tags ?: emptyList())
                } else {
                    throw InvalidRequestException("$accountAddress does not exists!. so, can't update").also { ex ->
                        logger.warn(ex.message)
                    }
                }
            }
        } else {
            throw InvalidRequestException("$accountAddress is invalid address")
        }
    }

    fun removeTags(accountAddress: String, removeTags: List<String>) {
        if(caverAccountService.isAccountAddress(accountAddress)) {
            accountService.removeTags(accountAddress, removeTags).also { account ->
                if(account != null) {
                    accountSearchClient.updateTags(account, account.tags ?: emptyList())
                } else {
                    throw InvalidRequestException("$accountAddress does not exists!. so, can't update").also { ex ->
                        logger.warn(ex.message)
                    }
                }
            }
        } else {
            throw InvalidRequestException("$accountAddress is invalid address")
        }
    }
}
