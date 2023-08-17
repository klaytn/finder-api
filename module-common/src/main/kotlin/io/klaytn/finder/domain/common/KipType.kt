package io.klaytn.finder.domain.common

enum class KipType {
    KIP7 { // https://kips.klaytn.com/KIPs/kip-7
        override fun getEvents() =
                listOf(
                        KipEvent(
                                "Transfer",
                                "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef",
                                mapOf("from" to "address", "to" to "address"),
                                mapOf("value" to "uint256")
                        ),
                        KipEvent(
                                "Approval",
                                "0x8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925",
                                mapOf("owner" to "address", "spender" to "address"),
                                mapOf("value" to "uint256")
                        ),
                        KipEvent(
                                "Paused",
                                "0x62e78cea01bee320cd4e420270b5ea74000d11b0c9f74754ebdbfc544b05a258",
                                mapOf(),
                                mapOf("account" to "address")
                        ),
                        KipEvent(
                                "Unpaused",
                                "0x5db9ee0a495bf2e6ff9c91a7834c1ba4fdd244a5e8aa4e537bd38aeae4b073aa",
                                mapOf(),
                                mapOf("account" to "address")
                        )
                )
    },
    KIP17 { // https://kips.klaytn.com/KIPs/kip-17
        override fun getEvents() =
                listOf(
                        KipEvent(
                                "Transfer",
                                "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef",
                                mapOf(
                                        "from" to "address",
                                        "to" to "address",
                                        "tokenId" to "uint256"
                                ),
                                mapOf()
                        ),
                        KipEvent(
                                "Approval",
                                "0x8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925",
                                mapOf(
                                        "owner" to "address",
                                        "approved" to "address",
                                        "tokenId" to "uint256"
                                ),
                                mapOf()
                        ),
                        KipEvent(
                                "ApprovalForAll",
                                "0x17307eab39ab6107e8899845ad3d59bd9653f200f220920489ca2b5937696c31",
                                mapOf("owner" to "address", "operator" to "address"),
                                mapOf("approved" to "bool")
                        ),
                        KipEvent(
                                "Paused",
                                "0x62e78cea01bee320cd4e420270b5ea74000d11b0c9f74754ebdbfc544b05a258",
                                mapOf(),
                                mapOf("account" to "address")
                        ),
                        KipEvent(
                                "Unpaused",
                                "0x5db9ee0a495bf2e6ff9c91a7834c1ba4fdd244a5e8aa4e537bd38aeae4b073aa",
                                mapOf(),
                                mapOf("account" to "address")
                        )
                )
    },
    KIP37 { // https://kips.klaytn.com/KIPs/kip-37
        override fun getEvents() =
                listOf(
                        KipEvent(
                                "TransferSingle",
                                "0xc3d58168c5ae7397731d063d5bbf3d657854427343f4c083240f7aacaa2d0f62",
                                mapOf(
                                        "operator" to "address",
                                        "from" to "address",
                                        "to" to "address"
                                ),
                                mapOf("tokenId" to "uint256", "value" to "uint256")
                        ),
                        KipEvent(
                                "TransferBatch",
                                "0x4a39dc06d4c0dbc64b70af90fd698a233a518aa5d07e595d983b8c0526c8f7fb",
                                mapOf(
                                        "operator" to "address",
                                        "from" to "address",
                                        "to" to "address"
                                ),
                                mapOf("tokenIds" to "uint256[]", "values" to "uint256[]")
                        ),
                        KipEvent(
                                "ApprovalForAll",
                                "0x17307eab39ab6107e8899845ad3d59bd9653f200f220920489ca2b5937696c31",
                                mapOf("owner" to "address", "operator" to "address"),
                                mapOf("approved" to "bool")
                        ),
                        KipEvent(
                                "URI",
                                "0x6bb7ff708619ba0610cba295a58592e0451dee2622938c8755667688daf3529b",
                                mapOf("tokenId" to "uint256"),
                                mapOf("value" to "string")
                        )
                )
    },
    ETC {
        override fun getEvents() =
                listOf(
                        KipEvent(
                                "Upgraded",
                                "0xbc7cd75a20ee27fd9adebab32041f755214dbc6bffa90cc0225b39da2e5c2d3b",
                                mapOf("implementation" to "address"),
                                mapOf()
                        ),
                        KipEvent(
                                "OwnershipTransferred",
                                "0x8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e0",
                                mapOf("previousOwner" to "address", "newOwner" to "address"),
                                mapOf()
                        ),
                )
    },
    KNS {
        override fun getEvents() =
                listOf(
                        KipEvent(
                                "NewResolver",
                                "0x335721b01866dc23fbee8b6b2c7b1e14d6f05c28cd35a2c934239f94095602a0",
                                mapOf("node" to "bytes32"),
                                mapOf("resolver" to "address")
                        ),
                        //            // event AddrChanged(bytes32 indexed node, address addr);
                        //            KipEvent(
                        //                "AddrChanged",
                        //
                        // "0x52d7d861f09ab3d26239d492e8968629f95e9e318cf0b73bfddc441522a15fd2",
                        //                mapOf("node" to "bytes32"),
                        //                mapOf("addr" to "address")
                        //            ),
                        )
    },
    ;

    abstract fun getEvents(): List<KipEvent>

    object Events {
        private val events = values().map { it.getEvents() }.flatten().groupBy { it.signature }

        fun get() = events
    }
}

data class KipEvent(
        val name: String,
        val signature: String,
        val indexedParams: Map<String, String>, // name, solidityType
        val nonIndexedParams: Map<String, String> // name, solidityType
)
