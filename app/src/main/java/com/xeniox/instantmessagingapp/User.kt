package com.xeniox.instantmessagingapp

import android.os.Parcelable
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.parcel.Parcelize

private lateinit var firebaseAnalytics: FirebaseAnalytics
@Parcelize
class User(
    val uid: String,
    val username: String,
    val profileImageUrl: String,
    val email: String,
    val color: String,
    val bio: String,
    val typing: String,
) : Parcelable { // Create a class called User
    constructor() : this("", "", "","","","", "")
    //created blank constructor as kotlin now requires this when making a class
}