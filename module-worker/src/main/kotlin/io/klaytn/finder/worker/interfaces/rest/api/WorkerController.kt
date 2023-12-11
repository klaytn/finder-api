package io.klaytn.finder.interfaces.rest.api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class WorkerController() {
    @GetMapping("/")
    fun healthCheck() = "OK"
}
