package io.klaytn.finder.worker.interfaces.rabbitmq.solidity

data class SolidityCompilerUploadRequest(
        val osPath: String,
        val buildPath: String,
)
