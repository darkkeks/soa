package me.darkkeks.soa.graphql.server.service

import me.darkkeks.soa.common.GameCommentRepository
import me.darkkeks.soa.common.GamePlayersRepository
import me.darkkeks.soa.common.GameRepository
import me.darkkeks.soa.common.UserRepository
import me.darkkeks.soa.common.model.Game
import me.darkkeks.soa.common.model.GameComment
import me.darkkeks.soa.common.model.GamePlayer
import me.darkkeks.soa.common.model.User
import me.darkkeks.soa.graphql.server.model.GqAddComment
import me.darkkeks.soa.graphql.server.model.GqAddGame
import me.darkkeks.soa.graphql.server.model.GqGame
import me.darkkeks.soa.graphql.server.model.GqGameComment
import me.darkkeks.soa.graphql.server.model.GqGamePlayer
import me.darkkeks.soa.graphql.server.model.GqGameSelector
import me.darkkeks.soa.graphql.server.model.GqUpdateScore
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate

@Component
class GameService(
    private val userRepository: UserRepository,
    private val gameRepository: GameRepository,
    private val gamePlayersRepository: GamePlayersRepository,
    private val gameCommentRepository: GameCommentRepository,
    private val transactionTemplate: TransactionTemplate,
) {
    fun getGames(selector: GqGameSelector): List<GqGame> {
        val games = when {
            selector.ids != null -> gameRepository.findAllById(selector.ids)
            selector.status != null -> gameRepository.findGamesByStatus(selector.status)
            else -> gameRepository.findAll().toList()
        }

        return games.map { game ->
            GqGame(
                id = game.id,
                status = game.status,
            )
        }
    }

    fun getPlayers(game: GqGame): List<GqGamePlayer> {
        val gamePlayers: List<GamePlayer> = gamePlayersRepository.findGamePlayersByGameId(game.id)

        val userIds = gamePlayers.map { it.uid }.toSet()
        val users = userRepository.findAllById(userIds).associateBy { it.uid }

        return gamePlayers.map { player ->
            GqGamePlayer(
                name = users[player.uid]!!.name,
                role = player.role,
                score = player.score,
            )
        }
    }

    fun getComments(game: GqGame): List<GqGameComment> {
        val gameComments: List<GameComment> = gameCommentRepository.findAllByGameIdOrderByCreateTime(game.id)

        return gameComments.map { comment ->
            GqGameComment(
                author = comment.author,
                content = comment.content,
                createdAt = comment.createTime,
            )
        }
    }

    fun addGame(addGame: GqAddGame): Long {
        val names: List<String> = addGame.players.map { it.name }

        val distinctNames = names.distinct().count()

        if (distinctNames != names.size) {
            throw IllegalArgumentException("Names should be unique")
        }

        val users: Map<String, User> = userRepository.findByNameIn(names).associateBy { it.name }

        names.forEach { name ->
            if (name !in users) {
                throw IllegalArgumentException("No user with name $name")
            }
        }

        val game: Game? = transactionTemplate.execute {
            val game = gameRepository.save(
                Game(
                    status = addGame.status,
                )
            )

            val gamePlayers = addGame.players.map { player ->
                GamePlayer(
                    gameId = game.id,
                    uid = users[player.name]!!.uid,
                    role = player.role,
                    score = player.score,
                )
            }

            gamePlayersRepository.saveAll(gamePlayers)

            game
        }

        return game?.id
            ?: throw IllegalStateException("Failed to add game")
    }

    fun postComment(addComment: GqAddComment): Long {
        val game = gameRepository.findById(addComment.gameId).orElse(null)
            ?: throw IllegalArgumentException("Game does not exist")

        val comment = gameCommentRepository.save(
            GameComment(
                gameId = game.id,
                author = addComment.author,
                content = addComment.content,
            )
        )

        return comment.id
    }

    fun updateScore(updateScore: GqUpdateScore): GqGamePlayer {
        val user = userRepository.findByName(updateScore.name)
            ?: throw IllegalArgumentException("User does not exist")

        val game = gameRepository.findById(updateScore.gameId).orElse(null)
            ?: throw IllegalArgumentException("Game does not exist")

        val gamePlayer = gamePlayersRepository.findByGameIdAndUid(game.id, user.uid)
            ?: throw IllegalArgumentException("User ${user.name} does not participate in game #${game.id}")

        val updatedGamePlayer = gamePlayersRepository.save(
            gamePlayer.copy(
                score = updateScore.score,
            )
        )

        return GqGamePlayer(
            name = user.name,
            role = updatedGamePlayer.role,
            score = updateScore.score,
        )
    }
}
