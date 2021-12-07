package com.example.instantmessagingapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.login_activity.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        // Listen for the login button press
        button_login_loginActivity.setOnClickListener {
            Log.d("LoginActivity", "Login button pressed")
            // Check if username is empty
            if (username_editText_login.text.toString().isEmpty()) {
                username_editText_login.error = "Username cannot be empty"
            }
            // Check if password is empty
            if (password_editText_login.text.toString().isEmpty()) {
                password_editText_login.error = "Password cannot be empty"
            }
            if (username_editText_login.text.toString().isNotEmpty() && password_editText_login.text.toString().isNotEmpty()) {
                //TODO: Login the user once details have been validated
            }
        }

        // Listen for the back to sign in button press
        button_back_to_signin.setOnClickListener {
            Log.d("LoginActivity", "Back to sign in button pressed")

            // Clear error messages
            username_editText_login.error = null
            password_editText_login.error = null

            //End the activity
            val intent = Intent(this, ConversationsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}