package me.darkkeks.soa.rest

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController


enum class Sex {
    MALE,
    FEMALE,
    OTHER,
    ;

    companion object {
        fun ofString(value: String): Sex? {
            return values().firstOrNull { it.name == value.uppercase() }
        }
    }
}

@Table("users")
data class User(
    @Id
    val uid: Long,
    val name: String,
    val avatar: String,
    val sex: Sex,
    val email: String,
)

@Repository
interface UserRepository : CrudRepository<User, Long> {
    fun findByName(name: String): User?
}

@RestController("/user")
class UserController(
    private val userRepository: UserRepository,
) {
    @GetMapping("/{name}")
    fun getUser(@PathVariable name: String): User? {
        return userRepository.findByName(name)
    }
}

@SpringBootApplication
class MafiaApplication

fun main(args: Array<String>) {
    runApplication<MafiaApplication>(*args)
}
