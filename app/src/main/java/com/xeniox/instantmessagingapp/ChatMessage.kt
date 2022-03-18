package com.xeniox.instantmessagingapp

import com.google.firebase.analytics.FirebaseAnalytics

private lateinit var firebaseAnalytics: FirebaseAnalytics
class ChatMessage(val id: String, val text:String, val fromId: String, val toId: String, val timestamp: Long) {

    constructor() : this("", "", "", "", -1)
}