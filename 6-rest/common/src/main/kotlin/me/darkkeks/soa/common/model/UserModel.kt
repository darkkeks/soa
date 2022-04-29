package me.darkkeks.soa.common.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("users")
data class User(
    @Id
    val uid: Long = 0,
    val name: String,
    val avatar: String,
    val sex: Sex,
    val email: String,
)

enum class Sex {
    MALE,
    FEMALE,
    OTHER,
}

@Table("user_stats")
data class UserStats(
    @Id
    val uid: Long,
    val games: Long,
    val wins: Long,
    val loses: Long,
    val timeSpent: Long,
)

