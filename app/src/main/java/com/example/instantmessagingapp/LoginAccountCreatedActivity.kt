package com.example.instantmessagingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.login_activity.*

class LoginAccountCreatedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_account_created)

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
            if (username_editText_login.text.toString()
                    .isNotEmpty() && password_editText_login.text.toString().isNotEmpty()
            ) {
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
            finish()
        }
        //TODO: Copy XML from login_activity to this XML to have identical login page


    }
}