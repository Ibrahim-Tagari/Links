package com.example.prog_poe_final.models

data class MessageRequest(
    val chat_id: Int,
    val sender_id: Int,
    val message_text: String
)
