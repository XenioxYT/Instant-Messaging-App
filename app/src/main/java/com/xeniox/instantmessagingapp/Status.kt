package com.xeniox.instantmessagingapp

import android.os.Parcelable
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.parcel.Parcelize

private lateinit var firebaseAnalytics: FirebaseAnalytics

class Status(val lastSeen: Long) { // Create a class called User
    constructor() : this(-1)
//created blank constructor as kotlin now requires this when making a class
}