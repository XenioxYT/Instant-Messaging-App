package com.xeniox.instantmessagingapp

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class User(
    val uid: String,
    val username: String,
    val profileImageUrl: String,
    val email: String,
    val color: String,
    val bio: String,
    val typing: String,
) : Parcelable {
    constructor() : this("", "", "","","","", "")
    //created blank constructor as kotlin now requires this when making a class
} // Create a class called User