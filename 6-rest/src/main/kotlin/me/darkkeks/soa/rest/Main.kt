package me.darkkeks.soa.rest

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Repository
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

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

@Repository
interface UserRepository : CrudRepository<User, Long> {
    fun findByName(name: String): User?
    fun findByNameIn(names: List<String>): List<User>
}

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

@SpringBootApplication
class MafiaApplication

fun main(args: Array<String>) {
    runApplication<MafiaApplication>(*args)
}
