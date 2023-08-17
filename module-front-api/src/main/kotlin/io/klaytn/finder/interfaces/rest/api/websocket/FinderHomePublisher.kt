package io.klaytn.finder.interfaces.rest.api.websocket

import io.klaytn.finder.infra.ServerMode
import io.klaytn.finder.service.FinderHomeService
import org.springframework.context.annotation.Profile
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.stereotype.Component

@Profile(ServerMode.API_MODE)
@Component
class FinderHomePublisher(
    private val finderHomeService: FinderHomeService,
    private val messagingTemplate: SimpMessageSendingOperations,
) {
    var lastBlockNo: Long = 0L

    fun sendBlock(blockNo: Long, timestamp: Int) {
        if (blockNo < lastBlockNo) {
            return
        }

        lastBlockNo = blockNo
        messagingTemplate.convertAndSend("/app/status", finderHomeService.getStatus(blockNo, timestamp))
        messagingTemplate.convertAndSend("/app/summary", finderHomeService.getSummary())
    }

    fun sendInternalTx(blockNo: Long, timestamp: Int) {
        // NO-OP
    }

    fun sendKlayPrice(klayPrice: Map<String, String>) {
        messagingTemplate.convertAndSend("/app/klay-price", finderHomeService.getKlayPrice(klayPrice))
    }
}
