package io.klaytn.finder.worker.config

import io.ipfs.api.IPFS
import org.springframework.context.annotation.Configuration

@Configuration
class IpfsConfig {
    //    @Bean
    fun ipfs() = IPFS("ipfs.infura.io", 5001, "/api/v0/", true)
}
