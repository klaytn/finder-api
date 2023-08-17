package io.klaytn.finder.domain.common

enum class MyContractType(val contractTypes: Set<ContractType>) {
    TOKEN(
            setOf(
                    ContractType.ERC20,
                    ContractType.KIP7,
                    ContractType.KIP17,
                    ContractType.KIP37,
                    ContractType.ERC721,
                    ContractType.ERC1155
            )
    ),
    CONTRACT(setOf(ContractType.CUSTOM))
}
