package com.example.networkintelligence.domain.model

data class ChatMessage(
    val role: Role,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
)

enum class Role { USER, MODEL }
