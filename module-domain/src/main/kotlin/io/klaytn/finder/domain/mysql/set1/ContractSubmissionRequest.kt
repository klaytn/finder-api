package io.klaytn.finder.domain.mysql.set1

import io.klaytn.finder.domain.common.WalletType
import io.klaytn.finder.domain.mysql.BaseEntity
import io.klaytn.finder.infra.db.jpa.JsonStringToStringListConverter
import javax.persistence.*

@Entity
@Table(name = "contract_submission_requests")
class ContractSubmissionRequest(
    @Column
    val contractAddress: String,                // for contract search
    @Column(columnDefinition = "TEXT")
    val contractCreatorSignature: String,       // for extract creator address

    @Lob
    val contractSourceCode: String,             // ContractCode#contractSourceCode
    @Column(columnDefinition = "TEXT")
    var constructorArguments: String?,          // ABI-encoded constructor arguments or Raw ABI Code(truncated)

    @Column
    val compilerVersion: String,                // ContractCode#compilerVersion
    @Column
    val licenseType: String,                    // ContractCode#licenseType
    @Column(columnDefinition = "TINYINT")
    val optimization: Boolean,                  // ContractCode#optimizationFlag
    @Column
    val optimizationRuns: Long?,                 // ContractCode#optimizationRunsCount
    @Column
    val evmVersion: String?,                    // ContractCode#optimizationEvmVersion

    @Column
    val tokenName: String?,                     // Contract#name
    @Column
    val tokenSymbol: String?,                   // Contract#symbol
    @Column(columnDefinition = "TEXT")
    val tokenIcon: String?,                     // Contract#icon

    @Column
    val officialWebSite: String?,               // Contract#officialSite
    @Column
    val officialEmailAddress: String?,          // Contract#officialEmailAddress

    @Column(columnDefinition = "TEXT")
    val contractCreationCode: String,          // Contact code retrieved through caver.

    @Column(columnDefinition = "TINYINT")
    var result: Boolean = false,

    @Column(columnDefinition = "TEXT")
    var resultMessage: String? = null,

    @Column(columnDefinition = "TINYINT")
    val walletType: WalletType?,

    @Column(columnDefinition = "TEXT")
    @Convert(converter = JsonStringToStringListConverter::class)
    var libraries: List<String>? = null,
) : BaseEntity()