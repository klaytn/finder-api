package io.klaytn.finder.interfaces.rest.papi

import io.klaytn.finder.domain.mysql.set1.signature.EventSignature
import io.klaytn.finder.domain.mysql.set1.signature.FunctionSignature
import io.klaytn.finder.infra.ServerMode
import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.klaytn.finder.service.signature.EventSignatureService
import io.klaytn.finder.service.signature.FunctionSignatureService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.*

@Profile(ServerMode.PRIVATE_API_MODE)
@RestController
@Tag(name= SwaggerConstant.TAG_PRIVATE)
class PapiSignatureController(
    val functionSignatureService: FunctionSignatureService,
    val eventSignatureService: EventSignatureService
) {
    // -- --------------------------------------------------------------------------------------------------------------
    // -- function signature
    // -- --------------------------------------------------------------------------------------------------------------

    @GetMapping("/papi/v1/function-signatures/{byteSignature}")
    fun getFunctionSignatures(@PathVariable byteSignature: String) =
        functionSignatureService.getFunctionSignature(byteSignature)

    @PostMapping("/papi/v1/function-signatures")
    fun addFunctionSignature(
        @RequestParam(required = false)  fourByteId: Long?,
        @RequestParam  bytesSignature: String,
        @RequestParam  textSignature: String) =
        functionSignatureService.addFunctionSignature(
            FunctionSignature(
                fourByteId = fourByteId,
                bytesSignature = bytesSignature,
                textSignature = textSignature,
                primary = null)
        )

    @PutMapping("/papi/v1/function-signatures/{id}")
    fun updateFunctionSignature(
        @PathVariable  id: Long,
        @RequestParam  primary: Boolean) =
        functionSignatureService.updateFunctionSignaturePrimary(id, primary)

    // -- --------------------------------------------------------------------------------------------------------------
    // -- event signature
    // -- --------------------------------------------------------------------------------------------------------------

    @GetMapping("/papi/v1/event-signatures/{hexSignature}")
    fun getEventSignatures(@PathVariable hexSignature: String) =
        eventSignatureService.getEventSignature(hexSignature)

    @PostMapping("/papi/v1/event-signatures")
    fun addEventSignature(
        @RequestParam(required = false)  fourByteId: Long?,
        @RequestParam  hexSignature: String,
        @RequestParam  textSignature: String) =
        eventSignatureService.addEventSignature(
            EventSignature(
                fourByteId = fourByteId,
                hexSignature = hexSignature,
                textSignature = textSignature,
                primary = null)
        )

    @PutMapping("/papi/v1/event-signatures/{id}")
    fun updateEventSignature(
        @PathVariable  id: Long,
        @RequestParam  primary: Boolean) =
        eventSignatureService.updateEventSignaturePrimary(id, primary)
}
