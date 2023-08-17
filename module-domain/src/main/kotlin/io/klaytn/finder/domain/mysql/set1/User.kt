package io.klaytn.finder.domain.mysql.set1

import io.klaytn.finder.domain.mysql.BaseEntity
import javax.persistence.*

@Entity
@Table(name = "users")
data class User(
    @Column
    val userName: String,

    @Column(columnDefinition = "INT")
    val userType: UserType,

    @Column
    val accessKey: String,

    @Column
    val secretKey: String,
) : BaseEntity()

enum class UserType(val value: Int, val apiLimit: Long) {
    NONE(0, 0),
    ADMIN(255, Long.MAX_VALUE);

    companion object {
        fun of(value: Int) = values().first { it.value == value }
    }
}

@Converter(autoApply = true)
class UserTypeTypeAttributeConverter : AttributeConverter<UserType, Int> {
    override fun convertToDatabaseColumn(attribute: UserType?) = attribute?.value

    override fun convertToEntityAttribute(dbData: Int?): UserType? = dbData?.let { UserType.of(it) }
}
