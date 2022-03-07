package com.xeniox.instantmessagingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_user_profile.*

class UserProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        val username = intent.getParcelableExtra<User>(NewConversationActivity.USER_KEY)!!.username
        val email = intent.getParcelableExtra<User>(NewConversationActivity.USER_KEY)!!.email

        text_email_profile.text = email
        text_user_profile.text = username

    }
}