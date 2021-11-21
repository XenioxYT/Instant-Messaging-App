package com.example.instantmessagingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener {
            Log.d("MainActivity", "Button clicked")
            val email = email_editText_register.toString()
            val password = password_editText_register.toString()
            val confirmPassword = confirm_password_editText_register.toString()
            val username = username_editText_register.toString()

            Log.d("MainActivity", "Email is $email")
            Log.d("MainActivity", "Password is $password")
            Log.d("MainActivity", "Confirm Password is $confirmPassword")
            Log.d("MainActivity", "Username is $username")
        }
    }
}
