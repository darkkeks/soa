package me.darkkeks.soa.common.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("games")
data class Game(
    @Id
    val id: Long = 0,
    val status: GameStatus,
)

enum class GameStatus {
    IN_PROGRESS,
    FINISHED,
}

@Table("game_players")
data class GamePlayer(
    @Id
    val id: Long = 0,
    val gameId: Long,
    val uid: Long,
    val role: GameRole,
    val score: Long,
)

enum class GameRole {
    MAFIA,
    CIVILIAN,
    DETECTIVE,
}

@Table("game_comments")
data class GameComment(
    @Id
    val id: Long = 0,
    val gameId: Long,
    val author: String,
    val content: String,
    val createTime: LocalDateTime = LocalDateTime.now(),
)
