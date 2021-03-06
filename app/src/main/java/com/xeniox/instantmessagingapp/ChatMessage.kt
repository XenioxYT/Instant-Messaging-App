package com.xeniox.instantmessagingapp

class ChatMessage(
    val id: String,
    val text: String,
    val fromId: String,
    val toId: String,
    val timestamp: Long,
    val fromUsername: String,
) {
    constructor() : this("", "", "", "", -1, "")
}