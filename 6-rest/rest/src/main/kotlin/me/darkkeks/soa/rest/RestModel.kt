package me.darkkeks.soa.rest

import me.darkkeks.soa.common.model.Sex

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
