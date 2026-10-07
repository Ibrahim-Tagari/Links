package com.example.prog_poe_final.models

data class User (
    val user_id: Int? = null,
    val name: String,
    val email: String,
    val password: String? = null,
    val created_at: String? = null,
    var bio: String? = null
)