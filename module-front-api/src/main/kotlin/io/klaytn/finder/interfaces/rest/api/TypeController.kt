package io.klaytn.finder.interfaces.rest.api

import io.klaytn.finder.infra.ServerMode
import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.klaytn.finder.domain.common.KipType
import io.klaytn.finder.view.model.transaction.TransactionTypeView
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Profile(ServerMode.API_MODE)
@RestController
@Tag(name= SwaggerConstant.TAG_PUBLIC)
class TypeController {
    private val transactionTypes = TransactionTypeView.toMap()

    @Operation(
        description = "Returns a list of Transaction types.",
    )
    @GetMapping(value = [
        "/api/v1/types/transaction",
        "/api/v1/transaction-types"
    ])
    fun getTransactionTypes() = transactionTypes

    @Operation(
        description = "Returns a list of Event-Log types.",
    )
    @GetMapping("/api/v1/types/event-logs")
    fun getEventLogTypes(): Map<String,String> {
        val types = mutableMapOf<String,String>()
        KipType.values().forEach { kipType ->
            kipType.getEvents().forEach { kipEvent ->
                types.putIfAbsent(kipEvent.name, kipEvent.signature)
            }
        }
        return types
    }
}
