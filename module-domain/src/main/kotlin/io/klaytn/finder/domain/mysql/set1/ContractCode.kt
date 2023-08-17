package io.klaytn.finder.domain.mysql.set1

import io.klaytn.finder.domain.mysql.BaseEntity
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Lob
import javax.persistence.Table

@Entity
@Table(name = "contract_codes")
data class ContractCode(
    @Column
    val contractAddress: String,

    @Column
    var contractName: String,

    @Column
    val compilerType: String = "Solidity",

    @Column
    var compilerVersion: String,

    @Column(columnDefinition = "TINYINT")
    var optimizationFlag: Boolean,

    @Column
    var optimizationRunsCount: Long,

    @Column
    var optimizationEvmVersion: String,

    @Lob
    var contractSourceCode: String,

    @Lob
    var contractAbi: String,

    @Column(columnDefinition = "TEXT")
    var contractCreationCode: String,

    @Column(length = 100)
    var licenseType: String,

    @Column(columnDefinition = "TEXT")
    var abiEncodedValue: String?,
) : BaseEntity() {
    companion object {
        fun of(contractAddress: String) =
            ContractCode(
                contractAddress = contractAddress,
                contractName = "",
                compilerVersion = "",
                optimizationFlag = false,
                optimizationRunsCount = 0,
                optimizationEvmVersion = "",
                contractSourceCode = "",
                contractAbi = "",
                contractCreationCode = "",
                licenseType = "",
                abiEncodedValue = null
            )
    }
}
