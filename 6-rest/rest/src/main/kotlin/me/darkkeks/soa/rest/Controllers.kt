package me.darkkeks.soa.rest

import me.darkkeks.soa.common.UserStatsRepository
import me.darkkeks.soa.common.model.Task
import me.darkkeks.soa.common.TaskQueueRepository
import me.darkkeks.soa.common.model.TaskStatus
import me.darkkeks.soa.common.model.User
import me.darkkeks.soa.common.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/user")
class UserController(
    private val userRepository: UserRepository,
) {

    @GetMapping
    fun getUsers(@RequestParam("name") name: List<String>): List<User> {
        return userRepository.findByNameIn(name.toList())
    }

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun postUser(@RequestBody addRequest: AddUserRequest): ResponseEntity<User> {
        if (userRepository.findByName(addRequest.name) != null) {
            return ResponseEntity(HttpStatus.CONFLICT)
        }

        val user: User = userRepository.save(
            User(
                name = addRequest.name,
                avatar = addRequest.avatar,
                sex = addRequest.sex,
                email = addRequest.email,
            )
        )

        return ResponseEntity.ok(user)
    }

    @PatchMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun patchUser(@RequestBody updateRequest: UpdateUserRequest): ResponseEntity<User> {
        val user = userRepository.findByName(updateRequest.name)
            ?: return ResponseEntity(HttpStatus.NOT_FOUND)

        val updatedUser = user.copy(
            avatar = updateRequest.avatar,
            sex = updateRequest.sex,
            email = updateRequest.email,
        )

        return ResponseEntity.ok(userRepository.save(updatedUser))
    }

    @DeleteMapping
    fun deleteUser(@RequestParam name: String): ResponseEntity<Void> {
        val user = userRepository.findByName(name)
            ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        userRepository.delete(user)
        return ResponseEntity(HttpStatus.OK)
    }
}

@RestController
@RequestMapping("/stats")
class StatsController(
    private val userRepository: UserRepository,
    private val userStatsRepository: UserStatsRepository,
    private val taskQueueRepository: TaskQueueRepository,
) {
    @GetMapping("/result")
    fun getResult(@RequestParam taskId: Long): ResponseEntity<String> {
        val task = taskQueueRepository.findById(taskId).orElse(null)
            ?: return ResponseEntity(HttpStatus.NOT_FOUND)

        if (task.status != TaskStatus.READY) {
            return ResponseEntity(HttpStatus.NO_CONTENT)
        }

        return ResponseEntity.ok(task.result)
    }

    @PostMapping("/request-stats")
    fun requestStats(@RequestParam name: String): ResponseEntity<Task> {
        val user = userRepository.findByName(name)
            ?: return ResponseEntity(HttpStatus.NOT_FOUND)

        val task = taskQueueRepository.save(
            Task(
                uid = user.uid,
                status = TaskStatus.NEW,
            )
        )

        return ResponseEntity.ok(task)
    }
}
