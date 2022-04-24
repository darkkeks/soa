package me.darkkeks.soa.rest

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : CrudRepository<User, Long> {
    fun findByName(name: String): User?
    fun findByNameIn(names: List<String>): List<User>
}

@Repository
interface StatsRepository : CrudRepository<Stats, Long>

@Repository
interface TaskQueueRepository : CrudRepository<Task, Long>
