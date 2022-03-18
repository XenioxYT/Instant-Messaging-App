package com.xeniox.instantmessagingapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

private lateinit var firebaseAnalytics: FirebaseAnalytics
class UserProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        // Obtain the FirebaseAnalytics instance.
        firebaseAnalytics = Firebase.analytics

//        val username = intent.getParcelableExtra<User>(ConversationsActivity.USER_KEY)!!.username
//        val email = intent.getParcelableExtra<User>(NewConversationActivity.USER_KEY)!!.email

//        text_email_profile.text = email
//        text_user_profile.text = username

    }
}