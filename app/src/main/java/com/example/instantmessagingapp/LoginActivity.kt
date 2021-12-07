package com.example.instantmessagingapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.login_activity.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity) // Set the layout of the activity

        // Listen for the login button press
        button_login_loginActivity.setOnClickListener {
            Log.d("LoginActivity", "Login button pressed") // Log the button press
            // Check if username is empty
            if (username_editText_login.text.toString().isEmpty()) { // If username is empty
                username_editText_login.error = "Username cannot be empty" // Set error message
            }
            // Check if password is empty
            if (password_editText_login.text.toString().isEmpty()) { // If password is empty
                password_editText_login.error = "Password cannot be empty" // Set the error message
            }
            if (username_editText_login.text.toString().isNotEmpty() && password_editText_login.text.toString().isNotEmpty()) { // If username and password are not empty
                //TODO: Login the user once details have been validated
            }
        }

        // Listen for the back to sign in button press
        button_back_to_signin.setOnClickListener { // When the back to sign in button is pressed
            Log.d("LoginActivity", "Back to sign in button pressed") // Log the button press

            // Clear error messages
            username_editText_login.error = null // Clear the username error message
            password_editText_login.error = null // Clear the password error message

            //End the activity
            val intent = Intent(this, ConversationsActivity::class.java) // Create an intent to go to the ConversationsActivity
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) // Clear the task and start a new one
            startActivity(intent) // Start the ConversationsActivity
        }
    }
}