package com.example.instantmessagingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.login_activity.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        // Set up the login form.
        button_login_loginActivity.setOnClickListener {
            Log.d("LoginActivity", "Login button pressed")
             // Check if text fields are empty
            if (username_editText_login.text.toString().isEmpty() || password_editText_login.text.toString().isEmpty()) {
                Log.d("LoginActivity", "Login failed")
                return@setOnClickListener
        }
    }
}