package com.example.instantmessagingapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.login_activity.*

class LoginAccountCreatedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_account_created)

        // Listen for the login button press
        button_login_loginActivity.setOnClickListener {
            Log.d("LoginActivity", "Login button pressed") // Logs the button press to the console
            // Check if username is empty or null
            if (email_editText_login.text.toString().isEmpty()) { // If username is empty
                email_editText_login.error = "Username cannot be empty" // Set error message
            }
            // Check if password is empty
            if (password_editText_login.text.toString().isEmpty()) { // If password is empty
                password_editText_login.error = "Password cannot be empty" // Set error message
            }
            if (email_editText_login.text.toString()
                    .isNotEmpty() && password_editText_login.text.toString().isNotEmpty()
            ) { // If username and password are not empty
                //TODO: Login the user once details have been validated
            }
        }

        // Listen for the back to sign in button press
        button_back_to_signin.setOnClickListener {
            Log.d(
                "LoginActivity",
                "Back to sign in button pressed"
            ) // Logs the button press to the console

            // Clear error messages
            email_editText_login.error = null // Clear error message
            password_editText_login.error = null // Clear error message

            //End the activity
            finish()
        }
        //TODO: Copy XML from login_activity to this XML to have identical login page


    }
}