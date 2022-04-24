package me.darkkeks.soa.rest

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

enum class Sex {
    MALE,
    FEMALE,
    OTHER,
}

@Table("users")
data class User(
    @Id
    val uid: Long = 0,
    val name: String,
    val avatar: String,
    val sex: Sex,
    val email: String,
)

@Table("stats")
data class Stats(
    @Id
    val uid: Long,
    val games: Long,
    val wins: Long,
    val loses: Long,
    val timeSpent: Long,
)

enum class TaskStatus {
    NEW,
    READY,
}

@Table("tasks")
data class Task(
    @Id
    val taskId: Long = 0,
    val uid: Long,
    val status: TaskStatus,
    val result: String? = null,
)

data class AddUserRequest(
    val name: String,
    val avatar: String,
    val sex: Sex,
    val email: String,
)

data class UpdateUserRequest(
    val name: String,
    val avatar: String,
    val sex: Sex,
    val email: String,
)
