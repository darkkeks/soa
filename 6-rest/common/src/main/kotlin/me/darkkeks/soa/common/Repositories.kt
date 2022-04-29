package me.darkkeks.soa.common

import me.darkkeks.soa.common.model.Game
import me.darkkeks.soa.common.model.GameComment
import me.darkkeks.soa.common.model.GamePlayer
import me.darkkeks.soa.common.model.GameStatus
import me.darkkeks.soa.common.model.Task
import me.darkkeks.soa.common.model.TaskStatus
import me.darkkeks.soa.common.model.User
import me.darkkeks.soa.common.model.UserStats
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : CrudRepository<User, Long> {
    fun findByName(name: String): User?
    fun findByNameIn(names: List<String>): List<User>
}

@Repository
interface UserStatsRepository : CrudRepository<UserStats, Long>

@Repository
interface GameRepository : CrudRepository<Game, Long> {
    fun findGamesByStatus(status: GameStatus): List<Game>
}

@Repository
interface GamePlayersRepository : CrudRepository<GamePlayer, Long> {
    fun findGamePlayersByGameId(gameId: Long): List<GamePlayer>
    fun findByGameIdAndUid(gameId: Long, uid: Long): GamePlayer?
}

@Repository
interface GameCommentRepository : CrudRepository<GameComment, Long> {
    fun findAllByGameIdOrderByCreateTime(gameId: Long): List<GameComment>
}

@Repository
interface TaskQueueRepository : CrudRepository<Task, Long> {
    fun findFirstByStatusOrderByTaskId(status: TaskStatus): Task?
}
