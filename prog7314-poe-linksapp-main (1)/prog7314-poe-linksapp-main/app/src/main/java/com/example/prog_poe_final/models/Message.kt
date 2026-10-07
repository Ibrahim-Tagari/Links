package com.example.prog_poe_final.models

data class Message(
    val message_id: Int,
    val chat_id: Int,
    val sender_id: Int,
    val sender_name: String? = null,
    val message_text: String,
    val created_at: String? = null,
)
