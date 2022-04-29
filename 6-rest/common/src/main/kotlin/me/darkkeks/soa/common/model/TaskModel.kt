package me.darkkeks.soa.common.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

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
